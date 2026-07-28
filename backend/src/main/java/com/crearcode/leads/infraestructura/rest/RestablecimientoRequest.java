package com.crearcode.leads.infraestructura.rest;

import jakarta.validation.constraints.NotBlank;

record RestablecimientoRequest(
		@NotBlank(message = "El token es obligatorio") String token,
		@NotBlank(message = "La contraseña es obligatoria") String contrasena) {

}
