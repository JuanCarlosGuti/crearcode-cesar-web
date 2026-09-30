package com.crearcode.leads.infraestructura.rest;

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
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.QuienResponde;
import com.crearcode.leads.dominio.UsuarioRepositorio;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.AjustesRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ProyectoResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La vista del cliente (HU-49 a HU-51, HU-57). El correo sale del token:
 * nadie ve ni responde el proyecto de otro cambiando un parámetro, y uno
 * ajeno responde 404 —no 403—, para no revelar que existe.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class MisProyectosControllerIT {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private GeneradorDeToken generadorDeToken;

	@Autowired
	private ProyectoRepositorio proyectos;

	private HttpHeaders comoCliente(String correo) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(ClienteDePrueba.registrarYAutenticar(usuarios, generadorDeToken, correo));
		return headers;
	}

	/** Un proyecto del cliente con "Diseño" ya en revisión y "Catálogo" pendiente. */
	private Proyecto proyectoEnRevisionDe(String correo) {
		Instant hace = Instant.now().minusSeconds(3600);
		Proyecto proyecto = Proyecto.enBlanco(new Correo(correo), "Café Valle S.A.S.",
				new DescripcionDelProyecto("Tienda de Café Valle", null, LocalDate.of(2026, 9, 1), null),
				new Porcentaje(19),
				List.of(new NuevaFase(new DatosDeFase("Fase 1", null, null, null), List.of(
						new DatosDeEntregable("Diseño", null, Dinero.de(1_000_000), MomentoDeCobro.AL_INICIAR),
						new DatosDeEntregable("Catálogo", null, Dinero.de(3_000_000), MomentoDeCobro.AL_APROBAR)))),
				hace);
		EntregableId diseno = idDe(proyecto, "Diseño");
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_CURSO, null, hace);
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_REVISION, null, hace);
		proyectos.guardar(proyecto);
		return proyecto;
	}

	private static EntregableId idDe(Proyecto proyecto, String nombre) {
		return proyecto.entregables().stream().filter(e -> e.nombre().equals(nombre)).findFirst().orElseThrow().id();
	}

	private <T> ResponseEntity<T> pedir(HttpMethod metodo, String ruta, Object cuerpo, HttpHeaders headers,
			Class<T> tipo) {
		return restTemplate.exchange(ruta, metodo, new HttpEntity<>(cuerpo, headers), tipo);
	}

	@Test
	void elClienteSoloVeSusProyectos() {
		Proyecto suyo = proyectoEnRevisionDe("mio@ejemplo.co");
		proyectoEnRevisionDe("ajeno@ejemplo.co");

		ResponseEntity<List<ProyectoResponse>> listado = restTemplate.exchange("/api/mis-proyectos",
				HttpMethod.GET, new HttpEntity<>(comoCliente("mio@ejemplo.co")), new ParameterizedTypeReference<>() {
				});

		assertThat(listado.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(listado.getBody()).extracting(ProyectoResponse::id).containsExactly(suyo.id().valor());
		// Lo interno del equipo no viaja al cliente.
		assertThat(listado.getBody().getFirst().clienteTieneCuenta()).isNull();
		assertThat(listado.getBody().getFirst().pendienteDePago()).isEqualByComparingTo("1190000");
	}

	@Test
	void unProyectoAjenoResponde404ComoSiNoExistiera() {
		Proyecto ajeno = proyectoEnRevisionDe("dueno@ejemplo.co");

		assertThat(pedir(HttpMethod.GET, "/api/mis-proyectos/" + ajeno.id().valor(), null,
				comoCliente("intruso@ejemplo.co"), Map.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(pedir(HttpMethod.GET, "/api/mis-proyectos/" + UUID.randomUUID(), null,
				comoCliente("intruso@ejemplo.co"), Map.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void elClienteApruebaLoQueEstaEnRevision() {
		Proyecto proyecto = proyectoEnRevisionDe("aprueba@ejemplo.co");

		ResponseEntity<ProyectoResponse> respuesta = pedir(HttpMethod.POST,
				"/api/mis-proyectos/" + proyecto.id().valor() + "/entregables/" + idDe(proyecto, "Diseño").valor()
						+ "/aprobacion",
				null, comoCliente("aprueba@ejemplo.co"), ProyectoResponse.class);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(respuesta.getBody().avance()).isEqualTo(25);
		assertThat(respuesta.getBody().fases().getFirst().entregables().getFirst())
				.satisfies(diseno -> {
					assertThat(diseno.estado()).isEqualTo(EstadoEntregable.APROBADO);
					assertThat(diseno.aprobadoPor()).isEqualTo(QuienResponde.CLIENTE);
				});
	}

	@Test
	void pedirAjustesSinNotaResponde400YConNotaDevuelveElEntregable() {
		Proyecto proyecto = proyectoEnRevisionDe("ajustes@ejemplo.co");
		String ruta = "/api/mis-proyectos/" + proyecto.id().valor() + "/entregables/"
				+ idDe(proyecto, "Diseño").valor() + "/ajustes";

		assertThat(pedir(HttpMethod.POST, ruta, new AjustesRequest("  "), comoCliente("ajustes@ejemplo.co"),
				Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<ProyectoResponse> respuesta = pedir(HttpMethod.POST, ruta,
				new AjustesRequest("El logo más grande"), comoCliente("ajustes@ejemplo.co"), ProyectoResponse.class);
		assertThat(respuesta.getBody().fases().getFirst().entregables().getFirst().notaDeAjustes())
				.isEqualTo("El logo más grande");
	}

	@Test
	void loQueNoEstaEnRevisionNoSeRespondeY409() {
		Proyecto proyecto = proyectoEnRevisionDe("pendiente@ejemplo.co");

		assertThat(pedir(HttpMethod.POST, "/api/mis-proyectos/" + proyecto.id().valor() + "/entregables/"
				+ idDe(proyecto, "Catálogo").valor() + "/aprobacion", null, comoCliente("pendiente@ejemplo.co"),
				Map.class).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void unClienteNoApruebaElProyectoDeOtro() {
		Proyecto ajeno = proyectoEnRevisionDe("otro-dueno@ejemplo.co");

		assertThat(pedir(HttpMethod.POST, "/api/mis-proyectos/" + ajeno.id().valor() + "/entregables/"
				+ idDe(ajeno, "Diseño").valor() + "/aprobacion", null, comoCliente("vecino@ejemplo.co"), Map.class)
				.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(proyectos.buscarPorId(ajeno.id()).orElseThrow().entregable(idDe(ajeno, "Diseño")).estado())
				.isEqualTo(EstadoEntregable.EN_REVISION);
	}

	@Test
	void sinTokenResponde401() {
		assertThat(pedir(HttpMethod.GET, "/api/mis-proyectos", null, new HttpHeaders(), Map.class).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}

}
