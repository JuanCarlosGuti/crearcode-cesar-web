package com.crearcode.leads.infraestructura.demo;

import java.time.Duration;
import java.util.Base64;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * Adaptador de Pollinations (F10d): imágenes gratis sin key. Sirve de
 * proveedor por defecto y de respaldo automático de Cloudflare Workers
 * AI (ver {@link GeneradorDeImagenesConRespaldo}). Sin SLA en su capa
 * gratis. Qué proveedor se usa lo decide
 * {@link ConfiguracionDeGeneradorDeImagenes} con la variable
 * DEMO_PROVEEDOR_IMAGENES.
 */
class PollinationsGeneradorDeImagenesAdapter implements GeneradorDeImagenes {

	private final RestClient clienteHttp;

	PollinationsGeneradorDeImagenesAdapter(String urlBase, long timeoutSegundos) {
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
