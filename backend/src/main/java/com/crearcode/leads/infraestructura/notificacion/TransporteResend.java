package com.crearcode.leads.infraestructura.notificacion;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Envío por la API HTTP de Resend. Es el transporte de producción
 * porque Render bloquea los puertos SMTP salientes en el plan gratuito
 * (ver {@link TransporteDeCorreo}).
 *
 * <p>La API key viaja solo en el header Authorization y nunca se
 * loguea. El remitente debe pertenecer a un dominio verificado en
 * Resend o la API responde 422.
 */
class TransporteResend implements TransporteDeCorreo {

	private final RestClient clienteHttp;
	private final RemitenteDeCorreo remitente;

	TransporteResend(String urlBase, String apiKey, RemitenteDeCorreo remitente, long timeoutSegundos) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
		fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
		this.clienteHttp = RestClient.builder()
				.baseUrl(urlBase)
				.defaultHeader("Authorization", "Bearer " + apiKey)
				.requestFactory(fabrica)
				.build();
		this.remitente = remitente;
	}

	@Override
	public void enviar(CorreoSaliente correo) {
		try {
			clienteHttp.post()
					.uri("/emails")
					.contentType(MediaType.APPLICATION_JSON)
					.body(peticionDesde(correo))
					.retrieve()
					.toBodilessEntity();
		} catch (Exception fallo) {
			throw new EnvioDeCorreoFallidoException("No se pudo enviar el correo por Resend", fallo);
		}
	}

	private PeticionDeEnvio peticionDesde(CorreoSaliente correo) {
		List<AdjuntoResend> adjuntos = correo.tieneAdjunto()
				? List.of(new AdjuntoResend(correo.adjunto().nombre(),
						Base64.getEncoder().encodeToString(correo.adjunto().contenido())))
				: null;

		return new PeticionDeEnvio(remitente.remitente(), List.of(correo.para()), correo.asunto(),
				correo.cuerpo(), List.of(remitente.responderA()), adjuntos);
	}

	/** Nombres tal como los espera la API de Resend (snake_case en reply_to). */
	record PeticionDeEnvio(String from, List<String> to, String subject, String text,
			List<String> reply_to, List<AdjuntoResend> attachments) {
	}

	record AdjuntoResend(String filename, String content) {
	}

}
