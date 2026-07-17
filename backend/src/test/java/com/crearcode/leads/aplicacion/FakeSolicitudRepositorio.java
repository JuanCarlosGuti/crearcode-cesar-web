package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.SolicitudRepositorio;

/** Fake en memoria de {@link SolicitudRepositorio} para tests de casos de uso. */
class FakeSolicitudRepositorio implements SolicitudRepositorio {

	private final List<SolicitudDeContacto> solicitudes = new ArrayList<>();

	@Override
	public void guardar(SolicitudDeContacto solicitud) {
		solicitudes.removeIf(existente -> existente.id().equals(solicitud.id()));
		solicitudes.add(solicitud);
	}

	@Override
	public Optional<SolicitudDeContacto> buscarPorId(SolicitudId id) {
		return solicitudes.stream().filter(s -> s.id().equals(id)).findFirst();
	}

	@Override
	public List<SolicitudDeContacto> listar() {
		return List.copyOf(solicitudes);
	}

	@Override
	public List<SolicitudDeContacto> listarPorEstado(EstadoSolicitud estado) {
		return solicitudes.stream().filter(s -> s.estado() == estado).toList();
	}

}
