package com.crearcode.leads.infraestructura.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AsistenteControllerIT {

	private static HttpServer stubGroq;
	private static final AtomicReference<Integer> statusDelStub = new AtomicReference<>(200);

	@BeforeAll
	static void levantarStubDeGroq() throws IOException {
		stubGroq = HttpServer.create(new InetSocketAddress(0), 0);
		stubGroq.createContext("/openai/v1/chat/completions", intercambio -> {
			String contenido = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Claro, te cuento.\"}}]}";
			byte[] cuerpo = contenido.getBytes(StandardCharsets.UTF_8);
			int status = statusDelStub.get();
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(status, cuerpo.length);
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
		// Límite anónimo bajo para probar HU-38 sin decenas de peticiones;
		// el rate limit por IP se relaja (su IT propio lo cubre).
		registry.add("app.asistente.limite-diario-anonimo", () -> 2);
		registry.add("app.rate-limit.asistente.max-intentos", () -> 1000);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private GeneradorDeToken generadorDeToken;

	@BeforeEach
	void reiniciarStub() {
		statusDelStub.set(200);
	}

	private static Map<String, Object> conversacion(String pregunta) {
		return Map.of("mensajes", List.of(Map.of("rol", "USUARIO", "texto", pregunta)));
	}

	private HttpEntity<Map<String, Object>> conSesionAnonima(Map<String, Object> cuerpo, String idSesion) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Sesion-Anonima", idSesion);
		return new HttpEntity<>(cuerpo, headers);
	}

	@Test
	void respondeLaPreguntaDeUnVisitante() {
		ResponseEntity<RespuestaAsistenteResponse> respuesta = restTemplate.postForEntity(
				"/api/asistente/mensajes",
				conSesionAnonima(conversacion("¿Qué servicios ofrecen?"), "sesion-ok"),
				RespuestaAsistenteResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().texto()).isEqualTo("Claro, te cuento.");
		assertThat(respuesta.getBody().escalarAHumano()).isFalse();
	}

	@Test
	void unaConversacionVaciaDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/mensajes",
				conSesionAnonima(Map.of("mensajes", List.of()), "sesion-vacia"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void unMensajeDemasiadoLargoDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/mensajes",
				conSesionAnonima(conversacion("a".repeat(1001)), "sesion-larga"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void unAnonimoQueAgotaSuCupoRecibe429ConElCodigoDeLimiteAnonimo() {
		String sesion = "sesion-limite";
		restTemplate.postForEntity("/api/asistente/mensajes",
				conSesionAnonima(conversacion("uno"), sesion), String.class);
		restTemplate.postForEntity("/api/asistente/mensajes",
				conSesionAnonima(conversacion("dos"), sesion), String.class);

		ResponseEntity<String> tercera = restTemplate.postForEntity("/api/asistente/mensajes",
				conSesionAnonima(conversacion("tres"), sesion), String.class);

		assertThat(tercera.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(tercera.getBody()).contains("limite-anonimo");
	}

	@Test
	void unClienteRegistradoTieneUnCupoMayorQueElAnonimo() {
		Usuario cliente = Usuario
				.registrarCliente(new Correo("asistente-cliente@correo-de-prueba.com"), "hash-cualquiera")
				.verificar();
		usuarios.guardar(cliente);
		String token = generadorDeToken.generar(cliente, Instant.now()).token();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);

		for (int i = 0; i < 3; i++) {
			ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/mensajes",
					new HttpEntity<>(conversacion("pregunta " + i), headers), String.class);
			assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
	}

	@Test
	void siElProveedorFallaElVisitanteRecibe503ConElCodigoNoDisponible() {
		statusDelStub.set(500);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/asistente/mensajes",
				conSesionAnonima(conversacion("hola"), "sesion-caida"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(respuesta.getBody()).contains("no-disponible");
	}

}
