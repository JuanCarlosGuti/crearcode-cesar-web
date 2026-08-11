package com.crearcode.leads.infraestructura.rest;

import java.math.BigDecimal;
import java.util.List;
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
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.UsuarioRepositorio;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.ClienteRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.CotizacionResponse;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.ItemRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.NuevoBorradorRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La vista del cliente (HU-46). Lo importante aquí no es el camino
 * feliz, sino que **un cliente no pueda ver ni responder la cotización
 * de otro** (invariante 6): la comprobación es de servidor y responde
 * 404, sin revelar que el recurso existe.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class MisCotizacionesControllerIT {

	private static final String ADMIN_USUARIO = "admin@crearcode-cesar.local";
	private static final String ADMIN_CONTRASENA = "cambiar-en-produccion";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private com.crearcode.leads.dominio.GeneradorDeToken generadorDeToken;

	private static String tokenAdminCacheado;

	private HttpHeaders headersAdmin() {
		if (tokenAdminCacheado == null) {
			tokenAdminCacheado = restTemplate.postForEntity("/api/auth/login",
					new LoginRequest(ADMIN_USUARIO, ADMIN_CONTRASENA), LoginResponse.class).getBody().token();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(tokenAdminCacheado);
		return headers;
	}

	private HttpHeaders headersDe(String correo) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(ClienteDePrueba.registrarYAutenticar(usuarios, generadorDeToken, correo));
		return headers;
	}

	/** Cotización ya enviada y dirigida a ese correo. */
	private CotizacionResponse cotizacionEnviadaPara(String correoDelCliente) {
		CotizacionResponse creada = restTemplate.exchange("/api/cotizaciones", HttpMethod.POST,
				new HttpEntity<>(new NuevoBorradorRequest(null,
						new ClienteRequest("Panaderia El Trigal", correoDelCliente, null, null), 19, 15, null,
						List.of(new ItemRequest("Desarrollo", 1, new BigDecimal("5000000")))), headersAdmin()),
				CotizacionResponse.class).getBody();

		return restTemplate.exchange("/api/cotizaciones/" + creada.id() + "/envio", HttpMethod.POST,
				new HttpEntity<>(null, headersAdmin()), CotizacionResponse.class).getBody();
	}

	@Test
	void elClienteVeSoloSusCotizaciones() {
		String suyo = "mia@correo-de-prueba.com";
		cotizacionEnviadaPara(suyo);
		cotizacionEnviadaPara("de-otro@correo-de-prueba.com");

		ResponseEntity<CotizacionResponse[]> respuesta = restTemplate.exchange("/api/mis-cotizaciones",
				HttpMethod.GET, new HttpEntity<>(headersDe(suyo)), CotizacionResponse[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotEmpty();
		assertThat(respuesta.getBody()).allMatch(c -> c.clienteCorreo().equalsIgnoreCase(suyo));
	}

	@Test
	void elClienteAceptaSuCotizacion() {
		String correo = "acepta@correo-de-prueba.com";
		CotizacionResponse enviada = cotizacionEnviadaPara(correo);

		ResponseEntity<CotizacionResponse> respuesta = restTemplate.exchange(
				"/api/mis-cotizaciones/" + enviada.id() + "/aceptacion", HttpMethod.POST,
				new HttpEntity<>(null, headersDe(correo)), CotizacionResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody().estado()).isEqualTo(EstadoCotizacion.ACEPTADA);
		assertThat(respuesta.getBody().respondidaEn()).isNotNull();
	}

	@Test
	void elClienteRechazaSuCotizacion() {
		String correo = "rechaza@correo-de-prueba.com";
		CotizacionResponse enviada = cotizacionEnviadaPara(correo);

		ResponseEntity<CotizacionResponse> respuesta = restTemplate.exchange(
				"/api/mis-cotizaciones/" + enviada.id() + "/rechazo", HttpMethod.POST,
				new HttpEntity<>(null, headersDe(correo)), CotizacionResponse.class);

		assertThat(respuesta.getBody().estado()).isEqualTo(EstadoCotizacion.RECHAZADA);
	}

	@Test
	void elClienteDescargaElPdfDeSuCotizacion() {
		String correo = "descarga@correo-de-prueba.com";
		CotizacionResponse enviada = cotizacionEnviadaPara(correo);

		ResponseEntity<byte[]> respuesta = restTemplate.exchange(
				"/api/mis-cotizaciones/" + enviada.id() + "/documento", HttpMethod.GET,
				new HttpEntity<>(headersDe(correo)), byte[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(new String(respuesta.getBody(), 0, 5)).isEqualTo("%PDF-");
	}

	// El corazon de la HU-46: acceso cruzado entre clientes.
	@Test
	void unClienteNoPuedeVerLaCotizacionDeOtro() {
		CotizacionResponse ajena = cotizacionEnviadaPara("dueno@correo-de-prueba.com");
		HttpEntity<Void> comoIntruso = new HttpEntity<>(headersDe("intruso@correo-de-prueba.com"));

		assertThat(restTemplate.exchange("/api/mis-cotizaciones/" + ajena.id(), HttpMethod.GET,
				comoIntruso, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(restTemplate.exchange("/api/mis-cotizaciones/" + ajena.id() + "/documento",
				HttpMethod.GET, comoIntruso, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void unClienteNoPuedeResponderLaCotizacionDeOtro() {
		CotizacionResponse ajena = cotizacionEnviadaPara("dueno2@correo-de-prueba.com");
		HttpEntity<Void> comoIntruso = new HttpEntity<>(headersDe("intruso2@correo-de-prueba.com"));

		ResponseEntity<String> intento = restTemplate.exchange(
				"/api/mis-cotizaciones/" + ajena.id() + "/aceptacion", HttpMethod.POST, comoIntruso,
				String.class);

		assertThat(intento.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		// Y sigue intacta para su dueño.
		ResponseEntity<CotizacionResponse> comoDueno = restTemplate.exchange(
				"/api/mis-cotizaciones/" + ajena.id(), HttpMethod.GET,
				new HttpEntity<>(headersDe("dueno2@correo-de-prueba.com")), CotizacionResponse.class);
		assertThat(comoDueno.getBody().estado()).isEqualTo(EstadoCotizacion.ENVIADA);
	}

	@Test
	void responderDosVecesEsUnConflicto() {
		String correo = "dosveces@correo-de-prueba.com";
		CotizacionResponse enviada = cotizacionEnviadaPara(correo);
		restTemplate.exchange("/api/mis-cotizaciones/" + enviada.id() + "/aceptacion", HttpMethod.POST,
				new HttpEntity<>(null, headersDe(correo)), CotizacionResponse.class);

		ResponseEntity<String> segundoIntento = restTemplate.exchange(
				"/api/mis-cotizaciones/" + enviada.id() + "/rechazo", HttpMethod.POST,
				new HttpEntity<>(null, headersDe(correo)), String.class);

		assertThat(segundoIntento.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void sinTokenNoSeVeNada() {
		assertThat(restTemplate.getForEntity("/api/mis-cotizaciones", String.class).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void unaCotizacionInexistenteDevuelve404() {
		ResponseEntity<String> respuesta = restTemplate.exchange(
				"/api/mis-cotizaciones/" + UUID.randomUUID(), HttpMethod.GET,
				new HttpEntity<>(headersDe("cualquiera@correo-de-prueba.com")), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

}
