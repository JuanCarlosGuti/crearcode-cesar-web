package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;

record LoginResponse(String token, Instant expiraEn) {

}
