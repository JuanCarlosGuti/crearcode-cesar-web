package com.crearcode.leads.infraestructura.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.aplicacion.CotizacionNoEncontradaException;
import com.crearcode.leads.dominio.ConsultarCotizacionesUseCase;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.GeneradorDeDocumento;
import com.crearcode.leads.dominio.ResponderCotizacionUseCase;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.CotizacionResponse;

/**
 * La vista del cliente (HU-46). El correo NUNCA llega en la petición:
 * sale del token autenticado, así que nadie puede pedir las cotizaciones
 * de otro cambiando un parámetro. Una cotización ajena responde 404, no
 * 403: no se revela ni siquiera que existe (invariante 6).
 */
@RestController
@RequestMapping("/api/mis-cotizaciones")
class MisCotizacionesController {

	private final ConsultarCotizacionesUseCase consultar;
	private final ResponderCotizacionUseCase responder;
	private final GeneradorDeDocumento generadorDeDocumento;

	MisCotizacionesController(ConsultarCotizacionesUseCase consultar, ResponderCotizacionUseCase responder,
			GeneradorDeDocumento generadorDeDocumento) {
		this.consultar = consultar;
		this.responder = responder;
		this.generadorDeDocumento = generadorDeDocumento;
	}

	@GetMapping
	List<CotizacionResponse> listar(Authentication autenticacion) {
		return consultar.listarDe(correoDe(autenticacion)).stream()
				.map(CotizacionResponse::desde)
				.toList();
	}

	@GetMapping("/{id}")
	CotizacionResponse obtener(@PathVariable UUID id, Authentication autenticacion) {
		return CotizacionResponse.desde(buscarPropia(id, autenticacion));
	}

	@PostMapping("/{id}/aceptacion")
	CotizacionResponse aceptar(@PathVariable UUID id, Authentication autenticacion) {
		return CotizacionResponse.desde(
				responder.aceptar(new CotizacionId(id), correoDe(autenticacion)));
	}

	@PostMapping("/{id}/rechazo")
	CotizacionResponse rechazar(@PathVariable UUID id, Authentication autenticacion) {
		return CotizacionResponse.desde(
				responder.rechazar(new CotizacionId(id), correoDe(autenticacion)));
	}

	@GetMapping("/{id}/documento")
	ResponseEntity<byte[]> descargar(@PathVariable UUID id, Authentication autenticacion) {
		Cotizacion cotizacion = buscarPropia(id, autenticacion);
		return CotizacionController.respuestaConPdf(cotizacion, generadorDeDocumento.generar(cotizacion));
	}

	private Cotizacion buscarPropia(UUID id, Authentication autenticacion) {
		CotizacionId cotizacionId = new CotizacionId(id);
		return consultar.obtenerDe(cotizacionId, correoDe(autenticacion))
				.orElseThrow(() -> new CotizacionNoEncontradaException(cotizacionId));
	}

	private static Correo correoDe(Authentication autenticacion) {
		return new Correo(autenticacion.getName());
	}

}
