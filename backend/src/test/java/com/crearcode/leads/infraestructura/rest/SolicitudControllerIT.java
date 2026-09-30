package com.crearcode.leads.infraestructura.rest;

import java.util.Map;
import java.util.UUID;

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

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SolicitudControllerIT {

	private static final String ADMIN_USUARIO = "admin@crearcode-cesar.local";
	private static final String ADMIN_CONTRASENA = "cambiar-en-produccion";

	@Autowired
	private TestRestTemplate restTemplate;

	private SolicitudRequest solicitudValida() {
		return new SolicitudRequest("Juan Pérez", "Empresa S.A.S.", "nombre@empresa.com", "3001234567",
				ServicioDeInteres.IA_Y_AUTOMATIZACION, "Quiero automatizar mi negocio", true, false, null);
	}

	// Cacheado a nivel de clase: un admin real inicia sesion una vez y
	// reusa el token durante toda su sesion, no vuelve a loguearse antes
	// de cada llamada. Ademas de mas realista, evita chocar con el
	// rate-limit propio y estricto del login (ISS-060) al llamarlo desde
	// multiples metodos de test en la misma clase.
	private static String tokenAdminCacheado;

	private HttpEntity<Void> conBearerAdmin() {
		if (tokenAdminCacheado == null) {
			tokenAdminCacheado = restTemplate.postForEntity("/api/auth/login",
					new LoginRequest(ADMIN_USUARIO, ADMIN_CONTRASENA), LoginResponse.class).getBody().token();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(tokenAdminCacheado);
		return new HttpEntity<>(headers);
	}

	@Test
	void registrarConDatosValidosDevuelve201YElId() {
		ResponseEntity<SolicitudCreadaResponse> respuesta = restTemplate.postForEntity(
				"/api/solicitudes", solicitudValida(), SolicitudCreadaResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(respuesta.getBody()).isNotNull();
		assertThat(respuesta.getBody().id()).isNotNull();
	}

	@Test
	void registrarConNombreVacioDevuelve400() {
		SolicitudRequest invalida = new SolicitudRequest("", "Empresa S.A.S.", "nombre@empresa.com",
				"3001234567", ServicioDeInteres.OTRO, "mensaje", true, false, null);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void registrarConCorreoConFormatoInvalidoDevuelve400() {
		SolicitudRequest invalida = new SolicitudRequest("Juan Pérez", null, "esto-no-es-un-correo",
				"3001234567", ServicioDeInteres.OTRO, "mensaje", true, false, null);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elEndpointDeRegistroEsPublicoSinAutenticacion() {
		// Si no fuera publico, un payload invalido devolveria 401/403 en vez
		// de 400: confirma que la ruta esta permitAll para POST.
		SolicitudRequest invalida = new SolicitudRequest("", null, "x", "x",
				ServicioDeInteres.OTRO, "", false, false, null);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void registrarConHoneypotRellenoRespondeExitosoPeroNoRegistraNiNotifica() {
		SolicitudRequest conHoneypot = new SolicitudRequest("Bot Spam", null, "bot@spam.com", "3009999999",
				ServicioDeInteres.OTRO, "mensaje de spam", true, false, "http://sitio-de-spam.com");

		ResponseEntity<SolicitudCreadaResponse> respuesta = restTemplate.postForEntity(
				"/api/solicitudes", conHoneypot, SolicitudCreadaResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(respuesta.getBody()).isNotNull();

		ResponseEntity<SolicitudResponse[]> listado = restTemplate.exchange(
				"/api/solicitudes", HttpMethod.GET, conBearerAdmin(), SolicitudResponse[].class);
		assertThat(listado.getBody()).noneSatisfy(
				solicitud -> assertThat(solicitud.correo()).isEqualTo("bot@spam.com"));
	}

	@Test
	void listarDevuelveLaSolicitudRecienRegistradaConSusDatos() {
		restTemplate.postForEntity("/api/solicitudes", solicitudValida(), SolicitudCreadaResponse.class);

		ResponseEntity<SolicitudResponse[]> respuesta = restTemplate.exchange(
				"/api/solicitudes", HttpMethod.GET, conBearerAdmin(), SolicitudResponse[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotEmpty();
		assertThat(respuesta.getBody())
				.anySatisfy(solicitud -> {
					assertThat(solicitud.nombre()).isEqualTo("Juan Pérez");
					assertThat(solicitud.correo()).isEqualTo("nombre@empresa.com");
					assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.NUEVA);
				});
	}

	@Test
	void listarConFiltroPorEstadoDescartadaNoIncluyeSolicitudesNuevas() {
		restTemplate.postForEntity("/api/solicitudes", solicitudValida(), SolicitudCreadaResponse.class);

		ResponseEntity<SolicitudResponse[]> respuesta = restTemplate.exchange(
				"/api/solicitudes?estado=DESCARTADA", HttpMethod.GET, conBearerAdmin(), SolicitudResponse[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).allSatisfy(
				solicitud -> assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.DESCARTADA));
	}

	private UUID registrarYObtenerId() {
		return restTemplate.postForEntity("/api/solicitudes", solicitudValida(), SolicitudCreadaResponse.class)
				.getBody()
				.id();
	}

	private HttpEntity<CambiarEstadoRequest> conBearerAdminYCuerpo(CambiarEstadoRequest cuerpo) {
		HttpEntity<Void> autenticacion = conBearerAdmin();
		return new HttpEntity<>(cuerpo, autenticacion.getHeaders());
	}

	@Test
	void cambiarEstadoConTransicionValidaDevuelve204() {
		UUID id = registrarYObtenerId();

		ResponseEntity<Void> respuesta = restTemplate.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
				conBearerAdminYCuerpo(new CambiarEstadoRequest(EstadoSolicitud.CONTACTADA)), Void.class, id);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void cambiarEstadoConIdInexistenteDevuelve404() {
		ResponseEntity<String> respuesta = restTemplate.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
				conBearerAdminYCuerpo(new CambiarEstadoRequest(EstadoSolicitud.CONTACTADA)), String.class,
				UUID.randomUUID());

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void cambiarEstadoConTransicionInvalidaDevuelve409() {
		UUID id = registrarYObtenerId();

		ResponseEntity<String> respuesta = restTemplate.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
				conBearerAdminYCuerpo(new CambiarEstadoRequest(EstadoSolicitud.CONVERTIDA)), String.class, id);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void cambiarEstadoSinAutenticacionDevuelve401() {
		UUID id = registrarYObtenerId();

		ResponseEntity<String> respuesta = restTemplate.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
				new HttpEntity<>(new CambiarEstadoRequest(EstadoSolicitud.CONTACTADA)), String.class, id);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}


	/**
	 * La politica v2 (§13) promete que la finalidad comercial tiene su
	 * propia casilla. Si la API las mezclara, no habria forma de saber a
	 * quien se le puede escribir sin revisar correos viejos — y la
	 * promesa seria falsa.
	 */
	@Test
	void laAutorizacionComercialViajaAparteYNoSeDaPorSupuesta() {
		SolicitudRequest sinComercial = solicitudValida();
		SolicitudRequest conComercial = new SolicitudRequest("Ana Gómez", null, "ana@empresa.com",
				"3009876543", ServicioDeInteres.OTRO, "Quiero novedades", true, true, null);

		UUID idSinComercial = restTemplate
				.postForEntity("/api/solicitudes", sinComercial, SolicitudCreadaResponse.class)
				.getBody().id();
		UUID idConComercial = restTemplate
				.postForEntity("/api/solicitudes", conComercial, SolicitudCreadaResponse.class)
				.getBody().id();

		assertThat(idSinComercial).isNotNull();
		assertThat(idConComercial).isNotNull();
		assertThat(idSinComercial).isNotEqualTo(idConComercial);
	}


	/**
	 * La casilla comercial es OPCIONAL, y un cliente que no la manda
	 * tiene que poder seguir registrando su solicitud. No es un caso
	 * teorico: las e2e lo hacen, un navegador con el JavaScript viejo en
	 * cache lo haria el dia del despliegue, y cualquier integracion
	 * externa tambien. Jackson 3 no convierte un boolean PRIMITIVO ausente
	 * en false: lo rechaza, y la solicitud se pierde con un 400.
	 *
	 * El IT de arriba no lo atrapaba porque arma la peticion en Java CON
	 * el campo. Este manda JSON crudo, que es lo que llega de verdad.
	 */
	@Test
	void unClienteQueNoMandaLaCasillaComercialSigueRegistrandoSuSolicitud() {
		Map<String, Object> sinCasillaComercial = Map.of(
				"nombre", "Ana Perez",
				"correo", "ana-sin-casilla@correo-de-prueba.com",
				"telefono", "3001234567",
				"servicioDeInteres", "OTRO",
				"mensaje", "Necesito una app de pedidos",
				"aceptaConsentimiento", true);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", sinCasillaComercial,
				String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(respuesta.getBody()).contains("\"id\"");
	}

}
