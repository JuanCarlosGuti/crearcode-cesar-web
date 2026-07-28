package com.crearcode.leads.infraestructura.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.MensajeDeChat;
import com.crearcode.leads.dominio.MensajeDeChatInvalidoException;
import com.crearcode.leads.dominio.ResponderAlVisitanteUseCase;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RolDeMensaje;

import jakarta.validation.Valid;

/**
 * Chat del asistente (F9). Público con Bearer opcional: si la petición
 * trae un token válido, la identidad es la del cliente registrado
 * (límite diario mayor, HU-38); sin token, la sesión anónima viene en
 * el header {@code X-Sesion-Anonima} generado por el navegador.
 */
@RestController
@RequestMapping("/api/asistente")
class AsistenteController {

	private final ResponderAlVisitanteUseCase responderAlVisitante;

	AsistenteController(ResponderAlVisitanteUseCase responderAlVisitante) {
		this.responderAlVisitante = responderAlVisitante;
	}

	@PostMapping("/mensajes")
	ResponseEntity<RespuestaAsistenteResponse> responder(
			@Valid @RequestBody ConversacionRequest request,
			@RequestHeader(value = "X-Sesion-Anonima", required = false) String idSesionAnonima,
			Authentication autenticacion) {
		ConversacionDeAsistente conversacion = new ConversacionDeAsistente(request.mensajes().stream()
				.map(mensaje -> new MensajeDeChat(rolDesde(mensaje.rol()), mensaje.texto()))
				.toList());
		IdentidadDelVisitante identidad = autenticacion != null && autenticacion.isAuthenticated()
				? IdentidadDelVisitante.registrada(autenticacion.getName())
				: IdentidadDelVisitante.anonima(idSesionAnonima);

		RespuestaDelAsistente respuesta = responderAlVisitante.responder(conversacion, identidad);
		return ResponseEntity.ok(new RespuestaAsistenteResponse(respuesta.texto(), respuesta.escalarAHumano()));
	}

	private static RolDeMensaje rolDesde(String rol) {
		try {
			return RolDeMensaje.valueOf(rol);
		} catch (IllegalArgumentException rolDesconocido) {
			throw new MensajeDeChatInvalidoException("Rol de mensaje desconocido");
		}
	}

}
