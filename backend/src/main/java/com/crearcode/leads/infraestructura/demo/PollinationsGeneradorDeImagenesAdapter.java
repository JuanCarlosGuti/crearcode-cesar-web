package com.crearcode.leads.infraestructura.demo;

import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * Adaptador de Pollinations (F10d): imágenes gratis sin key. Proveedor
 * INTERINO mientras la capa gratis de Gemini no incluya imágenes
 * (verificado 10 ago 2026: límite 0 en todos sus modelos de imagen) —
 * sin SLA, así que la decisión del proveedor de producción se cierra
 * con el usuario en ISS-132 (Gemini con cuota, Cloudflare o este).
 * Cambiar de proveedor es solo la variable DEMO_PROVEEDOR_IMAGENES.
 */
@Component
@ConditionalOnProperty(name = "app.demo.proveedor-imagenes", havingValue = "pollinations", matchIfMissing = true)
class PollinationsGeneradorDeImagenesAdapter implements GeneradorDeImagenes {

	private final RestClient clienteHttp;

	PollinationsGeneradorDeImagenesAdapter(
			@Value("${app.demo.pollinations.url}") String urlBase,
			@Value("${app.demo.pollinations.timeout-segundos}") long timeoutSegundos) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
		fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
		this.clienteHttp = RestClient.builder()
				.baseUrl(urlBase)
				.requestFactory(fabrica)
				.build();
	}

	@Override
	public ImagenGenerada generar(String descripcion) {
		try {
			byte[] imagen = clienteHttp.get()
					.uri(uriBuilder -> uriBuilder
							.pathSegment("prompt", descripcion)
							.queryParam("width", 768)
							.queryParam("height", 768)
							.queryParam("nologo", "true")
							.build())
					.retrieve()
					.body(byte[].class);

			if (imagen == null || imagen.length == 0) {
				throw new AsistenteNoDisponibleException("El proveedor devolvió una imagen vacía");
			}
			return new ImagenGenerada(Base64.getEncoder().encodeToString(imagen), "image/jpeg");
		} catch (RestClientException excepcion) {
			throw new AsistenteNoDisponibleException("Fallo llamando al proveedor de imágenes", excepcion);
		}
	}

}
