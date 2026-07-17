package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorreoTest {

	@Test
	void rechazaValorNulo() {
		assertThatThrownBy(() -> new Correo(null))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaValorVacioOSoloEspacios() {
		assertThatThrownBy(() -> new Correo("   "))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaFormatoInvalido() {
		assertThatThrownBy(() -> new Correo("no-es-un-correo"))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaCorreoDemasiadoLargo() {
		String local = "a".repeat(250);
		assertThatThrownBy(() -> new Correo(local + "@empresa.com"))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void aceptaFormatoValido() {
		Correo correo = new Correo("nombre@empresa.com");

		assertThat(correo.valor()).isEqualTo("nombre@empresa.com");
	}

	@Test
	void esIgualPorValor() {
		assertThat(new Correo("nombre@empresa.com")).isEqualTo(new Correo("nombre@empresa.com"));
	}

}
