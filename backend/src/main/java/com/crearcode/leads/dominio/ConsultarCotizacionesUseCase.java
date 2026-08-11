package com.crearcode.leads.dominio;

import java.util.List;
import java.util.Optional;

public interface ConsultarCotizacionesUseCase {

	/** Vista del equipo: todas, o las de un estado. */
	List<Cotizacion> listar(EstadoCotizacion estado);

	/** Vista del cliente: solo las dirigidas a su correo (invariante 6). */
	List<Cotizacion> listarDe(Correo correoDelCliente);

	Optional<Cotizacion> obtener(CotizacionId id);

	/**
	 * Obtiene una cotización comprobando que sea del cliente indicado. Si
	 * no lo es, se comporta como si no existiera: no revela cotizaciones
	 * ajenas.
	 */
	Optional<Cotizacion> obtenerDe(CotizacionId id, Correo correoDelCliente);

}
