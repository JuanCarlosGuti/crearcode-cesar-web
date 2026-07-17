package com.crearcode.leads.infraestructura.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SeguridadAdminIT {

	private static final String USUARIO = "admin-test";
	private static final String CONTRASENA = "clave-test-segura";

	@DynamicPropertySource
	static void credencialesAdmin(DynamicPropertyRegistry registry) {
		registry.add("app.admin.username", () -> USUARIO);
		registry.add("app.admin.password", () -> CONTRASENA);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void solicitaAutenticacionSinCredenciales() {
		ResponseEntity<String> respuesta = restTemplate.getForEntity("/api/solicitudes", String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void permiteAccederConCredencialesCorrectas() {
		ResponseEntity<String> respuesta = restTemplate
				.withBasicAuth(USUARIO, CONTRASENA)
				.getForEntity("/api/solicitudes", String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void rechazaCredencialesIncorrectas() {
		ResponseEntity<String> respuesta = restTemplate
				.withBasicAuth(USUARIO, "clave-incorrecta")
				.getForEntity("/api/solicitudes", String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void elRegistroPublicoNoRequiereAutenticacion() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", null, String.class);

		assertThat(respuesta.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
	}

}
