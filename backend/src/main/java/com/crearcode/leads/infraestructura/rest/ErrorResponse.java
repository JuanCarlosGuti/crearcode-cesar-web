package com.crearcode.leads.infraestructura.rest;

/** Cuerpo de error uniforme de toda la API — reusado también por los manejadores de seguridad (401/403). */
public record ErrorResponse(String mensaje) {

}
