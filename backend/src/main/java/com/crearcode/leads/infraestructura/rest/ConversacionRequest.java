package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

record ConversacionRequest(
		@NotEmpty(message = "La conversación no puede estar vacía") @Valid List<MensajeRequest> mensajes) {

	record MensajeRequest(
			@NotBlank(message = "El rol del mensaje es obligatorio") String rol,
			@NotBlank(message = "El texto del mensaje es obligatorio") String texto) {
	}

}
