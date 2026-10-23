package com.crearcode.leads.infraestructura.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.SolicitudDeDemo;
import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * ISS-232: el respaldo de Groq produce el mismo boceto que Claude, con
 * el mismo esquema en modo estricto. Contra un servidor local, sin
 * gastar cuota.
 */
class GroqGeneradorDeBocetosAdapterIT {

	private static final JsonMapper JSON = JsonMapper.shared();

	private static HttpServer stub;
	private static int puerto;

	private static final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
	private static final AtomicReference<String> autorizacionRecibida = new AtomicReference<>();
	private static final AtomicReference<Integer> statusARetornar = new AtomicReference<>(200);
	private static final AtomicReference<String> respuestaARetornar = new AtomicReference<>("");

	private static final SolicitudDeDemo SOLICITUD = new SolicitudDeDemo("Restaurante",
			"Vendemos almuerzos y domicilios", "Recibir pedidos sin saturar el WhatsApp");

	@BeforeAll
	static void levantarStub() throws IOException {
		stub = HttpServer.create(new InetSocketAddress(0), 0);
		stub.createContext("/openai/v1/chat/completions", intercambio -> {
			cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			autorizacionRecibida.set(intercambio.getRequestHeaders().getFirst("Authorization"));
			byte[] cuerpo = respuestaARetornar.get().getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(statusARetornar.get(), cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stub.start();
		puerto = stub.getAddress().getPort();
	}

	@AfterAll
	static void apagarStub() {
		stub.stop(0);
	}

	@BeforeEach
	void reiniciarStub() {
		statusARetornar.set(200);
		respuestaARetornar.set(respuestaDeGroq(BocetoDePrueba.JSON, "stop"));
		cuerpoRecibido.set(null);
	}

	private static String respuestaDeGroq(String contenido, String razonDeFin) {
		return JSON.writeValueAsString(Map.of("choices", List.of(Map.of(
				"message", Map.of("role", "assistant", "content", contenido),
				"finish_reason", razonDeFin))));
	}

	private GroqGeneradorDeBocetosAdapter crearAdaptador() {
		return new GroqGeneradorDeBocetosAdapter("http://localhost:" + puerto + "/openai/v1", "key-de-prueba",
				"modelo-de-prueba", 5);
	}

	@Test
	void armaElMismoBocetoConElEsquemaEnModoEstricto() {
		BocetoDeSolucion boceto = crearAdaptador().generar(SOLICITUD);

		assertThat(boceto.titulo()).isEqualTo("App de pedidos para tu restaurante");
		assertThat(autorizacionRecibida.get()).isEqualTo("Bearer key-de-prueba");

		JsonNode peticion = JSON.readTree(cuerpoRecibido.get());
		assertThat(peticion.path("model").asString()).isEqualTo("modelo-de-prueba");
		JsonNode formato = peticion.path("response_format");
		assertThat(formato.path("type").asString()).isEqualTo("json_schema");
		assertThat(formato.path("json_schema").path("strict").asBoolean()).isTrue();
		assertThat(formato.path("json_schema").path("schema").path("properties").has("pantallas")).isTrue();
		assertThat(peticion.path("messages").get(0).path("content").asString()).contains("LAS PIEZAS");
		assertThat(peticion.path("messages").get(1).path("content").asString()).contains("Recibir pedidos");
	}

	@Test
	void unaRespuestaCortadaPorLargoEsFallaDelProveedor() {
		respuestaARetornar.set(respuestaDeGroq("{\"titulo\": \"App", "length"));

		assertThatThrownBy(() -> crearAdaptador().generar(SOLICITUD))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unErrorDeGroqEsFallaDelProveedor() {
		statusARetornar.set(503);
		respuestaARetornar.set("{\"error\":{\"message\":\"over capacity\"}}");

		assertThatThrownBy(() -> crearAdaptador().generar(SOLICITUD))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unBocetoQueNoCumpleLasReglasEsFallaDelProveedor() {
		respuestaARetornar.set(respuestaDeGroq(BocetoDePrueba.JSON.replace("\"En camino\"", "\"$ 18.000\""), "stop"));

		assertThatThrownBy(() -> crearAdaptador().generar(SOLICITUD))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
