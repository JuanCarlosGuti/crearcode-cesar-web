package com.crearcode.leads.infraestructura.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.NotificadorPort;
import com.crearcode.leads.dominio.SolicitudDeContacto;

@Component
class NotificadorEmailAdapter implements NotificadorPort {

	private final TransporteDeCorreo transporte;
	private final String correoDestino;

	NotificadorEmailAdapter(TransporteDeCorreo transporte,
			@Value("${app.notificaciones.correo-destino}") String correoDestino) {
		this.transporte = transporte;
		this.correoDestino = correoDestino;
	}

	@Override
	public void notificarNuevaSolicitud(SolicitudDeContacto solicitud) {
		transporte.enviar(CorreoSaliente.simple(correoDestino,
				"Nueva solicitud de contacto - " + solicitud.servicioDeInteres(),
				construirCuerpo(solicitud)));
	}

	private String construirCuerpo(SolicitudDeContacto solicitud) {
		DatosDeContacto datos = solicitud.datosDeContacto();
		return """
				Nueva solicitud de contacto recibida desde el sitio web.

				Nombre: %s
				Empresa: %s
				Correo: %s
				Teléfono: %s
				Servicio de interés: %s

				Mensaje:
				%s
				"""
				.formatted(
						datos.nombre(),
						datos.empresa() == null ? "-" : datos.empresa(),
						datos.correo().valor(),
						datos.telefono().valor(),
						solicitud.servicioDeInteres(),
						solicitud.mensaje());
	}

}
