package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.EstadoCotizacion;

/** Fake en memoria de {@link CotizacionRepositorio} para tests de casos de uso. */
class FakeCotizacionRepositorio implements CotizacionRepositorio {

	private final List<Cotizacion> cotizaciones = new ArrayList<>();

	@Override
	public void guardar(Cotizacion cotizacion) {
		cotizaciones.removeIf(existente -> existente.id().equals(cotizacion.id()));
		cotizaciones.add(cotizacion);
	}

	@Override
	public Optional<Cotizacion> buscarPorId(CotizacionId id) {
		return cotizaciones.stream().filter(c -> c.id().equals(id)).findFirst();
	}

	@Override
	public List<Cotizacion> listar() {
		return List.copyOf(cotizaciones);
	}

	@Override
	public List<Cotizacion> listarPorEstado(EstadoCotizacion estado) {
		return cotizaciones.stream().filter(c -> c.estado() == estado).toList();
	}

	@Override
	public List<Cotizacion> listarPorCorreoDelCliente(Correo correo) {
		return cotizaciones.stream()
				.filter(c -> c.cliente().correo().valor().equalsIgnoreCase(correo.valor()))
				.toList();
	}

	int cuantasHay() {
		return cotizaciones.size();
	}

}
