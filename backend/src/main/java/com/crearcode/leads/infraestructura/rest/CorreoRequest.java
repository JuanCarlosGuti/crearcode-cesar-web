package com.crearcode.leads.infraestructura.rest;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo común del reenvío de verificación y de la recuperación. */
record CorreoRequest(@NotBlank(message = "El correo es obligatorio") String correo) {

}
