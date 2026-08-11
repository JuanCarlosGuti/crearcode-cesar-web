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
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.ClienteRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.CotizacionResponse;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.EditarBorradorRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.ItemRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.NuevoBorradorRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API del equipo (HU-44, HU-45, HU-47). Verifica también que un token de
 * CLIENTE no pueda tocar la gestión: el panel es solo del ADMIN.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CotizacionControllerIT {

	private static final String ADMIN_USUARIO = "admin@crearcode-cesar.local";
	private static final String ADMIN_CONTRASENA = "cambiar-en-produccion";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private com.crearcode.leads.dominio.UsuarioRepositorio usuarios;

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

	private NuevoBorradorRequest borradorValido(String correoDelCliente) {
		return new NuevoBorradorRequest(null,
				new ClienteRequest("Panaderia El Trigal", correoDelCliente, "3001234567", "900123456-1"),
				19, 15, "Incluye capacitacion",
				List.of(new ItemRequest("Desarrollo", 2, new BigDecimal("2500000"))));
	}

	private CotizacionResponse abrirBorrador(String correoDelCliente) {
		ResponseEntity<CotizacionResponse> respuesta = restTemplate.exchange("/api/cotizaciones",
				HttpMethod.POST, new HttpEntity<>(borradorValido(correoDelCliente), headersAdmin()),
				CotizacionResponse.class);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		return respuesta.getBody();
	}

	@Test
	void abrirUnBorradorDevuelve201ConLosTotalesCalculados() {
		CotizacionResponse creada = abrirBorrador("cliente1@correo-de-prueba.com");

		assertThat(creada.estado()).isEqualTo(EstadoCotizacion.BORRADOR);
		assertThat(creada.numero()).isNull();
		// 2 x 2.500.000 = 5.000.000, IVA 19% = 950.000
		assertThat(creada.subtotal()).isEqualByComparingTo("5000000");
		assertThat(creada.impuesto()).isEqualByComparingTo("950000");
		assertThat(creada.total()).isEqualByComparingTo("5950000");
	}

	@Test
	void editarUnBorradorReemplazaSusItems() {
		CotizacionResponse creada = abrirBorrador("cliente2@correo-de-prueba.com");

		ResponseEntity<CotizacionResponse> respuesta = restTemplate.exchange(
				"/api/cotizaciones/" + creada.id(), HttpMethod.PUT,
				new HttpEntity<>(new EditarBorradorRequest(
						List.of(new ItemRequest("Solo analisis", 1, new BigDecimal("1000000"))), "Nueva nota"),
						headersAdmin()),
				CotizacionResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody().items()).hasSize(1);
		assertThat(respuesta.getBody().total()).isEqualByComparingTo("1190000");
		assertThat(respuesta.getBody().notas()).isEqualTo("Nueva nota");
	}

	@Test
	void enviarAsignaElNumeroYCongelaLaCotizacion() {
		CotizacionResponse creada = abrirBorrador("cliente3@correo-de-prueba.com");

		ResponseEntity<CotizacionResponse> envio = restTemplate.exchange(
				"/api/cotizaciones/" + creada.id() + "/envio", HttpMethod.POST,
				new HttpEntity<>(null, headersAdmin()), CotizacionResponse.class);

		assertThat(envio.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(envio.getBody().estado()).isEqualTo(EstadoCotizacion.ENVIADA);
		assertThat(envio.getBody().numero()).matches("COT-\\d{4}-\\d{4,}");

		// Ya enviada, editarla es un conflicto de negocio, no un 500.
		ResponseEntity<String> edicion = restTemplate.exchange("/api/cotizaciones/" + creada.id(),
				HttpMethod.PUT,
				new HttpEntity<>(new EditarBorradorRequest(
						List.of(new ItemRequest("Otro", 1, new BigDecimal("1"))), null), headersAdmin()),
				String.class);
		assertThat(edicion.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void listarPermiteFiltrarPorEstado() {
		abrirBorrador("cliente4@correo-de-prueba.com");

		ResponseEntity<CotizacionResponse[]> respuesta = restTemplate.exchange(
				"/api/cotizaciones?estado=BORRADOR", HttpMethod.GET, new HttpEntity<>(headersAdmin()),
				CotizacionResponse[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody()).isNotEmpty();
		assertThat(respuesta.getBody()).allMatch(c -> c.estado() == EstadoCotizacion.BORRADOR);
	}

	@Test
	void descargarDevuelveElPdf() {
		CotizacionResponse creada = abrirBorrador("cliente5@correo-de-prueba.com");

		ResponseEntity<byte[]> respuesta = restTemplate.exchange(
				"/api/cotizaciones/" + creada.id() + "/documento", HttpMethod.GET,
				new HttpEntity<>(headersAdmin()), byte[].class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
		assertThat(respuesta.getBody()).isNotEmpty();
		assertThat(new String(respuesta.getBody(), 0, 5)).isEqualTo("%PDF-");
	}

	@Test
	void cancelarDevuelve204() {
		CotizacionResponse creada = abrirBorrador("cliente6@correo-de-prueba.com");

		ResponseEntity<Void> respuesta = restTemplate.exchange("/api/cotizaciones/" + creada.id(),
				HttpMethod.DELETE, new HttpEntity<>(headersAdmin()), Void.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	void unaCotizacionInexistenteDevuelve404() {
		ResponseEntity<String> respuesta = restTemplate.exchange("/api/cotizaciones/" + UUID.randomUUID(),
				HttpMethod.GET, new HttpEntity<>(headersAdmin()), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void unBorradorSinItemsNoSePuedeEnviar() {
		ResponseEntity<CotizacionResponse> creada = restTemplate.exchange("/api/cotizaciones",
				HttpMethod.POST,
				new HttpEntity<>(new NuevoBorradorRequest(null,
						new ClienteRequest("Sin items", "cliente7@correo-de-prueba.com", null, null),
						0, 15, null, List.of()), headersAdmin()),
				CotizacionResponse.class);

		ResponseEntity<String> envio = restTemplate.exchange(
				"/api/cotizaciones/" + creada.getBody().id() + "/envio", HttpMethod.POST,
				new HttpEntity<>(null, headersAdmin()), String.class);

		assertThat(envio.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void sinTokenNoSeAccedeALaGestion() {
		ResponseEntity<String> respuesta = restTemplate.getForEntity("/api/cotizaciones", String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	// El panel es del equipo: un CLIENTE autenticado no entra (misma
	// leccion que ISS-094 con las solicitudes).
	@Test
	void unTokenDeClienteNoPuedeGestionarCotizaciones() {
		String tokenCliente = ClienteDePrueba.registrarYAutenticar(usuarios, generadorDeToken,
				"gestion-ajena@correo-de-prueba.com");
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(tokenCliente);

		ResponseEntity<String> respuesta = restTemplate.exchange("/api/cotizaciones", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

}
