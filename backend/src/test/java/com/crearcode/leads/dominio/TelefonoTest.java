package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefonoTest {

	@Test
	void rechazaValorNulo() {
		assertThatThrownBy(() -> new Telefono(null))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaValorVacioOSoloEspacios() {
		assertThatThrownBy(() -> new Telefono("   "))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaNumeroQueNoEmpiezaPorTres() {
		assertThatThrownBy(() -> new Telefono("2001234567"))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaLongitudIncorrecta() {
		assertThatThrownBy(() -> new Telefono("30012345"))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void aceptaCelularSimple() {
		Telefono telefono = new Telefono("3001234567");

		assertThat(telefono.valor()).isEqualTo("3001234567");
	}

	@Test
	void normalizaEspaciosYGuiones() {
		assertThat(new Telefono("300 123-4567")).isEqualTo(new Telefono("3001234567"));
	}

	@Test
	void normalizaPrefijoInternacionalConMas() {
		assertThat(new Telefono("+57 300 123 4567")).isEqualTo(new Telefono("3001234567"));
	}

	@Test
	void normalizaPrefijoSinMas() {
		assertThat(new Telefono("57 300 123 4567")).isEqualTo(new Telefono("3001234567"));
	}

}
