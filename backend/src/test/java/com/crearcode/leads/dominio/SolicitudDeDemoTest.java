package com.crearcode.leads.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SolicitudDeDemoTest {

	@Test
	void normalizaLosTresCampos() {
		SolicitudDeDemo solicitud = new SolicitudDeDemo("  Restaurante  ",
				"  Vendemos almuerzos y domicilios  ", "  Recibir pedidos sin saturar el WhatsApp  ");

		assertThat(solicitud.sector()).isEqualTo("Restaurante");
		assertThat(solicitud.queHace()).isEqualTo("Vendemos almuerzos y domicilios");
		assertThat(solicitud.queNecesita()).isEqualTo("Recibir pedidos sin saturar el WhatsApp");
	}

	@Test
	void rechazaCamposVacios() {
		assertThatThrownBy(() -> new SolicitudDeDemo(" ", "hace", "necesita"))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
		assertThatThrownBy(() -> new SolicitudDeDemo("sector", " ", "necesita"))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
		assertThatThrownBy(() -> new SolicitudDeDemo("sector", "hace", " "))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
	}

	@Test
	void rechazaTextosDemasiadoLargos() {
		assertThatThrownBy(() -> new SolicitudDeDemo("x".repeat(61), "hace", "necesita"))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
		assertThatThrownBy(() -> new SolicitudDeDemo("sector", "x".repeat(301), "necesita"))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
		assertThatThrownBy(() -> new SolicitudDeDemo("sector", "hace", "x".repeat(301)))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
	}

}
