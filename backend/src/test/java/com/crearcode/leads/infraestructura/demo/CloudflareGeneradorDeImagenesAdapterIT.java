package com.crearcode.leads.infraestructura.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
 * Integration test del adaptador de Cloudflare Workers AI contra un
 * stub HTTP del JDK — sin red externa ni credenciales reales.
 */
class CloudflareGeneradorDeImagenesAdapterIT {

	private static final String MODELO = "@cf/black-forest-labs/flux-1-schnell";
	private static final String IMAGEN_BASE64 = Base64.getEncoder()
			.encodeToString("bytes-de-imagen-fake".getBytes(StandardCharsets.UTF_8));

	private static HttpServer stub;
	private static final AtomicReference<String> respuesta = new AtomicReference<>("");
	private static final AtomicReference<Integer> status = new AtomicReference<>(200);
	private static final AtomicReference<String> ultimaRuta = new AtomicReference<>("");
	private static final AtomicReference<String> ultimaAutorizacion = new AtomicReference<>("");
	private static final AtomicReference<String> ultimoCuerpo = new AtomicReference<>("");

	private CloudflareGeneradorDeImagenesAdapter adapter;

	@BeforeAll
	static void levantarStub() throws IOException {
		stub = HttpServer.create(new InetSocketAddress(0), 0);
		stub.createContext("/", intercambio -> {
			ultimaRuta.set(intercambio.getRequestURI().getPath());
			ultimaAutorizacion.set(String.valueOf(intercambio.getRequestHeaders().getFirst("Authorization")));
			ultimoCuerpo.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

			byte[] cuerpo = respuesta.get().getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(status.get(), cuerpo.length == 0 ? -1 : cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stub.start();
	}

	@AfterAll
	static void apagarStub() {
		stub.stop(0);
	}

	@BeforeEach
	void preparar() {
		status.set(200);
		respuesta.set("{\"result\":{\"image\":\"" + IMAGEN_BASE64 + "\"},\"success\":true}");
		adapter = new CloudflareGeneradorDeImagenesAdapter(
				"http://localhost:" + stub.getAddress().getPort(),
				"cuenta-de-prueba", "token-de-prueba", MODELO, 10);
	}

	@Test
	void pideLaImagenAlModeloDeLaCuentaConElTokenEnElHeader() {
		ImagenGenerada imagen = adapter.generar("App de pedidos para un restaurante");

		assertThat(ultimaRuta.get()).isEqualTo("/accounts/cuenta-de-prueba/ai/run/" + MODELO);
		assertThat(ultimaAutorizacion.get()).isEqualTo("Bearer token-de-prueba");
		assertThat(ultimoCuerpo.get()).contains("App de pedidos para un restaurante");
		assertThat(imagen.base64()).isEqualTo(IMAGEN_BASE64);
		assertThat(imagen.tipoMime()).isEqualTo("image/jpeg");
	}

	@Test
	void unErrorDelProveedorSeTraduceANoDisponible() {
		status.set(500);
		respuesta.set("");

		assertThatThrownBy(() -> adapter.generar("cualquier cosa"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unaRespuestaSinImagenSeTraduceANoDisponible() {
		respuesta.set("{\"result\":{},\"success\":true}");

		assertThatThrownBy(() -> adapter.generar("cualquier cosa"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unaRespuestaConSuccessFalseSeTraduceANoDisponible() {
		respuesta.set("{\"result\":{\"image\":\"" + IMAGEN_BASE64 + "\"},\"success\":false}");

		assertThatThrownBy(() -> adapter.generar("cualquier cosa"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
