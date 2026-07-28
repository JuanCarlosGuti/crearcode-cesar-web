package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MensajeDeChatTest {

	@Test
	void creaUnMensajeDeUsuarioConElTextoNormalizado() {
		MensajeDeChat mensaje = new MensajeDeChat(RolDeMensaje.USUARIO, "  ¿Qué servicios ofrecen?  ");

		assertThat(mensaje.rol()).isEqualTo(RolDeMensaje.USUARIO);
		assertThat(mensaje.texto()).isEqualTo("¿Qué servicios ofrecen?");
	}

	@Test
	void rechazaUnTextoVacioOEnBlanco() {
		assertThatThrownBy(() -> new MensajeDeChat(RolDeMensaje.USUARIO, "   "))
				.isInstanceOf(MensajeDeChatInvalidoException.class);
	}

	@Test
	void rechazaUnTextoMasLargoQueElMaximo() {
		String demasiadoLargo = "a".repeat(MensajeDeChat.LONGITUD_MAXIMA + 1);

		assertThatThrownBy(() -> new MensajeDeChat(RolDeMensaje.USUARIO, demasiadoLargo))
				.isInstanceOf(MensajeDeChatInvalidoException.class);
	}

	@Test
	void aceptaUnTextoDeExactamenteLaLongitudMaxima() {
		String alLimite = "a".repeat(MensajeDeChat.LONGITUD_MAXIMA);

		assertThat(new MensajeDeChat(RolDeMensaje.ASISTENTE, alLimite).texto()).hasSize(MensajeDeChat.LONGITUD_MAXIMA);
	}

	@Test
	void rechazaUnRolNulo() {
		assertThatThrownBy(() -> new MensajeDeChat(null, "hola"))
				.isInstanceOf(NullPointerException.class);
	}

}
