package com.crearcode.leads.infraestructura.rest;

import jakarta.validation.constraints.NotBlank;

record RegistroRequest(
		@NotBlank(message = "El correo es obligatorio") String correo,
		@NotBlank(message = "La contraseña es obligatoria") String contrasena) {

}
