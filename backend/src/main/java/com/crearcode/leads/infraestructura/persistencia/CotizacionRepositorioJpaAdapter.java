package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.EstadoCotizacion;

@Component
class CotizacionRepositorioJpaAdapter implements CotizacionRepositorio {

	private final CotizacionJpaRepository jpaRepository;

	CotizacionRepositorioJpaAdapter(CotizacionJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void guardar(Cotizacion cotizacion) {
		// Los ítems se regeneran en cada guardado (ids nuevos): el
		// agregado manda, y orphanRemoval limpia los anteriores.
		jpaRepository.findById(cotizacion.id().valor()).ifPresent(existente -> {
			existente.getItems().clear();
			jpaRepository.saveAndFlush(existente);
		});
		jpaRepository.save(CotizacionMapper.aEntidad(cotizacion));
	}

	@Override
	public Optional<Cotizacion> buscarPorId(CotizacionId id) {
		return jpaRepository.findById(id.valor()).map(CotizacionMapper::aDominio);
	}

	@Override
	public List<Cotizacion> listar() {
		return jpaRepository.findAll().stream().map(CotizacionMapper::aDominio).toList();
	}

	@Override
	public List<Cotizacion> listarPorEstado(EstadoCotizacion estado) {
		return jpaRepository.findByEstado(estado).stream().map(CotizacionMapper::aDominio).toList();
	}

	@Override
	public List<Cotizacion> listarPorCorreoDelCliente(Correo correo) {
		return jpaRepository.findByClienteCorreo(correo.valor()).stream()
				.map(CotizacionMapper::aDominio).toList();
	}

}
