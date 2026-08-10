package com.crearcode.leads.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NegocioSimuladoTest {

	@Test
	void normalizaEspaciosDeNombreYRubro() {
		NegocioSimulado negocio = new NegocioSimulado("  Ferretería La 16  ", "  ferretería  ");

		assertThat(negocio.nombre()).isEqualTo("Ferretería La 16");
		assertThat(negocio.rubro()).isEqualTo("ferretería");
	}

	@Test
	void rechazaNombreVacio() {
		assertThatThrownBy(() -> new NegocioSimulado("   ", "ferretería"))
				.isInstanceOf(NegocioSimuladoInvalidoException.class);
	}

	@Test
	void rechazaRubroVacio() {
		assertThatThrownBy(() -> new NegocioSimulado("Ferretería La 16", " "))
				.isInstanceOf(NegocioSimuladoInvalidoException.class);
	}

	@Test
	void rechazaNombreDeMasDe60Caracteres() {
		assertThatThrownBy(() -> new NegocioSimulado("x".repeat(61), "ferretería"))
				.isInstanceOf(NegocioSimuladoInvalidoException.class);
	}

	@Test
	void rechazaRubroDeMasDe40Caracteres() {
		assertThatThrownBy(() -> new NegocioSimulado("Ferretería", "x".repeat(41)))
				.isInstanceOf(NegocioSimuladoInvalidoException.class);
	}

	@Test
	void aceptaLosLargosMaximos() {
		NegocioSimulado negocio = new NegocioSimulado("x".repeat(60), "y".repeat(40));

		assertThat(negocio.nombre()).hasSize(60);
		assertThat(negocio.rubro()).hasSize(40);
	}

}
