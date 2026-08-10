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
 * API test del diagnóstico digital (F10c, ISS-125): contrato del
 * endpoint con el stub devolviendo el formato pactado.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class DiagnosticoControllerIT {

	private static final String INFORME_FORMATEADO = """
			VEREDICTO: Tu negocio tiene un problema de tiempo, no de ventas.
			OPORTUNIDAD: Respuestas automáticas | Las preguntas repetidas se contestan solas. | Dejas de contestar lo mismo todo el día.
			OPORTUNIDAD: Pedidos en un solo lugar | Cada pedido queda registrado con su estado. | Nadie vuelve a preguntar en qué va ese pedido.
			OPORTUNIDAD: Reportes que se arman solos | Ventas calculadas a partir de lo que registras. | Cierras el mes sin cuadrar nada a mano.
			""";

	private static HttpServer stubGroq;
	private static final AtomicReference<String> contenidoDelStub = new AtomicReference<>(INFORME_FORMATEADO);

	@BeforeAll
	static void levantarStubDeGroq() throws IOException {
		stubGroq = HttpServer.create(new InetSocketAddress(0), 0);
		stubGroq.createContext("/openai/v1/chat/completions", intercambio -> {
			String contenidoEscapado = contenidoDelStub.get()
					.replace("\\", "\\\\")
					.replace("\"", "\\\"")
					.replace("\n", "\\n");
			String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\""
					+ contenidoEscapado + "\"}}]}";
			byte[] cuerpo = json.getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(200, cuerpo.length);
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
		registry.add("app.diagnostico.limite-diario-anonimo", () -> 2);
		registry.add("app.rate-limit.asistente.max-intentos", () -> 1000);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeEach
	void reiniciarStub() {
		contenidoDelStub.set(INFORME_FORMATEADO);
	}

	private static Map<String, Object> cuestionario() {
		return Map.of("respuestas", List.of(
				Map.of("pregunta", "¿Cómo reciben los pedidos?", "respuesta", "WhatsApp y llamadas"),
				Map.of("pregunta", "¿Dónde guardan la información?", "respuesta", "En Excel o cuadernos")));
	}

	private static HttpEntity<Map<String, Object>> conSesionAnonima(Map<String, Object> cuerpo, String idSesion) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Sesion-Anonima", idSesion);
		return new HttpEntity<>(cuerpo, headers);
	}

	@Test
	void devuelveLaRadiografiaConVeredictoYTresOportunidades() {
		ResponseEntity<InformeDiagnosticoResponse> respuesta = restTemplate.postForEntity(
				"/api/asistente/diagnostico", conSesionAnonima(cuestionario(), "diag-ok"),
				InformeDiagnosticoResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().veredicto()).contains("problema de tiempo");
		assertThat(respuesta.getBody().oportunidades()).hasSize(3);
		assertThat(respuesta.getBody().oportunidades().getFirst().beneficio())
				.isEqualTo("Dejas de contestar lo mismo todo el día.");
	}

	@Test
	void unCuestionarioVacioDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/diagnostico",
				conSesionAnonima(Map.of("respuestas", List.of()), "diag-vacio"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elLimiteAnonimoDelDiagnosticoDevuelve429() {
		String sesion = "diag-limite";
		restTemplate.postForEntity("/api/asistente/diagnostico",
				conSesionAnonima(cuestionario(), sesion), String.class);
		restTemplate.postForEntity("/api/asistente/diagnostico",
				conSesionAnonima(cuestionario(), sesion), String.class);

		ResponseEntity<String> tercera = restTemplate.postForEntity("/api/asistente/diagnostico",
				conSesionAnonima(cuestionario(), sesion), String.class);

		assertThat(tercera.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(tercera.getBody()).contains("limite-anonimo");
	}

	@Test
	void unFormatoRotoDelProveedorDevuelve503SinCulparAlVisitante() {
		contenidoDelStub.set("texto sin el formato pactado");

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/diagnostico",
				conSesionAnonima(cuestionario(), "diag-roto"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(respuesta.getBody()).contains("no-disponible");
	}

}
