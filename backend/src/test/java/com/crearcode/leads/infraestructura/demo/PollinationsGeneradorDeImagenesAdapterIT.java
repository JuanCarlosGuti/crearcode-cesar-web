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
 * Integration test del adaptador interino de Pollinations (F10d,
 * ISS-127) contra un stub HTTP del JDK — sin red externa.
 */
class PollinationsGeneradorDeImagenesAdapterIT {

	private static final byte[] IMAGEN_FAKE = "bytes-de-imagen-fake".getBytes(StandardCharsets.UTF_8);

	private static HttpServer stub;
	private static final AtomicReference<Integer> statusDelStub = new AtomicReference<>(200);
	private static final AtomicReference<String> ultimaRuta = new AtomicReference<>("");
	private static final AtomicReference<String> ultimaQuery = new AtomicReference<>("");

	private PollinationsGeneradorDeImagenesAdapter adapter;

	@BeforeAll
	static void levantarStub() throws IOException {
		stub = HttpServer.create(new InetSocketAddress(0), 0);
		stub.createContext("/", intercambio -> {
			ultimaRuta.set(intercambio.getRequestURI().getPath());
			ultimaQuery.set(intercambio.getRequestURI().getQuery());
			int status = statusDelStub.get();
			byte[] cuerpo = status == 200 ? IMAGEN_FAKE : new byte[0];
			intercambio.getResponseHeaders().add("Content-Type", "image/jpeg");
			intercambio.sendResponseHeaders(status, cuerpo.length == 0 ? -1 : cuerpo.length);
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
		statusDelStub.set(200);
		adapter = new PollinationsGeneradorDeImagenesAdapter(
				"http://localhost:" + stub.getAddress().getPort(), 10);
	}

	@Test
	void pideLaImagenPorPromptYLaDevuelveEnBase64() {
		ImagenGenerada imagen = adapter.generar("App de pedidos para un restaurante");

		// getPath() decodifica el percent-encoding: se verifica el texto ya
		// decodificado (la codificación la hace el uriBuilder del cliente).
		assertThat(ultimaRuta.get()).startsWith("/prompt/");
		assertThat(ultimaRuta.get()).contains("App de pedidos para un restaurante");
		assertThat(ultimaQuery.get()).contains("nologo=true");
		assertThat(imagen.base64()).isEqualTo(Base64.getEncoder().encodeToString(IMAGEN_FAKE));
		assertThat(imagen.tipoMime()).isEqualTo("image/jpeg");
	}

	@Test
	void unErrorDelProveedorSeTraduceANoDisponible() {
		statusDelStub.set(500);

		assertThatThrownBy(() -> adapter.generar("cualquier cosa"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
