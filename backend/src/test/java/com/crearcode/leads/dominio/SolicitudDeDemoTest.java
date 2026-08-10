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

	@Test
	void unBocetoValidoRequiereTituloYEntre3Y6Funcionalidades() {
		ImagenGenerada imagen = new ImagenGenerada("YmFzZTY0", "image/png");
		BocetoDeDemo boceto = new BocetoDeDemo("App de pedidos", java.util.List.of("a", "b", "c"), imagen);

		assertThat(boceto.titulo()).isEqualTo("App de pedidos");
		assertThat(boceto.funcionalidades()).hasSize(3);

		assertThatThrownBy(() -> new BocetoDeDemo(" ", java.util.List.of("a", "b", "c"), imagen))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
		assertThatThrownBy(() -> new BocetoDeDemo("App", java.util.List.of("a", "b"), imagen))
				.isInstanceOf(SolicitudDeDemoInvalidaException.class);
	}

}
