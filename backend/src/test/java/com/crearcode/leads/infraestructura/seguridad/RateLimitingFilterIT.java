package com.crearcode.leads.infraestructura.seguridad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.infraestructura.rest.SolicitudRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Umbral bajo propio de este test (vía {@code @DynamicPropertySource}),
 * independiente del valor de producción: así no hace falta disparar
 * decenas de peticiones para probar el mecanismo. {@code BEFORE_CLASS}
 * además fuerza un contexto de Spring nuevo (y por lo tanto un
 * {@link RateLimitingFilter} con contador en cero) sin importar qué
 * otras clases de test hayan compartido esta configuración antes en el
 * mismo fork de Maven.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class RateLimitingFilterIT {

	private static final int MAX_SOLICITUDES_EN_ESTE_TEST = 3;
	private static final int MAX_INTENTOS_LOGIN_EN_ESTE_TEST = 3;
	private static final int MAX_CUENTAS_EN_ESTE_TEST = 3;

	@DynamicPropertySource
	static void umbralDePrueba(DynamicPropertyRegistry registry) {
		registry.add("app.rate-limit.max-solicitudes", () -> MAX_SOLICITUDES_EN_ESTE_TEST);
		registry.add("app.rate-limit.ventana-minutos", () -> 10);
		registry.add("app.rate-limit.login.max-intentos", () -> MAX_INTENTOS_LOGIN_EN_ESTE_TEST);
		registry.add("app.rate-limit.login.ventana-minutos", () -> 15);
		registry.add("app.rate-limit.registro.max-intentos", () -> MAX_CUENTAS_EN_ESTE_TEST);
		registry.add("app.rate-limit.registro.ventana-minutos", () -> 60);
		registry.add("app.rate-limit.verificacion.max-intentos", () -> MAX_CUENTAS_EN_ESTE_TEST);
		registry.add("app.rate-limit.verificacion.ventana-minutos", () -> 15);
		registry.add("app.rate-limit.reenvio-verificacion.max-intentos", () -> MAX_CUENTAS_EN_ESTE_TEST);
		registry.add("app.rate-limit.reenvio-verificacion.ventana-minutos", () -> 15);
		registry.add("app.rate-limit.recuperacion.max-intentos", () -> MAX_CUENTAS_EN_ESTE_TEST);
		registry.add("app.rate-limit.recuperacion.ventana-minutos", () -> 15);
		registry.add("app.rate-limit.restablecimiento.max-intentos", () -> MAX_CUENTAS_EN_ESTE_TEST);
		registry.add("app.rate-limit.restablecimiento.ventana-minutos", () -> 15);
		registry.add("app.rate-limit.asistente.max-intentos", () -> MAX_CUENTAS_EN_ESTE_TEST);
		registry.add("app.rate-limit.asistente.ventana-minutos", () -> 15);
		// Puerto cerrado: las peticiones que pasan el filtro fallan al
		// instante (503) en vez de intentar salir a la red real.
		registry.add("app.asistente.groq.url", () -> "http://localhost:1");
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void limitaLasSolicitudesRepetidasDesdeLaMismaIp() {
		SolicitudRequest solicitud = new SolicitudRequest("Juan Pérez", null, "nombre@empresa.com",
				"3001234567", ServicioDeInteres.OTRO, "mensaje", true, null);

		List<HttpStatusCode> estados = new ArrayList<>();
		for (int i = 0; i < MAX_SOLICITUDES_EN_ESTE_TEST + 3; i++) {
			estados.add(restTemplate.postForEntity("/api/solicitudes", solicitud, String.class).getStatusCode());
		}

		assertThat(estados).contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLosIntentosDeLoginRepetidosDesdeLaMismaIp() {
		Map<String, String> loginInvalido = Map.of("correo", "quien-sea@crearcode-cesar.local",
				"contrasena", "clave-incorrecta");

		List<HttpStatusCode> estados = new ArrayList<>();
		for (int i = 0; i < MAX_INTENTOS_LOGIN_EN_ESTE_TEST + 3; i++) {
			estados.add(restTemplate.postForEntity("/api/auth/login", loginInvalido, String.class).getStatusCode());
		}

		assertThat(estados).contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLosRegistrosDeCuentaRepetidosDesdeLaMismaIp() {
		assertThat(estadosTrasRepetir("/api/auth/registro",
				Map.of("correo", "rate-limit-registro@correo-de-prueba.com", "contrasena", "contrasena-larga")))
				.contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLasVerificacionesRepetidasDesdeLaMismaIp() {
		assertThat(estadosTrasRepetir("/api/auth/verificacion", Map.of("token", "token-cualquiera")))
				.contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLosReenviosDeVerificacionRepetidosDesdeLaMismaIp() {
		assertThat(estadosTrasRepetir("/api/auth/reenvio-verificacion",
				Map.of("correo", "rate-limit-reenvio@correo-de-prueba.com")))
				.contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLasRecuperacionesRepetidasDesdeLaMismaIp() {
		assertThat(estadosTrasRepetir("/api/auth/recuperacion",
				Map.of("correo", "rate-limit-recuperacion@correo-de-prueba.com")))
				.contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLosRestablecimientosRepetidosDesdeLaMismaIp() {
		assertThat(estadosTrasRepetir("/api/auth/restablecimiento",
				Map.of("token", "token-cualquiera", "contrasena", "contrasena-larga")))
				.contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void limitaLosMensajesAlAsistenteRepetidosDesdeLaMismaIp() {
		Map<String, Object> conversacion = Map.of("mensajes",
				List.of(Map.of("rol", "USUARIO", "texto", "hola")));

		List<HttpStatusCode> estados = new ArrayList<>();
		for (int i = 0; i < MAX_CUENTAS_EN_ESTE_TEST + 3; i++) {
			estados.add(restTemplate.postForEntity("/api/asistente/mensajes", conversacion, String.class)
					.getStatusCode());
		}

		assertThat(estados).contains(HttpStatus.TOO_MANY_REQUESTS);
	}

	private List<HttpStatusCode> estadosTrasRepetir(String ruta, Map<String, String> cuerpo) {
		List<HttpStatusCode> estados = new ArrayList<>();
		for (int i = 0; i < MAX_CUENTAS_EN_ESTE_TEST + 3; i++) {
			estados.add(restTemplate.postForEntity(ruta, cuerpo, String.class).getStatusCode());
		}
		return estados;
	}

}
