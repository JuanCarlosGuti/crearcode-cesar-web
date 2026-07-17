package com.crearcode.leads.infraestructura.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.dominio.AutenticarUsuarioUseCase;
import com.crearcode.leads.dominio.SesionAutenticada;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

	AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
		this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
	}

	@PostMapping("/login")
	ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		SesionAutenticada sesion = autenticarUsuarioUseCase.autenticar(request.correo(), request.contrasena());
		return ResponseEntity.ok(new LoginResponse(sesion.token(), sesion.expiraEn()));
	}

}
