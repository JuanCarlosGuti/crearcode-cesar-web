package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.SolicitudId;

/** Se lanza cuando un caso de uso busca una solicitud por id y no la encuentra. */
public class SolicitudNoEncontradaException extends RuntimeException {

	public SolicitudNoEncontradaException(SolicitudId id) {
		super("No existe una solicitud con id " + id.valor());
	}

}
