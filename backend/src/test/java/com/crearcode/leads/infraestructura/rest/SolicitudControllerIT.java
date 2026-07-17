package com.crearcode.leads.infraestructura.rest;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
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

	private static final String ADMIN_USUARIO = "admin";
	private static final String ADMIN_CONTRASENA = "cambiar-en-produccion";

	@Autowired
	private TestRestTemplate restTemplate;

	private SolicitudRequest solicitudValida() {
		return new SolicitudRequest("Juan Pérez", "Empresa S.A.S.", "nombre@empresa.com", "3001234567",
				ServicioDeInteres.IA_Y_AUTOMATIZACION, "Quiero automatizar mi negocio", true);
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
				"3001234567", ServicioDeInteres.OTRO, "mensaje", true);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void registrarConCorreoConFormatoInvalidoDevuelve400() {
		SolicitudRequest invalida = new SolicitudRequest("Juan Pérez", null, "esto-no-es-un-correo",
				"3001234567", ServicioDeInteres.OTRO, "mensaje", true);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elEndpointDeRegistroEsPublicoSinAutenticacion() {
		// Si no fuera publico, un payload invalido devolveria 401/403 en vez
		// de 400: confirma que la ruta esta permitAll para POST.
		SolicitudRequest invalida = new SolicitudRequest("", null, "x", "x",
				ServicioDeInteres.OTRO, "", false);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void listarDevuelveLaSolicitudRecienRegistradaConSusDatos() {
		restTemplate.postForEntity("/api/solicitudes", solicitudValida(), SolicitudCreadaResponse.class);

		ResponseEntity<SolicitudResponse[]> respuesta = restTemplate
				.withBasicAuth(ADMIN_USUARIO, ADMIN_CONTRASENA)
				.getForEntity("/api/solicitudes", SolicitudResponse[].class);

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

		ResponseEntity<SolicitudResponse[]> respuesta = restTemplate
				.withBasicAuth(ADMIN_USUARIO, ADMIN_CONTRASENA)
				.getForEntity("/api/solicitudes?estado=DESCARTADA", SolicitudResponse[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).allSatisfy(
				solicitud -> assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.DESCARTADA));
	}

	private UUID registrarYObtenerId() {
		return restTemplate.postForEntity("/api/solicitudes", solicitudValida(), SolicitudCreadaResponse.class)
				.getBody()
				.id();
	}

	@Test
	void cambiarEstadoConTransicionValidaDevuelve204() {
		UUID id = registrarYObtenerId();

		ResponseEntity<Void> respuesta = restTemplate
				.withBasicAuth(ADMIN_USUARIO, ADMIN_CONTRASENA)
				.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
						new HttpEntity<>(new CambiarEstadoRequest(EstadoSolicitud.CONTACTADA)), Void.class, id);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void cambiarEstadoConIdInexistenteDevuelve404() {
		ResponseEntity<String> respuesta = restTemplate
				.withBasicAuth(ADMIN_USUARIO, ADMIN_CONTRASENA)
				.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
						new HttpEntity<>(new CambiarEstadoRequest(EstadoSolicitud.CONTACTADA)), String.class,
						UUID.randomUUID());

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void cambiarEstadoConTransicionInvalidaDevuelve409() {
		UUID id = registrarYObtenerId();

		ResponseEntity<String> respuesta = restTemplate
				.withBasicAuth(ADMIN_USUARIO, ADMIN_CONTRASENA)
				.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
						new HttpEntity<>(new CambiarEstadoRequest(EstadoSolicitud.CONVERTIDA)), String.class, id);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void cambiarEstadoSinAutenticacionDevuelve401() {
		UUID id = registrarYObtenerId();

		ResponseEntity<String> respuesta = restTemplate.exchange("/api/solicitudes/{id}/estado", HttpMethod.PATCH,
				new HttpEntity<>(new CambiarEstadoRequest(EstadoSolicitud.CONTACTADA)), String.class, id);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

}
