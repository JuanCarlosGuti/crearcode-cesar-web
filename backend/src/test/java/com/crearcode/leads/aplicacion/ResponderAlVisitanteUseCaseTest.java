package com.crearcode.leads.aplicacion;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.MensajeDeChat;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RolDeMensaje;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponderAlVisitanteUseCaseTest {

	private static final int LIMITE_ANONIMO = 2;
	private static final int LIMITE_REGISTRADO = 4;
	private static final int LIMITE_GLOBAL = 6;

	/** Reloj de pruebas: fijo pero avanzable, para probar el reseteo diario. */
	private static final class RelojDePruebas extends Clock {
		private Instant ahora = Instant.parse("2026-07-29T12:00:00Z");

		void avanzar(Duration duracion) {
			ahora = ahora.plus(duracion);
		}

		@Override
		public Instant instant() {
			return ahora;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zona) {
			return this;
		}
	}

	private static final class FakeGeneradorDeRespuestas implements GeneradorDeRespuestas {
		private RespuestaDelAsistente respuesta = new RespuestaDelAsistente("Con gusto te cuento.", false);
		private boolean fallar = false;
		private int llamadas = 0;

		@Override
		public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
			llamadas++;
			if (fallar) {
				throw new AsistenteNoDisponibleException("proveedor caído");
			}
			return respuesta;
		}
	}

	private RelojDePruebas reloj;
	private FakeGeneradorDeRespuestas generador;
	private ResponderAlVisitanteUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		reloj = new RelojDePruebas();
		generador = new FakeGeneradorDeRespuestas();
		useCase = new ResponderAlVisitanteUseCaseImpl(generador, reloj,
				LIMITE_GLOBAL, LIMITE_REGISTRADO, LIMITE_ANONIMO);
	}

	private static ConversacionDeAsistente pregunta(String texto) {
		return new ConversacionDeAsistente(List.of(new MensajeDeChat(RolDeMensaje.USUARIO, texto)));
	}

	@Test
	void devuelveLaRespuestaDelGenerador() {
		RespuestaDelAsistente respuesta = useCase.responder(pregunta("¿Qué servicios ofrecen?"),
				IdentidadDelVisitante.anonima("sesion-1"));

		assertThat(respuesta.texto()).isEqualTo("Con gusto te cuento.");
		assertThat(generador.llamadas).isEqualTo(1);
	}

	@Test
	void unAnonimoQueAgotaSuLimiteDiarioRecibeLimiteAlcanzadoSinLlamarAlProveedor() {
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("sesion-1");
		useCase.responder(pregunta("uno"), anonimo);
		useCase.responder(pregunta("dos"), anonimo);

		assertThatThrownBy(() -> useCase.responder(pregunta("tres"), anonimo))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class)
				.satisfies(e -> assertThat(((LimiteDeUsoAlcanzadoException) e).identidadRegistrada()).isFalse());
		assertThat(generador.llamadas).isEqualTo(2);
	}

	@Test
	void otroAnonimoConservaSuPropioCupo() {
		useCase.responder(pregunta("uno"), IdentidadDelVisitante.anonima("sesion-1"));
		useCase.responder(pregunta("dos"), IdentidadDelVisitante.anonima("sesion-1"));

		assertThatCode(() -> useCase.responder(pregunta("hola"), IdentidadDelVisitante.anonima("sesion-2")))
				.doesNotThrowAnyException();
	}

	@Test
	void unRegistradoTieneUnLimiteMayorQueElAnonimo() {
		IdentidadDelVisitante registrado = IdentidadDelVisitante.registrada("cliente@correo-de-prueba.com");
		for (int i = 0; i < LIMITE_REGISTRADO; i++) {
			useCase.responder(pregunta("pregunta " + i), registrado);
		}

		assertThatThrownBy(() -> useCase.responder(pregunta("otra"), registrado))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class)
				.satisfies(e -> assertThat(((LimiteDeUsoAlcanzadoException) e).identidadRegistrada()).isTrue());
	}

	@Test
	void alAgotarseElLimiteGlobalDiarioNadieMasPuedePreguntar() {
		for (int i = 0; i < LIMITE_GLOBAL; i++) {
			useCase.responder(pregunta("pregunta " + i), IdentidadDelVisitante.anonima("sesion-" + i));
		}

		assertThatThrownBy(() -> useCase.responder(pregunta("otra"), IdentidadDelVisitante.anonima("sesion-nueva")))
				.isInstanceOf(LimiteGlobalAlcanzadoException.class);
		assertThat(generador.llamadas).isEqualTo(LIMITE_GLOBAL);
	}

	@Test
	void unFalloDelProveedorNoConsumeCupo() {
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("sesion-1");
		generador.fallar = true;

		assertThatThrownBy(() -> useCase.responder(pregunta("hola"), anonimo))
				.isInstanceOf(AsistenteNoDisponibleException.class);

		generador.fallar = false;
		useCase.responder(pregunta("uno"), anonimo);
		assertThatCode(() -> useCase.responder(pregunta("dos"), anonimo)).doesNotThrowAnyException();
	}

	@Test
	void losContadoresSeReseteanAlDiaSiguiente() {
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("sesion-1");
		useCase.responder(pregunta("uno"), anonimo);
		useCase.responder(pregunta("dos"), anonimo);

		reloj.avanzar(Duration.ofDays(1));

		assertThatCode(() -> useCase.responder(pregunta("nuevo día"), anonimo)).doesNotThrowAnyException();
	}

}
