package com.crearcode.leads.infraestructura.demo;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * Adaptador de Gemini Flash Image (F10d, decisión 8 de docs/10). La
 * key viaja solo en el header {@code x-goog-api-key} — nunca se
 * loguea. La URL es configurable para apuntar al stub en tests y CI.
 */
@Component
@ConditionalOnProperty(name = "app.demo.proveedor-imagenes", havingValue = "gemini")
class GeminiGeneradorDeImagenesAdapter implements GeneradorDeImagenes {

	private final RestClient clienteHttp;
	private final String modelo;

	GeminiGeneradorDeImagenesAdapter(
			@Value("${app.demo.gemini.url}") String urlBase,
			@Value("${app.demo.gemini.key}") String apiKey,
			@Value("${app.demo.gemini.modelo}") String modelo,
			@Value("${app.demo.gemini.timeout-segundos}") long timeoutSegundos) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
		fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
		this.clienteHttp = RestClient.builder()
				.baseUrl(urlBase)
				.defaultHeader("x-goog-api-key", apiKey)
				.requestFactory(fabrica)
				.build();
		this.modelo = modelo;
	}

	@Override
	public ImagenGenerada generar(String descripcion) {
		try {
			RespuestaGemini respuesta = clienteHttp.post()
					.uri("/models/{modelo}:generateContent", modelo)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new PeticionGemini(
							List.of(new Contenido(List.of(new ParteDeTexto(descripcion)))),
							new ConfigDeGeneracion(List.of("IMAGE"))))
					.retrieve()
					.body(RespuestaGemini.class);

			return extraerImagen(respuesta);
		} catch (RestClientException excepcion) {
			// El detalle va a la causa (logs internos); jamás al visitante.
			throw new AsistenteNoDisponibleException("Fallo llamando al proveedor de imágenes", excepcion);
		}
	}

	private static ImagenGenerada extraerImagen(RespuestaGemini respuesta) {
		if (respuesta != null && respuesta.candidates() != null) {
			for (Candidato candidato : respuesta.candidates()) {
				if (candidato.content() == null || candidato.content().parts() == null) {
					continue;
				}
				for (ParteDeRespuesta parte : candidato.content().parts()) {
					if (parte.inlineData() != null && parte.inlineData().data() != null
							&& !parte.inlineData().data().isBlank()) {
						return new ImagenGenerada(parte.inlineData().data(), parte.inlineData().mimeType());
					}
				}
			}
		}
		throw new AsistenteNoDisponibleException("El proveedor no devolvió ninguna imagen");
	}

	record PeticionGemini(List<Contenido> contents, ConfigDeGeneracion generationConfig) {
	}

	record Contenido(List<ParteDeTexto> parts) {
	}

	record ParteDeTexto(String text) {
	}

	record ConfigDeGeneracion(List<String> responseModalities) {
	}

	record RespuestaGemini(List<Candidato> candidates) {
	}

	record Candidato(ContenidoDeRespuesta content) {
	}

	record ContenidoDeRespuesta(List<ParteDeRespuesta> parts) {
	}

	record ParteDeRespuesta(DatosInline inlineData) {
	}

	record DatosInline(String mimeType, String data) {
	}

}
