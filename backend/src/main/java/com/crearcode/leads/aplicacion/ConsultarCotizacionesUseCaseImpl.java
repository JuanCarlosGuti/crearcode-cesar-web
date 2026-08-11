package com.crearcode.leads.aplicacion;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.ConsultarCotizacionesUseCase;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.EstadoCotizacion;

@Service
class ConsultarCotizacionesUseCaseImpl implements ConsultarCotizacionesUseCase {

	private final CotizacionRepositorio cotizaciones;

	ConsultarCotizacionesUseCaseImpl(CotizacionRepositorio cotizaciones) {
		this.cotizaciones = cotizaciones;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Cotizacion> listar(EstadoCotizacion estado) {
		return estado == null ? cotizaciones.listar() : cotizaciones.listarPorEstado(estado);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Cotizacion> listarDe(Correo correoDelCliente) {
		return cotizaciones.listarPorCorreoDelCliente(correoDelCliente);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Cotizacion> obtener(CotizacionId id) {
		return cotizaciones.buscarPorId(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Cotizacion> obtenerDe(CotizacionId id, Correo correoDelCliente) {
		return cotizaciones.buscarPorId(id).filter(cotizacion -> esDe(cotizacion, correoDelCliente));
	}

	static boolean esDe(Cotizacion cotizacion, Correo correo) {
		return cotizacion.cliente().correo().valor().equalsIgnoreCase(correo.valor());
	}

}
