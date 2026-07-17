package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.SolicitudRepositorio;

@Component
class SolicitudRepositorioJpaAdapter implements SolicitudRepositorio {

	private final SolicitudJpaRepository jpaRepository;

	SolicitudRepositorioJpaAdapter(SolicitudJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void guardar(SolicitudDeContacto solicitud) {
		jpaRepository.save(SolicitudMapper.aEntidad(solicitud));
	}

	@Override
	public Optional<SolicitudDeContacto> buscarPorId(SolicitudId id) {
		return jpaRepository.findById(id.valor()).map(SolicitudMapper::aDominio);
	}

	@Override
	public List<SolicitudDeContacto> listar() {
		return jpaRepository.findAll().stream().map(SolicitudMapper::aDominio).toList();
	}

	@Override
	public List<SolicitudDeContacto> listarPorEstado(EstadoSolicitud estado) {
		return jpaRepository.findByEstado(estado).stream().map(SolicitudMapper::aDominio).toList();
	}

}
