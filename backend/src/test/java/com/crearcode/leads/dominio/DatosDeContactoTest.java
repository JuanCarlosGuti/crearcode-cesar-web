package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatosDeContactoTest {

	private static final Correo CORREO = new Correo("nombre@empresa.com");
	private static final Telefono TELEFONO = new Telefono("3001234567");

	@Test
	void rechazaNombreNulo() {
		assertThatThrownBy(() -> new DatosDeContacto(null, "Empresa S.A.S.", CORREO, TELEFONO))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaNombreVacioOSoloEspacios() {
		assertThatThrownBy(() -> new DatosDeContacto("   ", "Empresa S.A.S.", CORREO, TELEFONO))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaNombreDemasiadoLargo() {
		String nombreLargo = "a".repeat(121);
		assertThatThrownBy(() -> new DatosDeContacto(nombreLargo, null, CORREO, TELEFONO))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaEmpresaDemasiadoLarga() {
		String empresaLarga = "a".repeat(121);
		assertThatThrownBy(() -> new DatosDeContacto("Juan Pérez", empresaLarga, CORREO, TELEFONO))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void aceptaSinEmpresa() {
		DatosDeContacto datos = new DatosDeContacto("Juan Pérez", null, CORREO, TELEFONO);

		assertThat(datos.empresa()).isNull();
	}

	@Test
	void creaConDatosValidos() {
		DatosDeContacto datos = new DatosDeContacto("Juan Pérez", "Empresa S.A.S.", CORREO, TELEFONO);

		assertThat(datos.nombre()).isEqualTo("Juan Pérez");
		assertThat(datos.empresa()).isEqualTo("Empresa S.A.S.");
		assertThat(datos.correo()).isEqualTo(CORREO);
		assertThat(datos.telefono()).isEqualTo(TELEFONO);
	}

}
