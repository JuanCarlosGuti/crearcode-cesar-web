package com.crearcode.leads.infraestructura.notificacion;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

/**
 * Envío por SMTP. Es el transporte de desarrollo local (Mailpit) y el
 * que ejercitan los tests con GreenMail. En Render **no sirve** en el
 * plan gratuito: los puertos SMTP salientes están bloqueados.
 */
class TransporteSmtp implements TransporteDeCorreo {

	private final JavaMailSender mailSender;
	private final RemitenteDeCorreo remitente;

	TransporteSmtp(JavaMailSender mailSender, RemitenteDeCorreo remitente) {
		this.mailSender = mailSender;
		this.remitente = remitente;
	}

	@Override
	public void enviar(CorreoSaliente correo) {
		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, correo.tieneAdjunto(),
					StandardCharsets.UTF_8.name());
			helper.setFrom(remitente.remitente());
			helper.setReplyTo(remitente.responderA());
			helper.setTo(correo.para());
			helper.setSubject(correo.asunto());
			helper.setText(correo.cuerpo());

			if (correo.tieneAdjunto()) {
				helper.addAttachment(correo.adjunto().nombre(),
						new ByteArrayResource(correo.adjunto().contenido()), "application/pdf");
			}

			mailSender.send(mensaje);
		} catch (Exception fallo) {
			throw new EnvioDeCorreoFallidoException("No se pudo enviar el correo por SMTP", fallo);
		}
	}

}
