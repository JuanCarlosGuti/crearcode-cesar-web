package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record SimuladorRequest(
		@NotNull(message = "El negocio es obligatorio") @Valid NegocioRequest negocio,
		@NotEmpty(message = "La conversación no puede estar vacía") @Valid List<ConversacionRequest.MensajeRequest> mensajes) {

	record NegocioRequest(
			@NotBlank(message = "El nombre del negocio es obligatorio") String nombre,
			@NotBlank(message = "El rubro es obligatorio") String rubro) {
	}

}
