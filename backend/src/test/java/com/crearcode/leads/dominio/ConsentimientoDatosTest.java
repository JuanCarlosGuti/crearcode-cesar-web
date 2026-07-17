package com.crearcode.leads.dominio;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsentimientoDatosTest {

	@Test
	void rechazaFechaAceptacionNula() {
		assertThatThrownBy(() -> new ConsentimientoDatos(true, null, "v1"))
				.isInstanceOf(ConsentimientoRequeridoException.class);
	}

	@Test
	void rechazaVersionPoliticaNulaOVacia() {
		assertThatThrownBy(() -> new ConsentimientoDatos(true, Instant.now(), " "))
				.isInstanceOf(ConsentimientoRequeridoException.class);
	}

	@Test
	void creaConAceptadoTrue() {
		Instant ahora = Instant.now();

		ConsentimientoDatos consentimiento = new ConsentimientoDatos(true, ahora, "v1");

		assertThat(consentimiento.aceptado()).isTrue();
		assertThat(consentimiento.fechaAceptacion()).isEqualTo(ahora);
		assertThat(consentimiento.versionPoliticaAceptada()).isEqualTo("v1");
	}

	@Test
	void permiteRepresentarConsentimientoNoAceptado() {
		ConsentimientoDatos consentimiento = new ConsentimientoDatos(false, Instant.now(), "v1");

		assertThat(consentimiento.aceptado()).isFalse();
	}

}
