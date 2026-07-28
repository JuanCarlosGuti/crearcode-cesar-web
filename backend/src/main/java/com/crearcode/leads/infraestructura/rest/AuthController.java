package com.crearcode.leads.infraestructura.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.dominio.AutenticarUsuarioUseCase;
import com.crearcode.leads.dominio.ContrasenaPlana;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.RegistrarClienteUseCase;
import com.crearcode.leads.dominio.ReenviarVerificacionUseCase;
import com.crearcode.leads.dominio.RestablecerContrasenaUseCase;
import com.crearcode.leads.dominio.SesionAutenticada;
import com.crearcode.leads.dominio.SolicitarRecuperacionUseCase;
import com.crearcode.leads.dominio.VerificarCorreoUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
	private final RegistrarClienteUseCase registrarClienteUseCase;
	private final VerificarCorreoUseCase verificarCorreoUseCase;
	private final ReenviarVerificacionUseCase reenviarVerificacionUseCase;
	private final SolicitarRecuperacionUseCase solicitarRecuperacionUseCase;
	private final RestablecerContrasenaUseCase restablecerContrasenaUseCase;

	AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase, RegistrarClienteUseCase registrarClienteUseCase,
			VerificarCorreoUseCase verificarCorreoUseCase, ReenviarVerificacionUseCase reenviarVerificacionUseCase,
			SolicitarRecuperacionUseCase solicitarRecuperacionUseCase,
			RestablecerContrasenaUseCase restablecerContrasenaUseCase) {
		this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
		this.registrarClienteUseCase = registrarClienteUseCase;
		this.verificarCorreoUseCase = verificarCorreoUseCase;
		this.reenviarVerificacionUseCase = reenviarVerificacionUseCase;
		this.solicitarRecuperacionUseCase = solicitarRecuperacionUseCase;
		this.restablecerContrasenaUseCase = restablecerContrasenaUseCase;
	}

	@PostMapping("/login")
	ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		SesionAutenticada sesion = autenticarUsuarioUseCase.autenticar(request.correo(), request.contrasena());
		return ResponseEntity
				.ok(new LoginResponse(sesion.token(), sesion.expiraEn(), sesion.rol(), sesion.correo().valor()));
	}

	@PostMapping("/registro")
	ResponseEntity<Void> registrar(@Valid @RequestBody RegistroRequest request) {
		registrarClienteUseCase.registrar(new Correo(request.correo()), new ContrasenaPlana(request.contrasena()));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/verificacion")
	ResponseEntity<Void> verificar(@Valid @RequestBody VerificacionRequest request) {
		verificarCorreoUseCase.verificar(request.token());
		return ResponseEntity.noContent().build();
	}

	/**
	 * 202 incondicional: el caso de uso es silencioso a propósito
	 * (invariantes 6 y 7 del contexto usuarios) — la respuesta nunca
	 * revela si el correo existe.
	 */
	@PostMapping("/reenvio-verificacion")
	ResponseEntity<Void> reenviarVerificacion(@Valid @RequestBody CorreoRequest request) {
		reenviarVerificacionUseCase.reenviar(request.correo());
		return ResponseEntity.accepted().build();
	}

	/** 202 incondicional, mismo motivo que el reenvío de verificación. */
	@PostMapping("/recuperacion")
	ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody CorreoRequest request) {
		solicitarRecuperacionUseCase.solicitar(request.correo());
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/restablecimiento")
	ResponseEntity<Void> restablecer(@Valid @RequestBody RestablecimientoRequest request) {
		restablecerContrasenaUseCase.restablecer(request.token(), new ContrasenaPlana(request.contrasena()));
		return ResponseEntity.noContent().build();
	}

}
