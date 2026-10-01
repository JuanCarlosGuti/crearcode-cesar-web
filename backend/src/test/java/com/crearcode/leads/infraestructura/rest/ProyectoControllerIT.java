package com.crearcode.leads.infraestructura.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.UsuarioRepositorio;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.CambioDeEstadoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ClienteDelProyectoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.DescripcionRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.DemoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.EntregableRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.EntregableResponse;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.FaseRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.NuevaFaseRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.NuevoProyectoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.PagoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ProyectoResponse;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ProyectoResumenResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API del equipo (HU-53 a HU-56). Todo exige rol ADMIN: un token de
 * cliente no toca la gestión, y la vista del cliente vive en
 * {@code /api/mis-proyectos}.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProyectoControllerIT {

	private static final String ADMIN = "admin@crearcode-cesar.local";

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private GeneradorDeToken generadorDeToken;

	@Autowired
	private CotizacionRepositorio cotizaciones;

	/**
	 * Token del admin sembrado, emitido con el generador real. No pasa
	 * por el login para no gastar su rate limit (5 intentos por IP).
	 */
	private HttpHeaders admin() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(generadorDeToken
				.generar(usuarios.buscarPorCorreo(new Correo(ADMIN)).orElseThrow(), Instant.now()).token());
		return headers;
	}

	private HttpHeaders cliente(String correo) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(ClienteDePrueba.registrarYAutenticar(usuarios, generadorDeToken, correo));
		return headers;
	}

	private static DescripcionRequest descripcion() {
		return new DescripcionRequest("Tienda de Café Valle", "Tu tienda para vender café", LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 12, 15));
	}

	private static NuevaFaseRequest faseConDosEntregables() {
		return new NuevaFaseRequest(new FaseRequest("Fase 1", "Que veas el diseño", null, null), List.of(
				new EntregableRequest("Diseño", "Logo y colores", new BigDecimal("1000000"),
						MomentoDeCobro.AL_INICIAR),
				new EntregableRequest("Catálogo", null, new BigDecimal("3000000"), MomentoDeCobro.AL_APROBAR)));
	}

	private <T> ResponseEntity<T> pedir(HttpMethod metodo, String ruta, Object cuerpo, HttpHeaders headers,
			Class<T> tipo) {
		return restTemplate.exchange(ruta, metodo, new HttpEntity<>(cuerpo, headers), tipo);
	}

	private ProyectoResponse crearEnBlanco(String correoDelCliente) {
		ResponseEntity<ProyectoResponse> respuesta = pedir(HttpMethod.POST, "/api/proyectos",
				new NuevoProyectoRequest(null, new ClienteDelProyectoRequest("Café Valle S.A.S.", correoDelCliente),
						19, descripcion(), List.of(faseConDosEntregables())),
				admin(), ProyectoResponse.class);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		return respuesta.getBody();
	}

	private static UUID idDe(ProyectoResponse proyecto, String nombre) {
		return proyecto.fases().stream().flatMap(fase -> fase.entregables().stream())
				.filter(entregable -> entregable.nombre().equals(nombre)).findFirst().orElseThrow().id();
	}

	private ProyectoResponse cambiarEstado(ProyectoResponse proyecto, UUID entregable, EstadoEntregable estado) {
		ResponseEntity<ProyectoResponse> respuesta = pedir(HttpMethod.POST,
				"/api/proyectos/" + proyecto.id() + "/entregables/" + entregable + "/estado",
				new CambioDeEstadoRequest(estado, null), admin(), ProyectoResponse.class);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		return respuesta.getBody();
	}

	@Test
	void crearUnProyectoEnBlancoDevuelve201ConElDineroCalculado() {
		ProyectoResponse creado = crearEnBlanco("sin-cuenta@ejemplo.co");

		assertThat(creado.estado()).isEqualTo(EstadoProyecto.ACTIVO);
		assertThat(creado.avance()).isZero();
		// 1 M + 3 M, con IVA del 19 %: 4.760.000. El anticipo ya es cobrable.
		assertThat(creado.total()).isEqualByComparingTo("4760000");
		assertThat(creado.pendienteDePago()).isEqualByComparingTo("1190000");
		assertThat(creado.saldo()).isEqualByComparingTo("4760000");
		EntregableResponse diseno = creado.fases().getFirst().entregables().getFirst();
		assertThat(diseno.impuesto()).isEqualByComparingTo("190000");
		assertThat(diseno.cobro()).isEqualByComparingTo("1190000");
		assertThat(diseno.esCobrable()).isTrue();
		// HU-53: el panel avisa que el correo todavía no tiene cuenta.
		assertThat(creado.clienteTieneCuenta()).isFalse();
	}

	@Test
	void elFlujoDeEntregasYPagosSeReflejaEnElSaldo() {
		ProyectoResponse proyecto = crearEnBlanco("flujo@ejemplo.co");
		UUID diseno = idDe(proyecto, "Diseño");

		cambiarEstado(proyecto, diseno, EstadoEntregable.EN_CURSO);
		cambiarEstado(proyecto, diseno, EstadoEntregable.EN_REVISION);
		ProyectoResponse aprobado = cambiarEstado(proyecto, diseno, EstadoEntregable.APROBADO);
		assertThat(aprobado.avance()).isEqualTo(25);

		ResponseEntity<ProyectoResponse> pagado = pedir(HttpMethod.POST, "/api/proyectos/" + proyecto.id() + "/pagos",
				new PagoRequest(diseno, new BigDecimal("590000"), LocalDate.of(2026, 9, 2), MedioDePago.NEQUI_DAVIPLATA,
						"Comprobante 123"),
				admin(), ProyectoResponse.class);

		assertThat(pagado.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(pagado.getBody().totalPagado()).isEqualByComparingTo("590000");
		assertThat(pagado.getBody().saldo()).isEqualByComparingTo("4170000");
		assertThat(pagado.getBody().pagos()).singleElement()
				.satisfies(pago -> assertThat(pago.referencia()).isEqualTo("Comprobante 123"));
	}

	@Test
	void unPagoQueSePasaResponde400ConLoQueFalta() {
		ProyectoResponse proyecto = crearEnBlanco("sobrepago@ejemplo.co");

		ResponseEntity<Map> respuesta = pedir(HttpMethod.POST, "/api/proyectos/" + proyecto.id() + "/pagos",
				new PagoRequest(idDe(proyecto, "Diseño"), new BigDecimal("1190001"), LocalDate.of(2026, 9, 2),
						MedioDePago.EFECTIVO, null),
				admin(), Map.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat((String) respuesta.getBody().get("mensaje")).contains("1190000");
	}

	@Test
	void unaTransicionInvalidaResponde409() {
		ProyectoResponse proyecto = crearEnBlanco("transicion@ejemplo.co");

		ResponseEntity<Map> respuesta = pedir(HttpMethod.POST,
				"/api/proyectos/" + proyecto.id() + "/entregables/" + idDe(proyecto, "Diseño") + "/estado",
				new CambioDeEstadoRequest(EstadoEntregable.APROBADO, null), admin(), Map.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void unEnlaceDeDemoQueNoEsHttpsResponde400() {
		ProyectoResponse proyecto = crearEnBlanco("demo@ejemplo.co");

		ResponseEntity<Map> respuesta = pedir(HttpMethod.PUT,
				"/api/proyectos/" + proyecto.id() + "/entregables/" + idDe(proyecto, "Diseño") + "/demo",
				new DemoRequest("javascript:alert(1)"), admin(), Map.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void elPlanSeEditaDesdeElPanel() {
		ProyectoResponse proyecto = crearEnBlanco("plan@ejemplo.co");
		String base = "/api/proyectos/" + proyecto.id();

		ProyectoResponse conFase = pedir(HttpMethod.POST, base + "/fases",
				new FaseRequest("Fase 2", null, null, null), admin(), ProyectoResponse.class).getBody();
		UUID fase2 = conFase.fases().get(1).id();
		ProyectoResponse conEntregable = pedir(HttpMethod.POST, base + "/fases/" + fase2 + "/entregables",
				new ProyectoDtos.NuevoEntregableRequest(new EntregableRequest("Pasarela", null,
						new BigDecimal("500000"), MomentoDeCobro.AL_APROBAR), true),
				admin(), ProyectoResponse.class).getBody();
		UUID pasarela = idDe(conEntregable, "Pasarela");

		assertThat(conEntregable.fases().get(1).entregables()).singleElement()
				.satisfies(entregable -> assertThat(entregable.esCambioDeAlcance()).isTrue());
		ResponseEntity<ProyectoResponse> sinEntregable = pedir(HttpMethod.DELETE,
				base + "/entregables/" + pasarela, null, admin(), ProyectoResponse.class);
		assertThat(sinEntregable.getBody().fases().get(1).entregables()).isEmpty();
	}

	@Test
	void desdeUnaCotizacionAceptadaSeProponeElPlanYSoloSeCreaUnaVez() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panadería El Trigal", new Correo("trigal@ejemplo.co"), null, null),
				new Porcentaje(19), Instant.now().minusSeconds(3600), Instant.now().plusSeconds(86_400), null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Módulo de pedidos", 1, Dinero.de(2_000_000)));
		cotizacion.agregarItem(new ItemDeCotizacion("Capacitación", 1, Dinero.de(500_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2097, 1), Instant.now().minusSeconds(60));
		cotizacion.aceptar(Instant.now().minusSeconds(30));
		cotizaciones.guardar(cotizacion);
		UUID cotizacionId = cotizacion.id().valor();

		ResponseEntity<List<NuevaFaseRequest>> propuesta = restTemplate.exchange(
				"/api/proyectos/propuesta?cotizacion=" + cotizacionId, HttpMethod.GET, new HttpEntity<>(admin()),
				new ParameterizedTypeReference<>() {
				});
		assertThat(propuesta.getBody().getFirst().entregables()).extracting(EntregableRequest::momentoDeCobro)
				.containsExactly(MomentoDeCobro.AL_INICIAR, MomentoDeCobro.AL_APROBAR);

		NuevoProyectoRequest pedido = new NuevoProyectoRequest(cotizacionId, null, null, descripcion(),
				propuesta.getBody());
		ResponseEntity<ProyectoResponse> creado = pedir(HttpMethod.POST, "/api/proyectos", pedido, admin(),
				ProyectoResponse.class);
		assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(creado.getBody().clienteNombre()).isEqualTo("Panadería El Trigal");
		assertThat(creado.getBody().origenCotizacionId()).isEqualTo(cotizacionId);

		assertThat(pedir(HttpMethod.POST, "/api/proyectos", pedido, admin(), Map.class).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);

		// El detalle de la cotización en el panel pregunta si ya tiene proyecto.
		ResponseEntity<ProyectoResumenResponse> suProyecto = pedir(HttpMethod.GET,
				"/api/proyectos/de-cotizacion/" + cotizacionId, null, admin(), ProyectoResumenResponse.class);
		assertThat(suProyecto.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(suProyecto.getBody().id()).isEqualTo(creado.getBody().id());
	}

	@Test
	void unaCotizacionSinProyectoResponde404() {
		assertThat(pedir(HttpMethod.GET, "/api/proyectos/de-cotizacion/" + UUID.randomUUID(), null, admin(),
				Map.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void elListadoTraeElResumenDeCadaProyecto() {
		ProyectoResponse proyecto = crearEnBlanco("listado@ejemplo.co");

		ResponseEntity<List<ProyectoResumenResponse>> listado = restTemplate.exchange("/api/proyectos",
				HttpMethod.GET, new HttpEntity<>(admin()), new ParameterizedTypeReference<>() {
				});

		assertThat(listado.getBody()).filteredOn(resumen -> resumen.id().equals(proyecto.id())).singleElement()
				.satisfies(resumen -> {
					assertThat(resumen.clienteCorreo()).isEqualTo("listado@ejemplo.co");
					assertThat(resumen.total()).isEqualByComparingTo("4760000");
					assertThat(resumen.pendienteDePago()).isEqualByComparingTo("1190000");
				});
	}

	@Test
	void unProyectoQueNoExisteResponde404() {
		assertThat(pedir(HttpMethod.GET, "/api/proyectos/" + UUID.randomUUID(), null, admin(), Map.class)
				.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void unClienteNoTocaLaGestion() {
		assertThat(pedir(HttpMethod.GET, "/api/proyectos", null, cliente("curioso@ejemplo.co"), Map.class)
				.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void sinTokenResponde401() {
		assertThat(pedir(HttpMethod.GET, "/api/proyectos", null, new HttpHeaders(), Map.class).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}

}
