package com.crearcode.leads.infraestructura;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ProgramadorDeGarantiasTest {

	/** Si revienta, la tarea no puede morir: mañana tiene que volver a correr. */
	@Test
	void unFalloNoTumbaLaTareaProgramada() {
		ProgramadorDeGarantias programador = new ProgramadorDeGarantias(() -> {
			throw new IllegalStateException("La base no responde");
		});

		assertThatCode(programador::cerrarLasVencidas).doesNotThrowAnyException();
	}

}
