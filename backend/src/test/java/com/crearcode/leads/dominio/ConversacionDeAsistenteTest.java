package com.crearcode.leads.dominio;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversacionDeAsistenteTest {

	private static MensajeDeChat deUsuario(String texto) {
		return new MensajeDeChat(RolDeMensaje.USUARIO, texto);
	}

	private static MensajeDeChat delAsistente(String texto) {
		return new MensajeDeChat(RolDeMensaje.ASISTENTE, texto);
	}

	@Test
	void creaUnaConversacionValidaQueTerminaConElUsuario() {
		ConversacionDeAsistente conversacion = new ConversacionDeAsistente(List.of(
				deUsuario("¿Qué servicios ofrecen?"),
				delAsistente("Tres líneas: desarrollo a la medida, IA y soluciones tecnológicas."),
				deUsuario("¿Cuánto tarda un proyecto?")));

		assertThat(conversacion.mensajes()).hasSize(3);
		assertThat(conversacion.ultimoMensaje().rol()).isEqualTo(RolDeMensaje.USUARIO);
	}

	@Test
	void rechazaUnaConversacionVacia() {
		assertThatThrownBy(() -> new ConversacionDeAsistente(List.of()))
				.isInstanceOf(ConversacionInvalidaException.class);
	}

	@Test
	void rechazaUnaConversacionQueNoTerminaConElUsuario() {
		assertThatThrownBy(() -> new ConversacionDeAsistente(List.of(
				deUsuario("hola"),
				delAsistente("¡Hola! ¿En qué te ayudo?"))))
				.isInstanceOf(ConversacionInvalidaException.class);
	}

	@Test
	void rechazaUnHistorialMasLargoQueElMaximo() {
		List<MensajeDeChat> demasiados = IntStream
				.rangeClosed(0, ConversacionDeAsistente.MAXIMO_DE_MENSAJES)
				.mapToObj(i -> i % 2 == 0 ? deUsuario("pregunta " + i) : delAsistente("respuesta " + i))
				.toList();

		assertThatThrownBy(() -> new ConversacionDeAsistente(demasiados))
				.isInstanceOf(ConversacionInvalidaException.class);
	}

	@Test
	void laListaDeMensajesEsInmutable() {
		ConversacionDeAsistente conversacion = new ConversacionDeAsistente(List.of(deUsuario("hola")));

		assertThatThrownBy(() -> conversacion.mensajes().add(deUsuario("otro")))
				.isInstanceOf(UnsupportedOperationException.class);
	}

}
