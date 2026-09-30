package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;
import java.util.UUID;

import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;

record SolicitudResponse(
		UUID id,
		String nombre,
		String empresa,
		String correo,
		String telefono,
		ServicioDeInteres servicioDeInteres,
		String mensaje,
		EstadoSolicitud estado,
		/**
		 * Si el lead autorizó recibir comunicaciones comerciales. Se
		 * expone porque sin verlo el dato no sirve de nada: la razón de
		 * guardarlo aparte es saber a quién se le puede escribir sin
		 * revisar correos viejos (política v2, §13).
		 */
		boolean aceptaComunicacionesComerciales,
		Instant fechaCreacion,
		Instant fechaUltimaActualizacion) {

	static SolicitudResponse desde(SolicitudDeContacto solicitud) {
		DatosDeContacto datos = solicitud.datosDeContacto();
		return new SolicitudResponse(
				solicitud.id().valor(),
				datos.nombre(),
				datos.empresa(),
				datos.correo().valor(),
				datos.telefono().valor(),
				solicitud.servicioDeInteres(),
				solicitud.mensaje(),
				solicitud.estado(),
				solicitud.consentimiento().comunicacionesComerciales(),
				solicitud.fechaCreacion(),
				solicitud.fechaUltimaActualizacion());
	}

}
