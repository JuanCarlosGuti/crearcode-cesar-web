package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;

import com.crearcode.leads.dominio.Rol;

record LoginResponse(String token, Instant expiraEn, Rol rol, String correo) {

}
