package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumeroDeCotizacionTest {

	@Test
	void rechazaValorNulo() {
		assertThatThrownBy(() -> new NumeroDeCotizacion(null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaFormatoQueNoEsElDelConsecutivo() {
		assertThatThrownBy(() -> new NumeroDeCotizacion("1234"))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void seArmaConElAnioYElConsecutivoRellenadoACuatroDigitos() {
		assertThat(NumeroDeCotizacion.de(2026, 7).valor()).isEqualTo("COT-2026-0007");
	}

	@Test
	void noTruncaConsecutivosDeMasDeCuatroDigitos() {
		assertThat(NumeroDeCotizacion.de(2026, 12345).valor()).isEqualTo("COT-2026-12345");
	}

	@Test
	void rechazaConsecutivoNoPositivo() {
		assertThatThrownBy(() -> NumeroDeCotizacion.de(2026, 0))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void esIgualPorValor() {
		assertThat(NumeroDeCotizacion.de(2026, 1)).isEqualTo(new NumeroDeCotizacion("COT-2026-0001"));
	}

}
