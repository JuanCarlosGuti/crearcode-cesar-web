package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatosDelClienteTest {

	private static final Correo CORREO = new Correo("cliente@empresa.com");

	@Test
	void rechazaNombreVacio() {
		assertThatThrownBy(() -> new DatosDelCliente("   ", CORREO, null, null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaNombreDemasiadoLargo() {
		assertThatThrownBy(() -> new DatosDelCliente("a".repeat(121), CORREO, null, null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaCorreoNulo() {
		assertThatThrownBy(() -> new DatosDelCliente("Panaderia El Trigal", null, null, null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	// Telefono e identificacion son opcionales: la app no hace nada
	// fiscal con el NIT, solo lo imprime en el PDF si el cliente lo da.
	@Test
	void aceptaTelefonoEIdentificacionAusentes() {
		DatosDelCliente cliente = new DatosDelCliente("Panaderia El Trigal", CORREO, null, null);

		assertThat(cliente.telefono()).isNull();
		assertThat(cliente.identificacion()).isNull();
	}

	@Test
	void normalizaLosOpcionalesEnBlancoANulo() {
		DatosDelCliente cliente = new DatosDelCliente("Panaderia El Trigal", CORREO, "  ", "");

		assertThat(cliente.telefono()).isNull();
		assertThat(cliente.identificacion()).isNull();
	}

	@Test
	void recortaLosEspaciosDelNombre() {
		assertThat(new DatosDelCliente("  Panaderia El Trigal  ", CORREO, null, null).nombre())
				.isEqualTo("Panaderia El Trigal");
	}

	@Test
	void esIgualPorValor() {
		assertThat(new DatosDelCliente("Panaderia El Trigal", CORREO, "3001234567", "900123456-1"))
				.isEqualTo(new DatosDelCliente("Panaderia El Trigal", CORREO, "3001234567", "900123456-1"));
	}

}
