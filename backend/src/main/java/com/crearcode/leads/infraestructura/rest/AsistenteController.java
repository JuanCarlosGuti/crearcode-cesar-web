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
import com.crearcode.leads.dominio.GenerarDemoDeDisenoUseCase;
import com.crearcode.leads.dominio.GenerarDiagnosticoUseCase;
import com.crearcode.leads.dominio.SolicitudDeDemo;
import com.crearcode.leads.dominio.MensajeDeChatInvalidoException;
import com.crearcode.leads.dominio.NegocioSimulado;
import com.crearcode.leads.dominio.ParDeDiagnostico;
import com.crearcode.leads.dominio.RespuestasDeDiagnostico;
import com.crearcode.leads.dominio.ResponderAlVisitanteUseCase;
import com.crearcode.leads.dominio.SimularChatbotUseCase;
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
	private final SimularChatbotUseCase simularChatbot;
	private final GenerarDiagnosticoUseCase generarDiagnostico;
	private final GenerarDemoDeDisenoUseCase generarDemo;

	AsistenteController(ResponderAlVisitanteUseCase responderAlVisitante, SimularChatbotUseCase simularChatbot,
			GenerarDiagnosticoUseCase generarDiagnostico, GenerarDemoDeDisenoUseCase generarDemo) {
		this.responderAlVisitante = responderAlVisitante;
		this.simularChatbot = simularChatbot;
		this.generarDiagnostico = generarDiagnostico;
		this.generarDemo = generarDemo;
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

	@PostMapping("/simulador")
	ResponseEntity<RespuestaAsistenteResponse> simular(
			@Valid @RequestBody SimuladorRequest request,
			@RequestHeader(value = "X-Sesion-Anonima", required = false) String idSesionAnonima,
			Authentication autenticacion) {
		NegocioSimulado negocio = new NegocioSimulado(request.negocio().nombre(), request.negocio().rubro());
		ConversacionDeAsistente conversacion = new ConversacionDeAsistente(request.mensajes().stream()
				.map(mensaje -> new MensajeDeChat(rolDesde(mensaje.rol()), mensaje.texto()))
				.toList());
		IdentidadDelVisitante identidad = autenticacion != null && autenticacion.isAuthenticated()
				? IdentidadDelVisitante.registrada(autenticacion.getName())
				: IdentidadDelVisitante.anonima(idSesionAnonima);

		RespuestaDelAsistente respuesta = simularChatbot.simular(negocio, conversacion, identidad);
		return ResponseEntity.ok(new RespuestaAsistenteResponse(respuesta.texto(), respuesta.escalarAHumano()));
	}

	@PostMapping("/diagnostico")
	ResponseEntity<InformeDiagnosticoResponse> diagnosticar(
			@Valid @RequestBody DiagnosticoRequest request,
			@RequestHeader(value = "X-Sesion-Anonima", required = false) String idSesionAnonima,
			Authentication autenticacion) {
		RespuestasDeDiagnostico respuestas = new RespuestasDeDiagnostico(request.respuestas().stream()
				.map(par -> new ParDeDiagnostico(par.pregunta(), par.respuesta()))
				.toList());
		IdentidadDelVisitante identidad = autenticacion != null && autenticacion.isAuthenticated()
				? IdentidadDelVisitante.registrada(autenticacion.getName())
				: IdentidadDelVisitante.anonima(idSesionAnonima);

		return ResponseEntity.ok(InformeDiagnosticoResponse.desde(generarDiagnostico.generar(respuestas, identidad)));
	}

	/**
	 * SOLO registrados (HU-42): la ruta NO está en permitAll, así que
	 * Spring Security exige el Bearer antes de llegar aquí; el caso de
	 * uso vuelve a validar la identidad (defensa en profundidad).
	 */
	@PostMapping("/demo-diseno")
	ResponseEntity<BocetoDemoResponse> demoDiseno(
			@Valid @RequestBody DemoDisenoRequest request,
			Authentication autenticacion) {
		SolicitudDeDemo solicitud = new SolicitudDeDemo(request.sector(), request.queHace(),
				request.queNecesita());
		IdentidadDelVisitante identidad = IdentidadDelVisitante.registrada(autenticacion.getName());

		return ResponseEntity.ok(BocetoDemoResponse.desde(generarDemo.generar(solicitud, identidad)));
	}

	private static RolDeMensaje rolDesde(String rol) {
		try {
			return RolDeMensaje.valueOf(rol);
		} catch (IllegalArgumentException rolDesconocido) {
			throw new MensajeDeChatInvalidoException("Rol de mensaje desconocido");
		}
	}

}
