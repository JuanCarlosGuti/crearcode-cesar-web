package com.crearcode.leads.infraestructura.rest;

import jakarta.validation.constraints.NotBlank;

record VerificacionRequest(@NotBlank(message = "El token es obligatorio") String token) {

}
