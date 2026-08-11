package com.crearcode.leads.infraestructura.notificacion;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.EnviadorDeCotizaciones;
import com.crearcode.leads.dominio.GeneradorDeDocumento;

import jakarta.mail.internet.MimeMessage;

/**
 * Le hace llegar la cotización al cliente con el PDF adjunto (HU-45).
 * A diferencia del resto de correos del sitio, este necesita
 * {@link MimeMessageHelper} en vez de {@code SimpleMailMessage}: es el
 * primero que lleva adjunto.
 *
 * <p>Como el resto de correos, el adaptador es quien construye el
 * enlace a la cuenta — la aplicación no conoce rutas del frontend.
 */
@Component
class EnviadorDeCotizacionesAdapter implements EnviadorDeCotizaciones {

	private final JavaMailSender mailSender;
	private final GeneradorDeDocumento generadorDeDocumento;
	private final String frontendUrl;

	EnviadorDeCotizacionesAdapter(JavaMailSender mailSender, GeneradorDeDocumento generadorDeDocumento,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.mailSender = mailSender;
		this.generadorDeDocumento = generadorDeDocumento;
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void enviar(Cotizacion cotizacion) {
		String numero = cotizacion.numero().valor();
		byte[] pdf = generadorDeDocumento.generar(cotizacion);

		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
			helper.setTo(cotizacion.cliente().correo().valor());
			helper.setSubject("Tu cotización " + numero + " · Crear Code Cesar");
			helper.setText("""
					Hola,

					Adjuntamos la cotización %s para %s.

					También puedes verla y responderla (aceptar o rechazar) desde tu
					cuenta:

					%s/mi-cuenta/cotizaciones

					La cotización es válida hasta la fecha indicada en el documento.
					Si tienes dudas, respóndenos este correo y lo hablamos.

					— Crear Code Cesar S.A.S. · Valledupar, Colombia
					""".formatted(numero, cotizacion.cliente().nombre(), frontendUrl));
			helper.addAttachment(numero + ".pdf", new ByteArrayResource(pdf), "application/pdf");

			mailSender.send(mensaje);
		} catch (Exception fallo) {
			// El caso de uso lo trata como best-effort: la cotización queda
			// enviada igual y el PDF se puede compartir a mano.
			throw new IllegalStateException("No se pudo enviar el correo de la cotización " + numero, fallo);
		}
	}

}
