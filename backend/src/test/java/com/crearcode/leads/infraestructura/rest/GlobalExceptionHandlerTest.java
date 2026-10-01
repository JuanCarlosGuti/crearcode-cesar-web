package com.crearcode.leads.infraestructura.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.crearcode.leads.aplicacion.LimiteGlobalAlcanzadoException;
import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.MontoInvalidoException;
import com.crearcode.leads.dominio.PorcentajeInvalidoException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El visitante ve un 503 en los dos casos, pero el código NO puede ser
 * el mismo: durante la auditoría del 28 sep 2026 las tres herramientas
 * respondieron {@code no-disponible} durante días y desde fuera era
 * imposible saber si se había agotado el cupo del día o si el proveedor
 * estaba caído (era lo segundo: Groq retiró el modelo). Con códigos
 * distintos, la interfaz dice la verdad y el monitoreo puede alertar
 * solo sobre lo que es una avería.
 */
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler manejador = new GlobalExceptionHandler();

	@Test
	void elCupoGlobalAgotadoRespondeConSuPropioCodigo() {
		ResponseEntity<ErrorAsistenteResponse> respuesta = manejador
				.limiteGlobalAlcanzado(new LimiteGlobalAlcanzadoException());

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(respuesta.getBody().codigo()).isEqualTo("limite-global");
	}

	@Test
	void elProveedorCaidoRespondeConSuPropioCodigo() {
		ResponseEntity<ErrorAsistenteResponse> respuesta = manejador
				.proveedorDeIaCaido(new AsistenteNoDisponibleException("Fallo llamando al proveedor de IA"));

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(respuesta.getBody().codigo()).isEqualTo("proveedor-caido");
	}

	@Test
	void ningunaDeLasDosRespuestasFiltraElDetalleTecnicoAlVisitante() {
		ResponseEntity<ErrorAsistenteResponse> respuesta = manejador.proveedorDeIaCaido(
				new AsistenteNoDisponibleException("404 model_not_found: llama-3.3-70b-versatile"));

		assertThat(respuesta.getBody().mensaje()).doesNotContain("model_not_found")
				.doesNotContain("llama-3.3-70b-versatile");
	}

	/**
	 * Dinero y Porcentaje los usan cotizaciones y proyectos (F12), asi
	 * que sus errores ya no se llaman "cotizacion invalida" — pero el
	 * cliente de la API tiene que seguir recibiendo el mismo 400.
	 */
	@Test
	void unMontoOPorcentajeInvalidoSigueSiendoUn400ConSuMensaje() {
		ResponseEntity<ErrorResponse> monto = manejador
				.valorInvalido(new MontoInvalidoException("El monto no puede ser negativo: -1"));
		ResponseEntity<ErrorResponse> porcentaje = manejador
				.valorInvalido(new PorcentajeInvalidoException("El porcentaje debe estar entre 0 y 100: 101"));

		assertThat(monto.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(monto.getBody().mensaje()).isEqualTo("El monto no puede ser negativo: -1");
		assertThat(porcentaje.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

}
