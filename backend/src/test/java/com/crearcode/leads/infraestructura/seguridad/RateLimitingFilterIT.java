package com.crearcode.leads.infraestructura.seguridad;

import java.util.ArrayList;
import java.util.List;

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

	@DynamicPropertySource
	static void umbralDePrueba(DynamicPropertyRegistry registry) {
		registry.add("app.rate-limit.max-solicitudes", () -> MAX_SOLICITUDES_EN_ESTE_TEST);
		registry.add("app.rate-limit.ventana-minutos", () -> 10);
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

}
