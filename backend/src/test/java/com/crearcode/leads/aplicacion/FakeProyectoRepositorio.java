package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoRepositorio;

/** Fake en memoria de {@link ProyectoRepositorio} para tests de casos de uso. */
class FakeProyectoRepositorio implements ProyectoRepositorio {

	private final List<Proyecto> proyectos = new ArrayList<>();
	private int guardados;

	@Override
	public void guardar(Proyecto proyecto) {
		proyectos.removeIf(existente -> existente.id().equals(proyecto.id()));
		proyectos.add(proyecto);
		guardados++;
	}

	@Override
	public Optional<Proyecto> buscarPorId(ProyectoId id) {
		return proyectos.stream().filter(p -> p.id().equals(id)).findFirst();
	}

	@Override
	public Optional<Proyecto> buscarPorIdParaModificar(ProyectoId id) {
		return buscarPorId(id);
	}

	@Override
	public List<Proyecto> listar() {
		return List.copyOf(proyectos);
	}

	@Override
	public List<Proyecto> listarPorCorreoDelCliente(Correo correo) {
		return proyectos.stream().filter(p -> p.perteneceA(correo)).toList();
	}

	@Override
	public Optional<Proyecto> buscarPorCotizacion(CotizacionId cotizacion) {
		return proyectos.stream().filter(p -> cotizacion.equals(p.origen())).findFirst();
	}

	@Override
	public List<Proyecto> listarPorEstado(EstadoProyecto estado) {
		return proyectos.stream().filter(p -> p.estado() == estado).toList();
	}

	int vecesGuardado() {
		return guardados;
	}

}
