package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContrasenaPlanaTest {

	@Test
	void aceptaUnaContrasenaDeAlMenosDiezCaracteres() {
		ContrasenaPlana contrasena = new ContrasenaPlana("1234567890");

		assertThat(contrasena.valor()).isEqualTo("1234567890");
	}

	@Test
	void rechazaUnaContrasenaDeMenosDeDiezCaracteres() {
		assertThatThrownBy(() -> new ContrasenaPlana("123456789"))
				.isInstanceOf(ContrasenaInvalidaException.class);
	}

	@Test
	void rechazaUnaContrasenaNula() {
		assertThatThrownBy(() -> new ContrasenaPlana(null))
				.isInstanceOf(ContrasenaInvalidaException.class);
	}

	@Test
	void rechazaUnaContrasenaEnBlanco() {
		assertThatThrownBy(() -> new ContrasenaPlana("          "))
				.isInstanceOf(ContrasenaInvalidaException.class);
	}

	@Test
	void suToStringNoReveleLaContrasena() {
		ContrasenaPlana contrasena = new ContrasenaPlana("secreta-de-verdad");

		assertThat(contrasena.toString()).doesNotContain("secreta-de-verdad");
	}

}
