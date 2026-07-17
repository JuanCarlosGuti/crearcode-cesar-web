package com.crearcode.leads.infraestructura.rest;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.RegistrarSolicitudUseCase;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitudes")
class SolicitudController {

	private final RegistrarSolicitudUseCase registrarSolicitudUseCase;
	private final Clock reloj;
	private final String versionPoliticaVigente;

	SolicitudController(RegistrarSolicitudUseCase registrarSolicitudUseCase, Clock reloj,
			@Value("${app.legal.version-politica-datos}") String versionPoliticaVigente) {
		this.registrarSolicitudUseCase = registrarSolicitudUseCase;
		this.reloj = reloj;
		this.versionPoliticaVigente = versionPoliticaVigente;
	}

	@PostMapping
	ResponseEntity<SolicitudCreadaResponse> registrar(@Valid @RequestBody SolicitudRequest request) {
		DatosDeContacto datos = new DatosDeContacto(request.nombre(), request.empresa(),
				new Correo(request.correo()), new Telefono(request.telefono()));
		ConsentimientoDatos consentimiento = new ConsentimientoDatos(
				request.aceptaConsentimiento(), Instant.now(reloj), versionPoliticaVigente);

		SolicitudId id = registrarSolicitudUseCase.registrar(
				datos, request.servicioDeInteres(), request.mensaje(), consentimiento);

		return ResponseEntity.status(HttpStatus.CREATED).body(new SolicitudCreadaResponse(id.valor()));
	}

}
