package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

record DiagnosticoRequest(
		@NotEmpty(message = "El cuestionario no puede estar vacío") @Valid List<ParRequest> respuestas) {

	record ParRequest(
			@NotBlank(message = "La pregunta es obligatoria") String pregunta,
			@NotBlank(message = "La respuesta es obligatoria") String respuesta) {
	}

}
