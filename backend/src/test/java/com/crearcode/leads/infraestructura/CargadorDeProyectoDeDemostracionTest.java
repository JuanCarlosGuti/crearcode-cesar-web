package com.crearcode.leads.infraestructura;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CargadorDeProyectoDeDemostracionTest {

	/**
	 * Donde no se admiten credenciales de desarrollo (producción), la
	 * contraseña de la cuenta de demostración tiene que venir de un
	 * secreto: la de por defecto está en el repositorio. Si no, la demo
	 * no se carga —y no se toca nada: los repositorios nulos lo
	 * comprobarían con un NullPointerException—, pero la aplicación
	 * arranca igual. Una demo mal configurada no puede tumbar el sitio.
	 */
	@Test
	void enProduccionConLaContrasenaDelRepositorioNoCargaNiTumbaNada() {
		CargadorDeProyectoDeDemostracion cargador = new CargadorDeProyectoDeDemostracion(null, null, null, null,
				"admin@ejemplo.co", "demo-del-portal", false);

		assertThat(cargador.puedeCargar()).isFalse();
		assertThatCode(() -> cargador.run(null)).doesNotThrowAnyException();
	}

	@Test
	void enProduccionSinContrasenaTampocoCarga() {
		assertThat(new CargadorDeProyectoDeDemostracion(null, null, null, null, "admin@ejemplo.co", "  ", false)
				.puedeCargar()).isFalse();
	}

	@Test
	void enProduccionConUnaContrasenaPropiaSiCarga() {
		assertThat(new CargadorDeProyectoDeDemostracion(null, null, null, null, "admin@ejemplo.co",
				"una-contrasena-de-secreto", false).puedeCargar()).isTrue();
	}

	@Test
	void enLocalLaContrasenaDelRepositorioSirve() {
		assertThat(new CargadorDeProyectoDeDemostracion(null, null, null, null, "admin@ejemplo.co",
				"demo-del-portal", true).puedeCargar()).isTrue();
	}

	/** El mínimo de cualquier contraseña de cliente (ContrasenaPlana). */
	@Test
	void unaContrasenaCortaNoSirveNiEnLocal() {
		assertThat(new CargadorDeProyectoDeDemostracion(null, null, null, null, "admin@ejemplo.co", "corta", true)
				.puedeCargar()).isFalse();
	}

}
