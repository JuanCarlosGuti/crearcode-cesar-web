package com.crearcode.leads.infraestructura.asistente;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.MensajeDeChat;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RolDeMensaje;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El 28 sep 2026 Groq retiró el modelo configurado y las tres
 * herramientas de IA se quedaron sin responder durante días, porque el
 * puerto tenía un único montaje: no había a dónde caerse. El demo de
 * diseño, que sí tenía respaldo desde ISS-137, siguió funcionando ese
 * mismo día. Esta clase le da al texto la misma red.
 */
class GeneradorDeRespuestasConRespaldoTest {

	private final List<String> llamados = new ArrayList<>();

	private final ConversacionDeAsistente conversacion = new ConversacionDeAsistente(
			List.of(new MensajeDeChat(RolDeMensaje.USUARIO, "¿qué servicios ofrecen?")));

	private GeneradorDeRespuestas queResponde(String nombre) {
		return new GeneradorDeRespuestas() {
			@Override
			public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
				return responder("contexto por defecto", conversacion);
			}

			@Override
			public RespuestaDelAsistente responder(String contexto, ConversacionDeAsistente conversacion) {
				llamados.add(nombre);
				return new RespuestaDelAsistente("responde " + nombre, false);
			}
		};
	}

	private GeneradorDeRespuestas queFalla(String nombre) {
		return new GeneradorDeRespuestas() {
			@Override
			public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
				return responder("contexto por defecto", conversacion);
			}

			@Override
			public RespuestaDelAsistente responder(String contexto, ConversacionDeAsistente conversacion) {
				llamados.add(nombre);
				throw new AsistenteNoDisponibleException("falló " + nombre);
			}
		};
	}

	@Test
	void usaElPrimarioYNiSiquieraLlamaAlRespaldoCuandoTodoVaBien() {
		GeneradorDeRespuestas generador = new GeneradorDeRespuestasConRespaldo(
				queResponde("primario"), queResponde("respaldo"));

		assertThat(generador.responder(conversacion).texto()).isEqualTo("responde primario");
		assertThat(llamados).containsExactly("primario");
	}

	@Test
	void cuandoElPrimarioFallaRespondeElRespaldo() {
		GeneradorDeRespuestas generador = new GeneradorDeRespuestasConRespaldo(
				queFalla("primario"), queResponde("respaldo"));

		assertThat(generador.responder(conversacion).texto()).isEqualTo("responde respaldo");
		assertThat(llamados).containsExactly("primario", "respaldo");
	}

	/**
	 * El simulador, el diagnóstico y el demo pasan su propio contexto de
	 * sistema: si el respaldo no lo recibiera, respondería como el
	 * asistente general y el visitante vería una respuesta fuera de sitio.
	 */
	@Test
	void elRespaldoRecibeElMismoContextoDeSistemaQueElPrimario() {
		List<String> contextos = new ArrayList<>();
		GeneradorDeRespuestas espia = new GeneradorDeRespuestas() {
			@Override
			public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
				throw new UnsupportedOperationException("no se usa en esta prueba");
			}

			@Override
			public RespuestaDelAsistente responder(String contexto, ConversacionDeAsistente conversacion) {
				contextos.add(contexto);
				return new RespuestaDelAsistente("ok", false);
			}
		};
		GeneradorDeRespuestas generador = new GeneradorDeRespuestasConRespaldo(queFalla("primario"), espia);

		generador.responder("eres el chatbot de una ferretería", conversacion);

		assertThat(contextos).containsExactly("eres el chatbot de una ferretería");
	}

	@Test
	void siAmbosFallanElVisitanteRecibeElErrorEstandarDeIndisponibilidad() {
		GeneradorDeRespuestas generador = new GeneradorDeRespuestasConRespaldo(
				queFalla("primario"), queFalla("respaldo"));

		assertThatThrownBy(() -> generador.responder(conversacion))
				.isInstanceOf(AsistenteNoDisponibleException.class);
		assertThat(llamados).containsExactly("primario", "respaldo");
	}

}
