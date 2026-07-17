package com.crearcode.leads.aplicacion;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ListarSolicitudesUseCase;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudRepositorio;

@Service
class ListarSolicitudesUseCaseImpl implements ListarSolicitudesUseCase {

	private final SolicitudRepositorio repositorio;

	ListarSolicitudesUseCaseImpl(SolicitudRepositorio repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional(readOnly = true)
	public List<SolicitudDeContacto> listar() {
		return repositorio.listar();
	}

	@Override
	@Transactional(readOnly = true)
	public List<SolicitudDeContacto> listarPorEstado(EstadoSolicitud estado) {
		return repositorio.listarPorEstado(estado);
	}

}
