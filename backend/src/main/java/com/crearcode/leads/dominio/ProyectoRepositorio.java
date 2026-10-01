package com.crearcode.leads.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia de proyectos (fase F12).
 */
public interface ProyectoRepositorio {

	void guardar(Proyecto proyecto);

	Optional<Proyecto> buscarPorId(ProyectoId id);

	/**
	 * Como {@link #buscarPorId}, pero bloqueando el proyecto hasta que
	 * termine la transacción. Lo usan los casos de uso que lo modifican:
	 * si el cliente aprueba mientras el equipo registra un pago, el
	 * segundo espera al primero en vez de pisar su cambio al guardar.
	 */
	Optional<Proyecto> buscarPorIdParaModificar(ProyectoId id);

	List<Proyecto> listar();

	/**
	 * Los proyectos de un correo — la vista del cliente. Sin distinguir
	 * mayúsculas, igual que {@link Proyecto#perteneceA}.
	 */
	List<Proyecto> listarPorCorreoDelCliente(Correo correo);

	/** Invariante 6: a lo sumo un proyecto por cotización. */
	Optional<Proyecto> buscarPorCotizacion(CotizacionId cotizacion);

	List<Proyecto> listarPorEstado(EstadoProyecto estado);

}
