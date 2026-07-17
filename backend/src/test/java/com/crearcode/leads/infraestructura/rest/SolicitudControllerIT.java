package com.crearcode.leads.infraestructura.rest;

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
class SolicitudControllerIT {

	@Autowired
	private TestRestTemplate restTemplate;

	private SolicitudRequest solicitudValida() {
		return new SolicitudRequest("Juan Pérez", "Empresa S.A.S.", "nombre@empresa.com", "3001234567",
				com.crearcode.leads.dominio.ServicioDeInteres.IA_Y_AUTOMATIZACION,
				"Quiero automatizar mi negocio", true);
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
				"3001234567", com.crearcode.leads.dominio.ServicioDeInteres.OTRO, "mensaje", true);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void registrarConCorreoConFormatoInvalidoDevuelve400() {
		SolicitudRequest invalida = new SolicitudRequest("Juan Pérez", null, "esto-no-es-un-correo",
				"3001234567", com.crearcode.leads.dominio.ServicioDeInteres.OTRO, "mensaje", true);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elEndpointDeRegistroEsPublicoSinAutenticacion() {
		// Si no fuera publico, un payload invalido devolveria 401/403 en vez
		// de 400: confirma que la ruta esta permitAll para POST.
		SolicitudRequest invalida = new SolicitudRequest("", null, "x", "x",
				com.crearcode.leads.dominio.ServicioDeInteres.OTRO, "", false);

		ResponseEntity<String> respuesta = restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

}
