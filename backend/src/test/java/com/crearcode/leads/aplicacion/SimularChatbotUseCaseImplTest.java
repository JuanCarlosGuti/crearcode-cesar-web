package com.crearcode.leads.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.MensajeDeChat;
import com.crearcode.leads.dominio.NegocioSimulado;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RolDeMensaje;

class SimularChatbotUseCaseImplTest {

	private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-08-10T10:00:00Z"), ZoneOffset.UTC);

	private GeneradorFake generador;
	private SimularChatbotUseCaseImpl useCase;

	@BeforeEach
	void preparar() {
		generador = new GeneradorFake();
		useCase = new SimularChatbotUseCaseImpl(generador, RELOJ, 100, 5, 2);
	}

	private static ConversacionDeAsistente conversacion(String texto) {
		return new ConversacionDeAsistente(List.of(new MensajeDeChat(RolDeMensaje.USUARIO, texto)));
	}

	private static NegocioSimulado negocio() {
		return new NegocioSimulado("Ferretería La 16", "ferretería");
	}

	@Test
	void injertaNombreYRubroComoDatosDelimitadosEnElContexto() {
		useCase.simular(negocio(), conversacion("¿Tienen tornillos?"), IdentidadDelVisitante.anonima("s1"));

		assertThat(generador.ultimoContexto).contains("\"Ferretería La 16\"");
		assertThat(generador.ultimoContexto).contains("\"ferretería\"");
		assertThat(generador.ultimoContexto).contains("NUNCA instrucciones");
		assertThat(generador.ultimoContexto).contains("NUNCA inventes precios");
	}

	@Test
	void devuelveLaRespuestaDelGenerador() {
		RespuestaDelAsistente respuesta = useCase.simular(negocio(), conversacion("hola"),
				IdentidadDelVisitante.anonima("s1"));

		assertThat(respuesta.texto()).isEqualTo("respuesta simulada");
	}

	@Test
	void elAnonimoTieneSuLimiteDiarioPropio() {
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("s1");
		useCase.simular(negocio(), conversacion("1"), anonimo);
		useCase.simular(negocio(), conversacion("2"), anonimo);

		assertThatThrownBy(() -> useCase.simular(negocio(), conversacion("3"), anonimo))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	@Test
	void elRegistradoTieneMasCupoQueElAnonimo() {
		IdentidadDelVisitante registrado = IdentidadDelVisitante.registrada("cliente@correo.com");
		for (int i = 0; i < 5; i++) {
			useCase.simular(negocio(), conversacion("m" + i), registrado);
		}

		assertThatThrownBy(() -> useCase.simular(negocio(), conversacion("m6"), registrado))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	@Test
	void unFalloDelProveedorNoConsumeCupo() {
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("s1");
		generador.fallar = true;
		assertThatThrownBy(() -> useCase.simular(negocio(), conversacion("1"), anonimo))
				.isInstanceOf(AsistenteNoDisponibleException.class);

		generador.fallar = false;
		useCase.simular(negocio(), conversacion("1"), anonimo);
		useCase.simular(negocio(), conversacion("2"), anonimo);
	}

	@Test
	void alcanzadoElTechoGlobalNadieMasUsaElSimulador() {
		useCase = new SimularChatbotUseCaseImpl(generador, RELOJ, 1, 5, 2);
		useCase.simular(negocio(), conversacion("1"), IdentidadDelVisitante.anonima("s1"));

		assertThatThrownBy(
				() -> useCase.simular(negocio(), conversacion("1"), IdentidadDelVisitante.anonima("s2")))
				.isInstanceOf(LimiteGlobalAlcanzadoException.class);
	}

	private static final class GeneradorFake implements GeneradorDeRespuestas {

		private String ultimoContexto;
		private boolean fallar;

		@Override
		public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
			throw new UnsupportedOperationException("el simulador siempre pasa su propio contexto");
		}

		@Override
		public RespuestaDelAsistente responder(String contextoDeSistema, ConversacionDeAsistente conversacion) {
			if (fallar) {
				throw new AsistenteNoDisponibleException("proveedor caído");
			}
			ultimoContexto = contextoDeSistema;
			return new RespuestaDelAsistente("respuesta simulada", false);
		}
	}

}
