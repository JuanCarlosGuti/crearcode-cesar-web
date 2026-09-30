package com.crearcode.leads.dominio;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DineroTest {

	@Test
	void rechazaMontoNulo() {
		assertThatThrownBy(() -> new Dinero(null))
				.isInstanceOf(MontoInvalidoException.class);
	}

	@Test
	void rechazaMontoNegativo() {
		assertThatThrownBy(() -> Dinero.de(-1))
				.isInstanceOf(MontoInvalidoException.class);
	}

	@Test
	void aceptaCero() {
		assertThat(Dinero.de(0)).isEqualTo(Dinero.CERO);
	}

	// El peso colombiano no maneja centavos en la practica comercial: el
	// VO normaliza la escala para que dos montos equivalentes sean
	// iguales y los totales no arrastren decimales fantasma.
	@Test
	void normalizaLaEscalaAPesosEnteros() {
		assertThat(new Dinero(new BigDecimal("1500.00"))).isEqualTo(Dinero.de(1500));
	}

	@Test
	void redondeaLosDecimalesAlPesoMasCercano() {
		assertThat(new Dinero(new BigDecimal("1500.6"))).isEqualTo(Dinero.de(1501));
	}

	@Test
	void sumaDosMontos() {
		assertThat(Dinero.de(1500).mas(Dinero.de(2500))).isEqualTo(Dinero.de(4000));
	}

	@Test
	void multiplicaPorUnaCantidad() {
		assertThat(Dinero.de(1500).por(3)).isEqualTo(Dinero.de(4500));
	}

	@Test
	void calculaUnPorcentajeDelMonto() {
		assertThat(Dinero.de(1_000_000).porcentaje(new Porcentaje(19))).isEqualTo(Dinero.de(190_000));
	}

	@Test
	void unPorcentajeDeCeroNoAgregaNada() {
		assertThat(Dinero.de(1_000_000).porcentaje(new Porcentaje(0))).isEqualTo(Dinero.CERO);
	}

	@Test
	void esIgualPorValor() {
		assertThat(Dinero.de(5000)).isEqualTo(Dinero.de(5000));
	}

	// F12: saldos y pagos parciales necesitan restar y comparar.
	@Test
	void restaDosMontos() {
		assertThat(Dinero.de(1_000_000).menos(Dinero.de(400_000))).isEqualTo(Dinero.de(600_000));
	}

	@Test
	void unaRestaQueQuedariaNegativaSeRechaza() {
		assertThatThrownBy(() -> Dinero.de(100).menos(Dinero.de(101)))
				.isInstanceOf(MontoInvalidoException.class);
	}

	@Test
	void comparaMontos() {
		assertThat(Dinero.de(2).esMayorQue(Dinero.de(1))).isTrue();
		assertThat(Dinero.de(1).esMayorQue(Dinero.de(1))).isFalse();
		assertThat(Dinero.CERO.esCero()).isTrue();
		assertThat(Dinero.de(1).esCero()).isFalse();
	}

}
