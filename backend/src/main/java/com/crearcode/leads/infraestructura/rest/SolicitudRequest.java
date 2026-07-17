package com.crearcode.leads.infraestructura.rest;

import com.crearcode.leads.dominio.ServicioDeInteres;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudRequest(
		@NotBlank(message = "El nombre es obligatorio") String nombre,
		String empresa,
		@NotBlank(message = "El correo es obligatorio") String correo,
		@NotBlank(message = "El teléfono es obligatorio") String telefono,
		@NotNull(message = "El servicio de interés es obligatorio") ServicioDeInteres servicioDeInteres,
		@NotBlank(message = "El mensaje es obligatorio") String mensaje,
		boolean aceptaConsentimiento) {

}
