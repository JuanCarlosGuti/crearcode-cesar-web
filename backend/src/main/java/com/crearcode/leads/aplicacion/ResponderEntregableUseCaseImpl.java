package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.ResponderEntregableUseCase;

/**
 * El cliente aprueba o pide ajustes desde su cuenta (HU-57, decisión
 * 27). El proyecto se busca ya filtrado por el correo del token: uno
 * ajeno responde igual que uno inexistente, y el equipo se entera por
 * correo de cada respuesta.
 */
@Service
class ResponderEntregableUseCaseImpl implements ResponderEntregableUseCase {

	private final ProyectoRepositorio proyectos;
	private final ApplicationEventPublisher eventos;
	private final Clock reloj;

	ResponderEntregableUseCaseImpl(ProyectoRepositorio proyectos, ApplicationEventPublisher eventos, Clock reloj) {
		this.proyectos = proyectos;
		this.eventos = eventos;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public Proyecto aprobar(ProyectoId id, EntregableId entregable, Correo cliente) {
		Proyecto proyecto = propio(id, cliente);
		proyecto.aprobarComoCliente(entregable, cliente, Instant.now(reloj));
		return guardarYAvisar(proyecto, entregable, true);
	}

	@Override
	@Transactional
	public Proyecto pedirAjustes(ProyectoId id, EntregableId entregable, Correo cliente, String nota) {
		Proyecto proyecto = propio(id, cliente);
		proyecto.pedirAjustesComoCliente(entregable, cliente, nota, Instant.now(reloj));
		return guardarYAvisar(proyecto, entregable, false);
	}

	private Proyecto propio(ProyectoId id, Correo cliente) {
		return proyectos.buscarPorIdParaModificar(id)
				.filter(proyecto -> proyecto.perteneceA(cliente))
				.orElseThrow(() -> new ProyectoNoEncontradoException(id));
	}

	private Proyecto guardarYAvisar(Proyecto proyecto, EntregableId entregable, boolean aprobado) {
		proyectos.guardar(proyecto);
		eventos.publishEvent(new EntregableRespondidoPorElCliente(proyecto, entregable, aprobado));
		return proyecto;
	}

}
