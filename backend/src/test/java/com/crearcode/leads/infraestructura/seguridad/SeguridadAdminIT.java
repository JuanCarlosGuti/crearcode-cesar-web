package com.crearcode.leads.infraestructura.seguridad;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SeguridadAdminIT {

	private static final String CORREO = "admin-test@crearcode-cesar-test.local";
	private static final String CONTRASENA = "clave-test-segura";

	@DynamicPropertySource
	static void credencialesAdmin(DynamicPropertyRegistry registry) {
		registry.add("app.admin.username", () -> CORREO);
		registry.add("app.admin.password", () -> CONTRASENA);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	private record TokenResponse(String token) {
	}

	private String iniciarSesionYObtenerToken() {
		ResponseEntity<TokenResponse> respuesta = restTemplate.postForEntity(
				"/api/auth/login", Map.of("correo", CORREO, "contrasena", CONTRASENA), TokenResponse.class);
		return respuesta.getBody().token();
	}

	private HttpEntity<Void> conBearer(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return new HttpEntity<>(headers);
	}

	@Test
	void solicitaAutenticacionSinCredenciales() {
		ResponseEntity<String> respuesta = restTemplate.getForEntity("/api/solicitudes", String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void permiteAccederConUnTokenValido() {
		String token = iniciarSesionYObtenerToken();

		ResponseEntity<String> respuesta = restTemplate.exchange(
				"/api/solicitudes", HttpMethod.GET, conBearer(token), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void rechazaCredencialesIncorrectasEnElLogin() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity(
				"/api/auth/login", Map.of("correo", CORREO, "contrasena", "clave-incorrecta"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void rechazaUnTokenInvalido() {
		ResponseEntity<String> respuesta = restTemplate.exchange(
				"/api/solicitudes", HttpMethod.GET, conBearer("token-invalido"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void elRegistroPublicoNoRequiereAutenticacion() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", null, String.class);

		assertThat(respuesta.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private CifradorDeContrasenas cifrador;

	/**
	 * El panel admin exige el rol ADMIN, no basta con estar autenticado:
	 * desde la fase F8 existen tokens de CLIENTE y un cliente registrado
	 * no debe poder leer ni tocar las solicitudes de otros (ISS-094).
	 */
	@Test
	void unTokenDeClienteRecibe403EnElPanelAdmin() {
		String correoCliente = "cliente-seguridad@correo-de-prueba.com";
		String contrasena = "clave-de-cliente";
		usuarios.guardar(Usuario.registrarCliente(new Correo(correoCliente), cifrador.hash(contrasena)).verificar());
		ResponseEntity<TokenResponse> login = restTemplate.postForEntity(
				"/api/auth/login", Map.of("correo", correoCliente, "contrasena", contrasena), TokenResponse.class);
		String tokenCliente = login.getBody().token();

		ResponseEntity<String> listado = restTemplate.exchange(
				"/api/solicitudes", HttpMethod.GET, conBearer(tokenCliente), String.class);
		ResponseEntity<String> cambioDeEstado = restTemplate.exchange(
				"/api/solicitudes/00000000-0000-0000-0000-000000000000/estado", HttpMethod.PATCH,
				new HttpEntity<>(Map.of("estado", "EN_REVISION"), conBearer(tokenCliente).getHeaders()), String.class);

		assertThat(listado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(cambioDeEstado.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

}
