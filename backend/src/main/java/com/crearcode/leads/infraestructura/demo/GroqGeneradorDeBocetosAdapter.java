package com.crearcode.leads.infraestructura.demo;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.GeneradorDeBocetos;
import com.crearcode.leads.dominio.SolicitudDeDemo;

/**
 * Groq arma el mismo boceto que Claude cuando Claude falla (decisión
 * 33): mismas instrucciones, mismo esquema —en modo estricto, que
 * {@code openai/gpt-oss-120b} admite— y el mismo lector. La key viaja
 * solo en el header Authorization y nunca se loguea.
 */
class GroqGeneradorDeBocetosAdapter implements GeneradorDeBocetos {

	private static final int MAXIMO_DE_TOKENS = 6000;
	private static final double TEMPERATURA = 0.4;
	private static final Map<String, Object> FORMATO = Map.of(
			"type", "json_schema",
			"json_schema", Map.of("name", "boceto", "strict", true, "schema", InstruccionesDeBoceto.ESQUEMA));

	private final RestClient clienteHttp;
	private final String modelo;

	GroqGeneradorDeBocetosAdapter(String urlBase, String apiKey, String modelo, long timeoutSegundos) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
		fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
		this.clienteHttp = RestClient.builder()
				.baseUrl(urlBase)
				.defaultHeader("Authorization", "Bearer " + apiKey)
				.requestFactory(fabrica)
				.build();
		this.modelo = modelo;
	}

	@Override
	public BocetoDeSolucion generar(SolicitudDeDemo solicitud) {
		RespuestaGroq respuesta;
		try {
			respuesta = clienteHttp.post()
					.uri("/chat/completions")
					.contentType(MediaType.APPLICATION_JSON)
					.body(new PeticionGroq(modelo,
							List.of(new MensajeGroq("system", InstruccionesDeBoceto.INSTRUCCIONES),
									new MensajeGroq("user", InstruccionesDeBoceto.mensajeDelVisitante(solicitud))),
							TEMPERATURA, MAXIMO_DE_TOKENS, FORMATO))
					.retrieve()
					.body(RespuestaGroq.class);
		} catch (RestClientException excepcion) {
			throw new AsistenteNoDisponibleException("Fallo llamando a Groq para el boceto", excepcion);
		}
		if (respuesta == null || respuesta.choices() == null || respuesta.choices().isEmpty()
				|| respuesta.choices().getFirst().message() == null
				|| respuesta.choices().getFirst().message().content() == null) {
			throw new AsistenteNoDisponibleException("Groq devolvió un boceto sin contenido");
		}
		OpcionGroq opcion = respuesta.choices().getFirst();
		if (opcion.finish_reason() != null && !"stop".equals(opcion.finish_reason())) {
			// "length" deja el JSON a medias: el modo estricto no lo cubre.
			throw new AsistenteNoDisponibleException("Groq no terminó el boceto (" + opcion.finish_reason() + ")");
		}
		return LectorDeBoceto.leer(opcion.message().content());
	}

	record PeticionGroq(String model, List<MensajeGroq> messages, double temperature, int max_tokens,
			Map<String, Object> response_format) {
	}

	record MensajeGroq(String role, String content) {
	}

	record RespuestaGroq(List<OpcionGroq> choices) {
	}

	record OpcionGroq(MensajeGroq message, String finish_reason) {
	}

}
