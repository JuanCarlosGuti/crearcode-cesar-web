package com.crearcode.leads.infraestructura;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CargadorDeProyectoDeDemostracionTest {

	/**
	 * Segundo candado: aunque alguien encienda la carga en un entorno con
	 * credenciales de producción, la aplicación no arranca. Una cuenta con
	 * contraseña conocida no puede llegar a producción por un descuido.
	 */
	@Test
	void seNiegaAArrancarDondeNoSeAdmitenCredencialesDeDesarrollo() {
		assertThatThrownBy(() -> new CargadorDeProyectoDeDemostracion(null, null, null, null, "admin@ejemplo.co",
				false))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("PERMITIR_CREDENCIALES_DE_DESARROLLO");
	}

}
