package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;
import java.util.Map;

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
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AuthControllerIT {

	private static final String ADMIN_USUARIO = "admin@crearcode-cesar.local";
	private static final String ADMIN_CONTRASENA = "cambiar-en-produccion";
	private static final String CONTRASENA_VALIDA = "contrasena-segura";

	/**
	 * Esta clase ejercita el login a propósito muchas veces (flujos de
	 * verificación y restablecimiento incluidos), así que relaja el límite
	 * estricto de producción (5/15 min por IP). El límite real tiene su
	 * propio IT dedicado ({@code RateLimitingFilterIT}).
	 */
	@DynamicPropertySource
	static void relajarRateLimitDeLogin(DynamicPropertyRegistry registry) {
		registry.add("app.rate-limit.login.max-intentos", () -> 1000);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private TokenDeUsuarioRepositorio tokens;

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
	void loginDevuelveElRolYElCorreoDeLaSesion() {
		ResponseEntity<LoginResponse> respuesta = restTemplate.postForEntity(
				"/api/auth/login", new LoginRequest(ADMIN_USUARIO, ADMIN_CONTRASENA), LoginResponse.class);

		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().rol()).isEqualTo(Rol.ADMIN);
		assertThat(respuesta.getBody().correo()).isEqualTo(ADMIN_USUARIO);
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

	@Test
	void registroConDatosValidosDevuelve201() {
		ResponseEntity<Void> respuesta = registrar("registro-nuevo@correo-de-prueba.com");

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	void registroConCorreoDuplicadoDevuelve409() {
		registrar("registro-duplicado@correo-de-prueba.com");

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/auth/registro",
				Map.of("correo", "registro-duplicado@correo-de-prueba.com", "contrasena", CONTRASENA_VALIDA),
				String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void registroConContrasenaCortaDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/auth/registro",
				Map.of("correo", "registro-corto@correo-de-prueba.com", "contrasena", "corta"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(respuesta.getBody()).contains("al menos 10 caracteres");
	}

	@Test
	void registroConCorreoInvalidoDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/auth/registro",
				Map.of("correo", "esto-no-es-un-correo", "contrasena", CONTRASENA_VALIDA), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void loginDeClienteSinVerificarDevuelve403() {
		registrar("cliente-sin-verificar@correo-de-prueba.com");

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/auth/login",
				new LoginRequest("cliente-sin-verificar@correo-de-prueba.com", CONTRASENA_VALIDA), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(respuesta.getBody()).contains("no está verificada");
	}

	@Test
	void verificacionConTokenInvalidoDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/auth/verificacion",
				Map.of("token", "token-que-no-existe"), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(respuesta.getBody()).contains("inválido o ya venció");
	}

	@Test
	void elFlujoDeVerificacionDejaIniciarSesionComoCliente() {
		registrar("cliente-verificado@correo-de-prueba.com");
		String tokenEnClaro = generarTokenPara("cliente-verificado@correo-de-prueba.com",
				PropositoDeToken.VERIFICACION);

		ResponseEntity<Void> verificacion = restTemplate.postForEntity("/api/auth/verificacion",
				Map.of("token", tokenEnClaro), Void.class);
		assertThat(verificacion.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<LoginResponse> login = restTemplate.postForEntity("/api/auth/login",
				new LoginRequest("cliente-verificado@correo-de-prueba.com", CONTRASENA_VALIDA), LoginResponse.class);
		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(login.getBody()).isNotNull();
		assertThat(login.getBody().rol()).isEqualTo(Rol.CLIENTE);
		assertThat(login.getBody().correo()).isEqualTo("cliente-verificado@correo-de-prueba.com");
	}

	@Test
	void elReenvioDeVerificacionRespondeGenericoAunSiElCorreoNoExiste() {
		ResponseEntity<Void> respuesta = restTemplate.postForEntity("/api/auth/reenvio-verificacion",
				Map.of("correo", "no-existe@correo-de-prueba.com"), Void.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
	}

	@Test
	void laRecuperacionRespondeGenericoAunSiElCorreoNoExiste() {
		ResponseEntity<Void> respuesta = restTemplate.postForEntity("/api/auth/recuperacion",
				Map.of("correo", "no-existe@correo-de-prueba.com"), Void.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
	}

	@Test
	void restablecimientoConTokenInvalidoDevuelve400() {
		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/auth/restablecimiento",
				Map.of("token", "token-que-no-existe", "contrasena", CONTRASENA_VALIDA), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(respuesta.getBody()).contains("inválido o ya venció");
	}

	@Test
	void elFlujoDeRestablecimientoCambiaLaContrasenaYVerificaLaCuenta() {
		registrar("cliente-recupera@correo-de-prueba.com");
		String tokenEnClaro = generarTokenPara("cliente-recupera@correo-de-prueba.com",
				PropositoDeToken.RECUPERACION);

		ResponseEntity<Void> restablecimiento = restTemplate.postForEntity("/api/auth/restablecimiento",
				Map.of("token", tokenEnClaro, "contrasena", "contrasena-nueva-larga"), Void.class);
		assertThat(restablecimiento.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<LoginResponse> login = restTemplate.postForEntity("/api/auth/login",
				new LoginRequest("cliente-recupera@correo-de-prueba.com", "contrasena-nueva-larga"),
				LoginResponse.class);
		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	private ResponseEntity<Void> registrar(String correo) {
		return restTemplate.postForEntity("/api/auth/registro",
				Map.of("correo", correo, "contrasena", CONTRASENA_VALIDA), Void.class);
	}

	/**
	 * Genera el token directamente contra los puertos de dominio: el valor
	 * en claro solo viaja por correo y estos tests no levantan SMTP (el
	 * flujo completo con el enlace real del correo lo cubre el e2e de
	 * cuentas con Mailpit, ISS-099).
	 */
	private String generarTokenPara(String correo, PropositoDeToken proposito) {
		Usuario usuario = usuarios.buscarPorCorreo(new Correo(correo)).orElseThrow();
		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(usuario.id(), proposito, Instant.now());
		tokens.guardar(generado.token());
		return generado.valorEnClaro();
	}

}
