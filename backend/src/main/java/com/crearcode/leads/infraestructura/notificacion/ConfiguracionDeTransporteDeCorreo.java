package com.crearcode.leads.infraestructura.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Único punto donde se decide cómo salen los correos, con la variable
 * {@code MAIL_TRANSPORTE}:
 *
 * <ul>
 * <li>{@code smtp} (default) — desarrollo local contra Mailpit y tests
 * con GreenMail.</li>
 * <li>{@code resend} — producción, por API HTTP. Render bloquea los
 * puertos SMTP salientes en el plan gratuito, así que por SMTP el envío
 * se queda colgado hasta el timeout y el correo nunca llega.</li>
 * </ul>
 */
@Configuration
class ConfiguracionDeTransporteDeCorreo {

	@Bean
	TransporteDeCorreo transporteDeCorreo(
			@Value("${app.correo.transporte}") String transporte,
			@Value("${app.correo.resend.url}") String resendUrl,
			@Value("${app.correo.resend.api-key}") String resendApiKey,
			@Value("${app.correo.resend.timeout-segundos}") long resendTimeout,
			RemitenteDeCorreo remitente,
			JavaMailSender mailSender) {

		return "resend".equalsIgnoreCase(transporte)
				? new TransporteResend(resendUrl, resendApiKey, remitente, resendTimeout)
				: new TransporteSmtp(mailSender, remitente);
	}

}
