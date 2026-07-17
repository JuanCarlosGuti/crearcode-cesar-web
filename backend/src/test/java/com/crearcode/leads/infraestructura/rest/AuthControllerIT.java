package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.crearcode.leads.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AuthControllerIT {

	private static final String ADMIN_USUARIO = "admin@crearcode-cesar.local";
	private static final String ADMIN_CONTRASENA = "cambiar-en-produccion";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void loginConCredencialesCorrectasDevuelveUnToken() {
		ResponseEntity<LoginResponse> respuesta = restTemplate.postForEntity(
				"/api/auth/login", new LoginRequest(ADMIN_USUARIO, ADMIN_CONTRASENA), LoginResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().token()).isNotBlank();
		assertThat(respuesta.getBody().expiraEn()).isAfter(Instant.now());
	}

	@Test
	void loginConContrasenaIncorrectaDevuelve401ConMensajeGenerico() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity(
				"/api/auth/login", new LoginRequest(ADMIN_USUARIO, "clave-incorrecta"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(respuesta.getBody()).contains("Correo o contraseña incorrectos");
	}

	@Test
	void loginConCorreoInexistenteDevuelveElMismoMensajeGenerico() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity(
				"/api/auth/login", new LoginRequest("no-existe@crearcode-cesar.local", "cualquiera"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(respuesta.getBody()).contains("Correo o contraseña incorrectos");
	}

	@Test
	void loginSinCorreoDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity(
				"/api/auth/login", new LoginRequest("", ADMIN_CONTRASENA), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

}
