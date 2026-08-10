package com.crearcode.leads.infraestructura.demo;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * Adaptador de Cloudflare Workers AI (proveedor primario de imágenes
 * del demo de diseño). El token viaja solo en el header
 * {@code Authorization} — nunca se loguea — y la URL es configurable
 * para apuntar al stub en tests y CI.
 *
 * <p>El modelo se concatena a la ruta en vez de expandirse como
 * variable de plantilla: su nombre lleva barras
 * ({@code @cf/black-forest-labs/flux-1-schnell}) y la expansión las
 * codificaría como {@code %2F}, rompiendo la ruta. El valor viene de
 * configuración, nunca del visitante.
 */
class CloudflareGeneradorDeImagenesAdapter implements GeneradorDeImagenes {

	private final RestClient clienteHttp;
	private final String idDeCuenta;
	private final String modelo;

	CloudflareGeneradorDeImagenesAdapter(String urlBase, String idDeCuenta, String apiToken, String modelo,
			long timeoutSegundos) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory();
		fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
		this.clienteHttp = RestClient.builder()
				.baseUrl(urlBase)
				.defaultHeader("Authorization", "Bearer " + apiToken)
				.requestFactory(fabrica)
				.build();
		this.idDeCuenta = idDeCuenta;
		this.modelo = modelo;
	}

	@Override
	public ImagenGenerada generar(String descripcion) {
		try {
			RespuestaCloudflare respuesta = clienteHttp.post()
					.uri("/accounts/{cuenta}/ai/run/" + modelo, idDeCuenta)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new PeticionCloudflare(descripcion))
					.retrieve()
					.body(RespuestaCloudflare.class);

			return extraerImagen(respuesta);
		} catch (RestClientException excepcion) {
			// El detalle va a la causa (logs internos); jamás al visitante.
			throw new AsistenteNoDisponibleException("Fallo llamando al proveedor de imágenes", excepcion);
		}
	}

	private static ImagenGenerada extraerImagen(RespuestaCloudflare respuesta) {
		if (respuesta == null || !respuesta.success() || respuesta.result() == null
				|| respuesta.result().image() == null || respuesta.result().image().isBlank()) {
			throw new AsistenteNoDisponibleException("El proveedor no devolvió ninguna imagen");
		}
		return new ImagenGenerada(respuesta.result().image(), "image/jpeg");
	}

	record PeticionCloudflare(String prompt) {
	}

	record RespuestaCloudflare(ResultadoCloudflare result, boolean success) {
	}

	record ResultadoCloudflare(String image) {
	}

}
