package com.crearcode.leads.infraestructura.asistente;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.MensajeDeChat;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RolDeMensaje;

/**
 * Adaptador de Groq (API compatible con OpenAI, ADR-10). El prompt de
 * sistema se carga una sola vez desde {@code asistente-contexto.md};
 * la señal de escalamiento es el marcador {@code [ESCALAR]} al final
 * de la respuesta (se retira del texto y se expone como bandera).
 * La key viaja solo en el header Authorization — nunca se loguea.
 */
@Component
class GroqGeneradorDeRespuestasAdapter implements GeneradorDeRespuestas {

	private static final String MARCADOR_ESCALAMIENTO = "[ESCALAR]";

	private final RestClient clienteHttp;
	private final String modelo;
	private final String promptDeSistema;

	GroqGeneradorDeRespuestasAdapter(
			@Value("${app.asistente.groq.url}") String urlBase,
			@Value("${app.asistente.groq.key}") String apiKey,
			@Value("${app.asistente.groq.modelo}") String modelo,
			@Value("${app.asistente.groq.timeout-segundos}") long timeoutSegundos) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
		fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
		this.clienteHttp = RestClient.builder()
				.baseUrl(urlBase)
				.defaultHeader("Authorization", "Bearer " + apiKey)
				.requestFactory(fabrica)
				.build();
		this.modelo = modelo;
		this.promptDeSistema = cargarPromptDeSistema();
	}

	@Override
	public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
		try {
			RespuestaGroq respuesta = clienteHttp.post()
					.uri("/chat/completions")
					.contentType(MediaType.APPLICATION_JSON)
					.body(new PeticionGroq(modelo, armarMensajes(conversacion), 0.3, 500))
					.retrieve()
					.body(RespuestaGroq.class);

			String texto = extraerTexto(respuesta);
			boolean escalar = texto.contains(MARCADOR_ESCALAMIENTO);
			String textoLimpio = texto.replace(MARCADOR_ESCALAMIENTO, "").trim();
			if (textoLimpio.isEmpty()) {
				throw new AsistenteNoDisponibleException("El proveedor devolvió una respuesta vacía");
			}
			return new RespuestaDelAsistente(textoLimpio, escalar);
		} catch (RestClientException excepcion) {
			// El detalle va a la causa (logs internos); jamás al visitante.
			throw new AsistenteNoDisponibleException("Fallo llamando al proveedor de IA", excepcion);
		}
	}

	private List<MensajeGroq> armarMensajes(ConversacionDeAsistente conversacion) {
		List<MensajeGroq> mensajes = new java.util.ArrayList<>();
		mensajes.add(new MensajeGroq("system", promptDeSistema));
		for (MensajeDeChat mensaje : conversacion.mensajes()) {
			mensajes.add(new MensajeGroq(mensaje.rol() == RolDeMensaje.USUARIO ? "user" : "assistant",
					mensaje.texto()));
		}
		return mensajes;
	}

	private static String extraerTexto(RespuestaGroq respuesta) {
		if (respuesta == null || respuesta.choices() == null || respuesta.choices().isEmpty()
				|| respuesta.choices().getFirst().message() == null
				|| respuesta.choices().getFirst().message().content() == null) {
			throw new AsistenteNoDisponibleException("El proveedor devolvió una respuesta sin contenido");
		}
		return respuesta.choices().getFirst().message().content();
	}

	private static String cargarPromptDeSistema() {
		try {
			return new ClassPathResource("asistente-contexto.md").getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException excepcion) {
			throw new UncheckedIOException("No se pudo cargar asistente-contexto.md", excepcion);
		}
	}

	record PeticionGroq(String model, List<MensajeGroq> messages, double temperature, int max_tokens) {
	}

	record MensajeGroq(String role, String content) {
	}

	record RespuestaGroq(List<OpcionGroq> choices) {
	}

	record OpcionGroq(MensajeGroq message) {
	}

}
