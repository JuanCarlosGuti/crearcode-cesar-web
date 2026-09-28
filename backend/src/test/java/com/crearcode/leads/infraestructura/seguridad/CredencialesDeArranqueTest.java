package com.crearcode.leads.infraestructura.seguridad;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code ADMIN_PASSWORD} y {@code JWT_SECRET} tienen un valor por
 * defecto para que el proyecto arranque recién clonado. El riesgo es
 * que ese valor llegue a producción sin que nadie se entere: un
 * secreto que no se carga no rompe nada visible — la app arranca, el
 * login funciona, y cualquiera que haya leído el repositorio puede
 * firmar un token de administrador.
 *
 * <p>
 * Con {@code app.seguridad.permitir-credenciales-de-desarrollo=false},
 * que es lo que pone el despliegue, arrancar con esos valores es un
 * error de arranque y no una nota al pie.
 */
class CredencialesDeArranqueTest {

	private static final String ADMIN_POR_DEFECTO = "cambiar-en-produccion";
	private static final String JWT_POR_DEFECTO = "cambiar-este-secreto-en-produccion-1234567890";
	private static final String JWT_BUENO = "un-secreto-largo-de-verdad-con-mas-de-32-caracteres";

	@Test
	void enDesarrolloLosValoresPorDefectoDejanArrancar() {
		assertThatCode(() -> new CredencialesDeArranque(true, ADMIN_POR_DEFECTO, JWT_POR_DEFECTO).verificar())
				.doesNotThrowAnyException();
	}

	@Test
	void enProduccionLaContrasenaDeAdminPorDefectoImpideArrancar() {
		assertThatThrownBy(() -> new CredencialesDeArranque(false, ADMIN_POR_DEFECTO, JWT_BUENO).verificar())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ADMIN_PASSWORD");
	}

	@Test
	void enProduccionElSecretoJwtPorDefectoImpideArrancar() {
		assertThatThrownBy(() -> new CredencialesDeArranque(false, "una-contrasena-de-verdad", JWT_POR_DEFECTO)
				.verificar())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET");
	}

	@Test
	void enProduccionUnaVariableVaciaImpideArrancar() {
		assertThatThrownBy(() -> new CredencialesDeArranque(false, "   ", JWT_BUENO).verificar())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ADMIN_PASSWORD");
	}

	/**
	 * HS256 con una clave más corta que su salida es un secreto que se
	 * puede buscar por fuerza bruta; la propia librería lo rechaza, pero
	 * mejor enterarse al arrancar que en la primera petición de login.
	 */
	@Test
	void enProduccionUnSecretoJwtCortoImpideArrancar() {
		assertThatThrownBy(() -> new CredencialesDeArranque(false, "una-contrasena-de-verdad", "corto")
				.verificar())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET");
	}

	@Test
	void enProduccionConCredencialesDeVerdadArranca() {
		assertThatCode(() -> new CredencialesDeArranque(false, "una-contrasena-de-verdad", JWT_BUENO).verificar())
				.doesNotThrowAnyException();
	}

	/**
	 * El mensaje va a los logs del servidor: tiene que decir qué falta,
	 * nunca cuánto vale.
	 */
	@Test
	void elErrorNombraLaVariableSinFiltrarSuValor() {
		assertThatThrownBy(() -> new CredencialesDeArranque(false, "una-contrasena-de-verdad", JWT_POR_DEFECTO)
				.verificar())
				.satisfies(error -> assertThat(error.getMessage()).doesNotContain(JWT_POR_DEFECTO));
	}

}
