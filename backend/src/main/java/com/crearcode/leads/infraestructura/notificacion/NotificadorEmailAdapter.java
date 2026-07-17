package com.crearcode.leads.infraestructura.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.NotificadorPort;
import com.crearcode.leads.dominio.SolicitudDeContacto;

@Component
class NotificadorEmailAdapter implements NotificadorPort {

	private final JavaMailSender mailSender;
	private final String correoDestino;

	NotificadorEmailAdapter(JavaMailSender mailSender,
			@Value("${app.notificaciones.correo-destino}") String correoDestino) {
		this.mailSender = mailSender;
		this.correoDestino = correoDestino;
	}

	@Override
	public void notificarNuevaSolicitud(SolicitudDeContacto solicitud) {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(correoDestino);
		mensaje.setSubject("Nueva solicitud de contacto - " + solicitud.servicioDeInteres());
		mensaje.setText(construirCuerpo(solicitud));
		mailSender.send(mensaje);
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
