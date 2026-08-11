package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PorcentajeTest {

	@Test
	void rechazaValorNegativo() {
		assertThatThrownBy(() -> new Porcentaje(-1))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaValorMayorACien() {
		assertThatThrownBy(() -> new Porcentaje(101))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	// Cero es valido a proposito: la empresa aun no confirma su condicion
	// de IVA (docs/05, datos pendientes de F11).
	@Test
	void aceptaCeroYCien() {
		assertThat(new Porcentaje(0).valor()).isZero();
		assertThat(new Porcentaje(100).valor()).isEqualTo(100);
	}

	@Test
	void esIgualPorValor() {
		assertThat(new Porcentaje(19)).isEqualTo(new Porcentaje(19));
	}

}
