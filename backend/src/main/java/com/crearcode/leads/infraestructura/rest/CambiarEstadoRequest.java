package com.crearcode.leads.infraestructura.rest;

import com.crearcode.leads.dominio.EstadoSolicitud;

import jakarta.validation.constraints.NotNull;

record CambiarEstadoRequest(@NotNull(message = "El nuevo estado es obligatorio") EstadoSolicitud nuevoEstado) {

}
