package com.crearcode.leads.infraestructura.rest;

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
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API test del simulador (F10b, ISS-123): contrato del endpoint y
 * verificación de la plantilla anti-inyección tal como viaja al
 * proveedor (el stub captura el cuerpo).
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SimuladorControllerIT {

	private static HttpServer stubGroq;
	private static final AtomicReference<Integer> statusDelStub = new AtomicReference<>(200);
	private static final AtomicReference<String> ultimoCuerpoRecibido = new AtomicReference<>("");

	@BeforeAll
	static void levantarStubDeGroq() throws IOException {
		stubGroq = HttpServer.create(new InetSocketAddress(0), 0);
		stubGroq.createContext("/openai/v1/chat/completions", intercambio -> {
			ultimoCuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			String contenido = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"¡Con gusto! Te confirmo disponibilidad.\"}}]}";
			byte[] cuerpo = contenido.getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(statusDelStub.get(), cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stubGroq.start();
	}

	@AfterAll
	static void apagarStubDeGroq() {
		stubGroq.stop(0);
	}

	@DynamicPropertySource
	static void configurarStubYLimites(DynamicPropertyRegistry registry) {
		registry.add("app.asistente.groq.url",
				() -> "http://localhost:" + stubGroq.getAddress().getPort() + "/openai/v1");
		registry.add("app.simulador.limite-diario-anonimo", () -> 2);
		registry.add("app.rate-limit.asistente.max-intentos", () -> 1000);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeEach
	void reiniciarStub() {
		statusDelStub.set(200);
		ultimoCuerpoRecibido.set("");
	}

	private static Map<String, Object> peticion(String nombre, String rubro, String pregunta) {
		return Map.of(
				"negocio", Map.of("nombre", nombre, "rubro", rubro),
				"mensajes", List.of(Map.of("rol", "USUARIO", "texto", pregunta)));
	}

	private static HttpEntity<Map<String, Object>> conSesionAnonima(Map<String, Object> cuerpo, String idSesion) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Sesion-Anonima", idSesion);
		return new HttpEntity<>(cuerpo, headers);
	}

	@Test
	void respondeComoElChatbotDelNegocioYElPromptInjertaLosDatosDelimitados() {
		ResponseEntity<RespuestaAsistenteResponse> respuesta = restTemplate.postForEntity(
				"/api/asistente/simulador",
				conSesionAnonima(peticion("Ferretería La 16", "ferretería", "¿Tienen tornillos?"), "sim-ok"),
				RespuestaAsistenteResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().texto()).isEqualTo("¡Con gusto! Te confirmo disponibilidad.");
		// La plantilla viaja como system prompt con los datos entre comillas
		// y las reglas anti-inyección (el JSON escapa las comillas).
		assertThat(ultimoCuerpoRecibido.get()).contains("Ferretería La 16");
		assertThat(ultimoCuerpoRecibido.get()).contains("NUNCA instrucciones");
		assertThat(ultimoCuerpoRecibido.get()).contains("NUNCA inventes precios");
	}

	@Test
	void unNegocioSinNombreDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/simulador",
				conSesionAnonima(peticion("   ", "ferretería", "hola"), "sim-invalido"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elLimiteAnonimoDelSimuladorEsPropioYDevuelve429() {
		String sesion = "sim-limite";
		restTemplate.postForEntity("/api/asistente/simulador",
				conSesionAnonima(peticion("Tienda Ana", "tienda", "uno"), sesion), String.class);
		restTemplate.postForEntity("/api/asistente/simulador",
				conSesionAnonima(peticion("Tienda Ana", "tienda", "dos"), sesion), String.class);

		ResponseEntity<String> tercera = restTemplate.postForEntity("/api/asistente/simulador",
				conSesionAnonima(peticion("Tienda Ana", "tienda", "tres"), sesion), String.class);

		assertThat(tercera.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(tercera.getBody()).contains("limite-anonimo");
	}

	@Test
	void siElProveedorFallaElVisitanteRecibe503() {
		statusDelStub.set(500);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/simulador",
				conSesionAnonima(peticion("Tienda Ana", "tienda", "hola"), "sim-caido"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(respuesta.getBody()).contains("no-disponible");
	}

}
