package com.crearcode.leads.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia de cotizaciones (fase F11).
 */
public interface CotizacionRepositorio {

	void guardar(Cotizacion cotizacion);

	Optional<Cotizacion> buscarPorId(CotizacionId id);

	List<Cotizacion> listar();

	List<Cotizacion> listarPorEstado(EstadoCotizacion estado);

	/** Las cotizaciones dirigidas a un correo — la vista del cliente. */
	List<Cotizacion> listarPorCorreoDelCliente(Correo correo);

}
