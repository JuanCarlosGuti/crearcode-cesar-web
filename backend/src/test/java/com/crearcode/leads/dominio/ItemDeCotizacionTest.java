package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemDeCotizacionTest {

	@Test
	void rechazaDescripcionVacia() {
		assertThatThrownBy(() -> new ItemDeCotizacion("   ", 1, Dinero.de(1000)))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaDescripcionDemasiadoLarga() {
		assertThatThrownBy(() -> new ItemDeCotizacion("a".repeat(201), 1, Dinero.de(1000)))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaCantidadNoPositiva() {
		assertThatThrownBy(() -> new ItemDeCotizacion("Desarrollo del modulo de pedidos", 0, Dinero.de(1000)))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaValorUnitarioNulo() {
		assertThatThrownBy(() -> new ItemDeCotizacion("Desarrollo del modulo de pedidos", 1, null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	// El subtotal se CALCULA siempre (invariante 2 del contexto): nadie
	// puede inyectar un total distinto del que sale de cantidad x valor.
	@Test
	void calculaElSubtotalDesdeLaCantidadYElValorUnitario() {
		ItemDeCotizacion item = new ItemDeCotizacion("Horas de desarrollo", 40, Dinero.de(120_000));

		assertThat(item.subtotal()).isEqualTo(Dinero.de(4_800_000));
	}

	@Test
	void aceptaValorUnitarioCero() {
		ItemDeCotizacion item = new ItemDeCotizacion("Capacitacion incluida sin costo", 1, Dinero.CERO);

		assertThat(item.subtotal()).isEqualTo(Dinero.CERO);
	}

	@Test
	void esIgualPorValor() {
		assertThat(new ItemDeCotizacion("Horas de desarrollo", 40, Dinero.de(120_000)))
				.isEqualTo(new ItemDeCotizacion("Horas de desarrollo", 40, Dinero.de(120_000)));
	}

}
