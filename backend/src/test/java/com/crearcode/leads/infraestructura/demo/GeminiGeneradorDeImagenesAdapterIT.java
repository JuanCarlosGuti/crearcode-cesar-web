package com.crearcode.leads.infraestructura.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ImagenGenerada;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test del adaptador de Gemini (F10d, ISS-127) contra un
 * stub HTTP del JDK: verifica el contrato del request (ruta del
 * modelo, key en header, prompt como texto) y el parseo de la imagen
 * base64 — sin red externa ni cuota.
 */
class GeminiGeneradorDeImagenesAdapterIT {

	private static HttpServer stubGemini;
	private static final AtomicReference<Integer> statusDelStub = new AtomicReference<>(200);
	private static final AtomicReference<String> contenidoDelStub = new AtomicReference<>("");
	private static final AtomicReference<String> ultimaRuta = new AtomicReference<>("");
	private static final AtomicReference<String> ultimaKey = new AtomicReference<>("");
	private static final AtomicReference<String> ultimoCuerpo = new AtomicReference<>("");

	private static final String RESPUESTA_CON_IMAGEN = """
			{"candidates":[{"content":{"parts":[
			  {"text":"Aquí está tu boceto"},
			  {"inlineData":{"mimeType":"image/png","data":"aW1hZ2VuLWZha2U="}}
			]}}]}
			""";

	private GeminiGeneradorDeImagenesAdapter adapter;

	@BeforeAll
	static void levantarStub() throws IOException {
		stubGemini = HttpServer.create(new InetSocketAddress(0), 0);
		stubGemini.createContext("/", intercambio -> {
			ultimaRuta.set(intercambio.getRequestURI().getPath());
			ultimaKey.set(intercambio.getRequestHeaders().getFirst("x-goog-api-key"));
			ultimoCuerpo.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] cuerpo = contenidoDelStub.get().getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(statusDelStub.get(), cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stubGemini.start();
	}

	@AfterAll
	static void apagarStub() {
		stubGemini.stop(0);
	}

	@BeforeEach
	void preparar() {
		statusDelStub.set(200);
		contenidoDelStub.set(RESPUESTA_CON_IMAGEN);
		adapter = new GeminiGeneradorDeImagenesAdapter(
				"http://localhost:" + stubGemini.getAddress().getPort() + "/v1beta",
				"key-de-prueba", "gemini-modelo-prueba", 10);
	}

	@Test
	void llamaAlModeloConLaKeyEnHeaderYDevuelveLaImagen() {
		ImagenGenerada imagen = adapter.generar("App de pedidos para un restaurante");

		assertThat(ultimaRuta.get()).isEqualTo("/v1beta/models/gemini-modelo-prueba:generateContent");
		assertThat(ultimaKey.get()).isEqualTo("key-de-prueba");
		assertThat(ultimoCuerpo.get()).contains("App de pedidos para un restaurante");
		assertThat(ultimoCuerpo.get()).contains("IMAGE");
		assertThat(imagen.base64()).isEqualTo("aW1hZ2VuLWZha2U=");
		assertThat(imagen.tipoMime()).isEqualTo("image/png");
	}

	@Test
	void unErrorDelProveedorSeTraduceANoDisponible() {
		statusDelStub.set(500);

		assertThatThrownBy(() -> adapter.generar("cualquier cosa"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unaRespuestaSinImagenSeTraduceANoDisponible() {
		contenidoDelStub.set("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"solo texto\"}]}}]}");

		assertThatThrownBy(() -> adapter.generar("cualquier cosa"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
