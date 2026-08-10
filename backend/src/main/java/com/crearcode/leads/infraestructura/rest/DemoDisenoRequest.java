package com.crearcode.leads.infraestructura.rest;

import jakarta.validation.constraints.NotBlank;

record DemoDisenoRequest(
		@NotBlank(message = "El sector es obligatorio") String sector,
		@NotBlank(message = "Qué hace tu negocio es obligatorio") String queHace,
		@NotBlank(message = "Qué necesitas resolver es obligatorio") String queNecesita) {
}
