package com.crearcode.leads.infraestructura.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.aplicacion.ProyectoNoEncontradoException;
import com.crearcode.leads.dominio.ConsultarProyectosUseCase;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ResponderEntregableUseCase;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.AjustesRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ProyectoResponse;

/**
 * La vista del cliente (HU-49 a HU-51, HU-57). El correo NUNCA llega en
 * la petición: sale del token autenticado, así que nadie puede ver ni
 * responder el proyecto de otro cambiando un parámetro. Uno ajeno
 * responde 404, no 403: no se revela ni siquiera que existe.
 */
@RestController
@RequestMapping("/api/mis-proyectos")
class MisProyectosController {

	private final ConsultarProyectosUseCase consultar;
	private final ResponderEntregableUseCase responder;

	MisProyectosController(ConsultarProyectosUseCase consultar, ResponderEntregableUseCase responder) {
		this.consultar = consultar;
		this.responder = responder;
	}

	@GetMapping
	List<ProyectoResponse> listar(Authentication autenticacion) {
		return consultar.listarDe(correoDe(autenticacion)).stream()
				.map(proyecto -> ProyectoResponse.desde(proyecto, null))
				.toList();
	}

	@GetMapping("/{id}")
	ProyectoResponse obtener(@PathVariable UUID id, Authentication autenticacion) {
		ProyectoId proyectoId = new ProyectoId(id);
		return consultar.obtenerDe(proyectoId, correoDe(autenticacion))
				.map(proyecto -> ProyectoResponse.desde(proyecto, null))
				.orElseThrow(() -> new ProyectoNoEncontradoException(proyectoId));
	}

	@PostMapping("/{id}/entregables/{entregable}/aprobacion")
	ProyectoResponse aprobar(@PathVariable UUID id, @PathVariable UUID entregable, Authentication autenticacion) {
		return ProyectoResponse.desde(
				responder.aprobar(new ProyectoId(id), new EntregableId(entregable), correoDe(autenticacion)), null);
	}

	@PostMapping("/{id}/entregables/{entregable}/ajustes")
	ProyectoResponse pedirAjustes(@PathVariable UUID id, @PathVariable UUID entregable,
			@RequestBody AjustesRequest request, Authentication autenticacion) {
		return ProyectoResponse.desde(responder.pedirAjustes(new ProyectoId(id), new EntregableId(entregable),
				correoDe(autenticacion), request.nota()), null);
	}

	private static Correo correoDe(Authentication autenticacion) {
		return new Correo(autenticacion.getName());
	}

}
