package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoRepositorio;

/**
 * Cada método lleva su propia transacción: las colecciones del proyecto
 * son perezosas y el mapper las recorre. Dentro de la transacción de un
 * caso de uso, simplemente se une a ella.
 */
@Component
class ProyectoRepositorioJpaAdapter implements ProyectoRepositorio {

	private final ProyectoJpaRepository jpaRepository;

	ProyectoRepositorioJpaAdapter(ProyectoJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	/**
	 * La fila del proyecto se actualiza en su sitio —no se borra y se
	 * vuelve a crear—, así el bloqueo de {@link #buscarPorIdParaModificar}
	 * sigue valiendo para quien espera. Fases, entregables y pagos se
	 * reescriben: el agregado manda, y orphanRemoval limpia los que ya no
	 * están.
	 */
	@Override
	@Transactional
	public void guardar(Proyecto proyecto) {
		jpaRepository.findById(proyecto.id().valor()).ifPresent(existente -> {
			existente.getFases().clear();
			existente.getPagos().clear();
			jpaRepository.saveAndFlush(existente);
		});
		jpaRepository.save(ProyectoMapper.aEntidad(proyecto));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Proyecto> buscarPorId(ProyectoId id) {
		return jpaRepository.findById(id.valor()).map(ProyectoMapper::aDominio);
	}

	@Override
	@Transactional
	public Optional<Proyecto> buscarPorIdParaModificar(ProyectoId id) {
		return jpaRepository.findByIdParaModificar(id.valor()).map(ProyectoMapper::aDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Proyecto> listar() {
		return jpaRepository.findAllByOrderByCreadoEnDesc().stream().map(ProyectoMapper::aDominio).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Proyecto> listarPorCorreoDelCliente(Correo correo) {
		return jpaRepository.findByClienteCorreo(correo.valor()).stream().map(ProyectoMapper::aDominio).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Proyecto> buscarPorCotizacion(CotizacionId cotizacion) {
		return jpaRepository.findByOrigenCotizacionId(cotizacion.valor()).map(ProyectoMapper::aDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Proyecto> listarPorEstado(EstadoProyecto estado) {
		return jpaRepository.findByEstado(estado).stream().map(ProyectoMapper::aDominio).toList();
	}

}
