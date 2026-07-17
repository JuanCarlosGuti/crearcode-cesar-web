package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.CambiarEstadoSolicitudUseCase;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.SolicitudRepositorio;

@Service
class CambiarEstadoSolicitudUseCaseImpl implements CambiarEstadoSolicitudUseCase {

	private final SolicitudRepositorio repositorio;
	private final Clock reloj;

	CambiarEstadoSolicitudUseCaseImpl(SolicitudRepositorio repositorio, Clock reloj) {
		this.repositorio = repositorio;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public void cambiarEstado(SolicitudId id, EstadoSolicitud nuevoEstado) {
		SolicitudDeContacto solicitud = repositorio.buscarPorId(id)
				.orElseThrow(() -> new SolicitudNoEncontradaException(id));

		solicitud.cambiarEstado(nuevoEstado, Instant.now(reloj));

		repositorio.guardar(solicitud);
	}

}
