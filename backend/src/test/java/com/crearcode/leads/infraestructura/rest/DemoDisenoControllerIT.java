package com.crearcode.leads.infraestructura.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API test del demo de diseño (F10d, ISS-128): SOLO registrados, con
 * doble stub — Groq (texto con formato) y Pollinations (bytes de
 * imagen) — sin red externa ni cuota.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class DemoDisenoControllerIT {

	private static final String PROPUESTA_FORMATEADA = "TITULO: App de pedidos para tu restaurante\\n"
			+ "FUNCIONALIDAD: Menú digital con disponibilidad\\n"
			+ "FUNCIONALIDAD: Pedidos ordenados a la cocina\\n"
			+ "FUNCIONALIDAD: Pago con link o contraentrega\\n"
			+ "FUNCIONALIDAD: Seguimiento del domicilio\\n"
			+ "FUNCIONALIDAD: Reporte diario de ventas";

	private static HttpServer stub;

	@BeforeAll
	static void levantarStubs() throws IOException {
		stub = HttpServer.create(new InetSocketAddress(0), 0);
		stub.createContext("/openai/v1/chat/completions", intercambio -> {
			String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\""
					+ PROPUESTA_FORMATEADA + "\"}}]}";
			byte[] cuerpo = json.getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(200, cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stub.createContext("/prompt", intercambio -> {
			byte[] cuerpo = "bytes-de-imagen".getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "image/jpeg");
			intercambio.sendResponseHeaders(200, cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stub.start();
	}

	@AfterAll
	static void apagarStubs() {
		stub.stop(0);
	}

	@DynamicPropertySource
	static void configurarStubsYLimites(DynamicPropertyRegistry registry) {
		registry.add("app.asistente.groq.url",
				() -> "http://localhost:" + stub.getAddress().getPort() + "/openai/v1");
		registry.add("app.demo.pollinations.url",
				() -> "http://localhost:" + stub.getAddress().getPort());
		registry.add("app.demo.limite-diario-registrado", () -> 2);
		registry.add("app.rate-limit.asistente.max-intentos", () -> 1000);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private GeneradorDeToken generadorDeToken;

	private static Map<String, Object> solicitud() {
		return Map.of("sector", "Restaurante", "queHace", "Vendemos almuerzos y domicilios",
				"queNecesita", "Recibir pedidos sin saturar el WhatsApp");
	}

	private HttpHeaders headersDeCliente(String correo) {
		Usuario cliente = Usuario.registrarCliente(new Correo(correo), "hash-cualquiera").verificar();
		usuarios.guardar(cliente);
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(generadorDeToken.generar(cliente, Instant.now()).token());
		return headers;
	}

	@Test
	void sinTokenElDemoRespondeNoAutorizado() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/demo-diseno",
				new HttpEntity<>(solicitud()), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void unClienteRegistradoRecibeSuBocetoCompleto() {
		ResponseEntity<BocetoDemoResponse> respuesta = restTemplate.postForEntity(
				"/api/asistente/demo-diseno",
				new HttpEntity<>(solicitud(), headersDeCliente("demo-cliente@correo-de-prueba.com")),
				BocetoDemoResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().titulo()).isEqualTo("App de pedidos para tu restaurante");
		assertThat(respuesta.getBody().funcionalidades()).hasSize(5);
		assertThat(respuesta.getBody().imagenBase64()).isNotBlank();
		assertThat(respuesta.getBody().tipoMime()).isEqualTo("image/jpeg");
	}

	@Test
	void unaSolicitudSinSectorDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/demo-diseno",
				new HttpEntity<>(Map.of("sector", " ", "queHace", "algo", "queNecesita", "algo"),
						headersDeCliente("demo-invalido@correo-de-prueba.com")),
				String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elLimiteDiarioDelRegistradoDevuelve429() {
		HttpHeaders headers = headersDeCliente("demo-limite@correo-de-prueba.com");
		restTemplate.postForEntity("/api/asistente/demo-diseno", new HttpEntity<>(solicitud(), headers),
				String.class);
		restTemplate.postForEntity("/api/asistente/demo-diseno", new HttpEntity<>(solicitud(), headers),
				String.class);

		ResponseEntity<String> tercera = restTemplate.postForEntity("/api/asistente/demo-diseno",
				new HttpEntity<>(solicitud(), headers), String.class);

		assertThat(tercera.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(tercera.getBody()).contains("limite-registrado");
	}

}
