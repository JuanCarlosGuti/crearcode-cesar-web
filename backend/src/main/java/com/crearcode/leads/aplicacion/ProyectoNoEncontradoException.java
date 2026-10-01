package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.ProyectoId;

/**
 * El proyecto no existe, o no es del cliente que lo pide: desde fuera
 * las dos cosas se ven igual (404), para no revelar proyectos ajenos.
 */
public class ProyectoNoEncontradoException extends RuntimeException {

	public ProyectoNoEncontradoException(ProyectoId id) {
		super("No existe el proyecto " + id.valor());
	}

}
