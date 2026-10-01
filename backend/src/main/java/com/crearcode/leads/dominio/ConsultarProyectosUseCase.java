package com.crearcode.leads.dominio;

import java.util.List;
import java.util.Optional;

public interface ConsultarProyectosUseCase {

	/** Vista del equipo: todos. */
	List<Proyecto> listar();

	Optional<Proyecto> obtener(ProyectoId id);

	/** Vista del cliente: solo los de su correo (invariante 7). */
	List<Proyecto> listarDe(Correo correoDelCliente);

	/**
	 * Obtiene un proyecto comprobando que sea del cliente. Si no lo es,
	 * se comporta como si no existiera: no revela proyectos ajenos.
	 */
	Optional<Proyecto> obtenerDe(ProyectoId id, Correo correoDelCliente);

	Optional<Proyecto> deLaCotizacion(CotizacionId cotizacion);

	/**
	 * HU-53: el panel avisa si el correo del cliente todavía no tiene
	 * cuenta — el proyecto existe igual y lo verá cuando se registre.
	 */
	boolean clienteTieneCuenta(Correo correoDelCliente);

}
