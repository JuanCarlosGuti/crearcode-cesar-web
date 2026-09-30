package com.crearcode.leads.infraestructura.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El cupo anónimo se contaba contra {@code X-Sesion-Anonima}, un valor
 * que genera y controla el propio navegador: bastaba borrar
 * {@code sessionStorage} —o mandar otro header— para tener el cupo
 * entero otra vez, cuantas veces se quisiera (auditoría del 28 sep
 * 2026, P0-2a).
 *
 * <p>
 * Esta prueba es la que faltaba: agota el cupo cambiando de sesión en
 * cada petición, que es exactamente lo que hace quien se salta el
 * límite, y exige que el techo por red lo corte igual. Vive en su
 * propia clase porque necesita límites minúsculos que romperían al
 * resto de pruebas del asistente.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CupoPorRedIT {

	private static final int TECHO_POR_RED = 3;

	private static HttpServer stubGroq;

	@BeforeAll
	static void levantarStubDeGroq() throws IOException {
		stubGroq = HttpServer.create(new InetSocketAddress(0), 0);
		stubGroq.createContext("/openai/v1/chat/completions", intercambio -> {
			byte[] cuerpo = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Claro.\"}}]}"
					.getBytes(StandardCharsets.UTF_8);
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
	static void configurarLimites(DynamicPropertyRegistry registry) {
		registry.add("app.asistente.groq.url",
				() -> "http://localhost:" + stubGroq.getAddress().getPort() + "/openai/v1");
		// Cupo personal alto y techo de red bajo: así lo único que puede
		// cortar es la red, que es lo que se está probando.
		registry.add("app.asistente.limite-diario-anonimo", () -> 100);
		registry.add("app.asistente.limite-diario-por-red", () -> TECHO_POR_RED);
		registry.add("app.rate-limit.asistente.max-intentos", () -> 1000);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	private ResponseEntity<String> preguntarComo(String idSesion) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Sesion-Anonima", idSesion);
		Map<String, Object> cuerpo = Map.of("mensajes",
				List.of(Map.of("rol", "USUARIO", "texto", "¿qué servicios ofrecen?")));
		return restTemplate.postForEntity("/api/asistente/mensajes", new HttpEntity<>(cuerpo, headers),
				String.class);
	}

	@Test
	void cambiarDeSesionAnonimaEnCadaPeticionYaNoDevuelveElCupo() {
		for (int i = 0; i < TECHO_POR_RED; i++) {
			assertThat(preguntarComo("sesion-nueva-" + i).getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		ResponseEntity<String> despuesDelTecho = preguntarComo("sesion-nueva-999");

		assertThat(despuesDelTecho.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(despuesDelTecho.getBody()).contains("limite-anonimo");
	}

}
