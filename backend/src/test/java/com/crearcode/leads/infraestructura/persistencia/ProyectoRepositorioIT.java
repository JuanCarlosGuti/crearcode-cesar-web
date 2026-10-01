package com.crearcode.leads.infraestructura.persistencia;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.Entregable;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.Fase;
import com.crearcode.leads.dominio.FaseId;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.NuevoPago;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.Pago;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.QuienResponde;
import com.crearcode.leads.dominio.UrlDeDemo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ida y vuelta contra PostgreSQL real (Testcontainers). Cada lectura va
 * después de vaciar el contexto de persistencia: si no, JPA devolvería
 * los objetos que ya tiene en memoria y un error de mapeo pasaría
 * desapercibido.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ProyectoRepositorioIT {

	private static final Instant AHORA = Instant.parse("2026-10-01T15:00:00Z");
	private static final Correo CLIENTE = new Correo("cliente@ejemplo.co");
	private static final Correo EQUIPO = new Correo("admin@crearcodecesar.com");

	@Autowired
	private ProyectoRepositorio repositorio;

	@Autowired
	private CotizacionRepositorio cotizaciones;

	@Autowired
	private EntityManager entityManager;

	private Proyecto proyectoConDosFases(Correo cliente) {
		return Proyecto.enBlanco(cliente, "Café Valle S.A.S.",
				new DescripcionDelProyecto("Tienda de Café Valle", "Tu tienda para vender café",
						LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15)),
				new Porcentaje(19),
				List.of(
						new NuevaFase(new DatosDeFase("Diseño", "Que veas cómo se verá", LocalDate.of(2026, 10, 1),
								LocalDate.of(2026, 10, 15)), List.of(
										new DatosDeEntregable("Identidad", "Logo y colores", Dinero.de(1_000_000),
												MomentoDeCobro.AL_INICIAR),
										new DatosDeEntregable("Pantallas", null, Dinero.de(2_000_000),
												MomentoDeCobro.AL_APROBAR))),
						new NuevaFase(new DatosDeFase("Construcción", null, null, null), List.of(
								new DatosDeEntregable("Catálogo", null, Dinero.de(3_000_000),
										MomentoDeCobro.AL_APROBAR)))),
				AHORA);
	}

	private Proyecto recargado(Proyecto proyecto) {
		entityManager.flush();
		entityManager.clear();
		return repositorio.buscarPorId(proyecto.id()).orElseThrow();
	}

	private static EntregableId idDe(Proyecto proyecto, String nombre) {
		return proyecto.entregables().stream().filter(e -> e.nombre().equals(nombre)).findFirst().orElseThrow().id();
	}

	@Test
	void guardaYRecuperaUnProyectoCompleto() {
		Proyecto original = proyectoConDosFases(CLIENTE);
		EntregableId identidad = idDe(original, "Identidad");
		original.ponerDemo(identidad, new UrlDeDemo("https://figma.com/proto/cafe"), AHORA);
		original.cambiarEstadoDeEntregable(identidad, EstadoEntregable.EN_CURSO, null, AHORA);
		original.cambiarEstadoDeEntregable(identidad, EstadoEntregable.EN_REVISION, null, AHORA);
		original.cambiarEstadoDeEntregable(identidad, EstadoEntregable.CON_AJUSTES, "Logo más grande", AHORA);
		original.escribirResumenDeFase(original.fases().getFirst().id(), "Quedó la identidad.", AHORA);
		original.registrarPago(new NuevoPago(identidad, Dinero.de(590_000), LocalDate.of(2026, 10, 2),
				MedioDePago.NEQUI_DAVIPLATA, OrigenDePago.MANUAL, "Comprobante 123", EQUIPO), AHORA);

		repositorio.guardar(original);
		Proyecto recuperado = recargado(original);

		assertThat(recuperado.id()).isEqualTo(original.id());
		assertThat(recuperado.correoDelCliente()).isEqualTo(CLIENTE);
		assertThat(recuperado.nombreDelCliente()).isEqualTo("Café Valle S.A.S.");
		assertThat(recuperado.descripcion()).isEqualTo(original.descripcion());
		assertThat(recuperado.impuesto()).isEqualTo(new Porcentaje(19));
		assertThat(recuperado.estado()).isEqualTo(EstadoProyecto.ACTIVO);
		assertThat(recuperado.creadoEn()).isEqualTo(AHORA);
		assertThat(recuperado.actualizadoEn()).isEqualTo(AHORA);

		assertThat(recuperado.fases()).extracting(Fase::id)
				.containsExactlyElementsOf(original.fases().stream().map(Fase::id).toList());
		assertThat(recuperado.fases().getFirst().datos()).isEqualTo(original.fases().getFirst().datos());
		assertThat(recuperado.fases().getFirst().resumenParaElCliente()).isEqualTo("Quedó la identidad.");
		assertThat(recuperado.entregables()).extracting(Entregable::nombre)
				.containsExactly("Identidad", "Pantallas", "Catálogo");

		Entregable conAjustes = recuperado.entregable(identidad);
		assertThat(conAjustes.datos()).isEqualTo(original.entregable(identidad).datos());
		assertThat(conAjustes.estado()).isEqualTo(EstadoEntregable.CON_AJUSTES);
		assertThat(conAjustes.notaDeAjustes()).isEqualTo("Logo más grande");
		assertThat(conAjustes.demo()).isEqualTo(new UrlDeDemo("https://figma.com/proto/cafe"));
		assertThat(conAjustes.esCambioDeAlcance()).isFalse();

		Pago pago = recuperado.pagos().getFirst();
		assertThat(pago).isEqualTo(original.pagos().getFirst());
		assertThat(recuperado.saldo()).isEqualTo(original.saldo());
	}

	/**
	 * Los identificadores de fases, entregables y pagos son los del
	 * dominio: el portal y los pagos los referencian, así que guardar
	 * otra vez no puede cambiarlos.
	 */
	@Test
	void guardarOtraVezConservaLosIdentificadoresYReflejaLosCambios() {
		Proyecto proyecto = proyectoConDosFases(CLIENTE);
		repositorio.guardar(proyecto);
		proyecto = recargado(proyecto);

		FaseId construccion = proyecto.fases().get(1).id();
		EntregableId pantallas = idDe(proyecto, "Pantallas");
		proyecto.moverEntregable(pantallas, construccion, 0, AHORA.plusSeconds(60));
		proyecto.quitarEntregable(idDe(proyecto, "Catálogo"), AHORA.plusSeconds(60));
		repositorio.guardar(proyecto);
		Proyecto recuperado = recargado(proyecto);

		assertThat(recuperado.fase(construccion).entregables()).extracting(Entregable::id).containsExactly(pantallas);
		assertThat(recuperado.entregables()).extracting(Entregable::nombre).containsExactly("Identidad", "Pantallas");
		assertThat(recuperado.actualizadoEn()).isEqualTo(AHORA.plusSeconds(60));
	}

	@Test
	void guardaLaGarantiaYQuienAprobo() {
		Proyecto proyecto = Proyecto.enBlanco(CLIENTE, "Café Valle", new DescripcionDelProyecto("Tienda", null,
				LocalDate.of(2026, 10, 1), null), new Porcentaje(0),
				List.of(new NuevaFase(new DatosDeFase("Única", null, null, null), List.of(
						new DatosDeEntregable("Todo", null, Dinero.de(1_000), MomentoDeCobro.AL_APROBAR)))),
				AHORA);
		EntregableId todo = idDe(proyecto, "Todo");
		proyecto.cambiarEstadoDeEntregable(todo, EstadoEntregable.EN_CURSO, null, AHORA);
		proyecto.cambiarEstadoDeEntregable(todo, EstadoEntregable.EN_REVISION, null, AHORA);
		proyecto.aprobarComoCliente(todo, CLIENTE, AHORA);

		repositorio.guardar(proyecto);
		Proyecto recuperado = recargado(proyecto);

		assertThat(recuperado.estado()).isEqualTo(EstadoProyecto.EN_GARANTIA);
		assertThat(recuperado.finDeGarantia()).isEqualTo(AHORA.plus(Duration.ofDays(60)));
		assertThat(recuperado.entregable(todo).aprobadoPor()).isEqualTo(QuienResponde.CLIENTE);
		assertThat(recuperado.entregable(todo).aprobadoEn()).isEqualTo(AHORA);
		assertThat(repositorio.listarPorEstado(EstadoProyecto.EN_GARANTIA)).extracting(Proyecto::id)
				.containsExactly(proyecto.id());
	}

	@Test
	void laVistaDelClienteNoDistingueMayusculasEnElCorreo() {
		Proyecto delCliente = proyectoConDosFases(CLIENTE);
		repositorio.guardar(delCliente);
		repositorio.guardar(proyectoConDosFases(new Correo("otro@ejemplo.co")));
		entityManager.flush();
		entityManager.clear();

		assertThat(repositorio.listarPorCorreoDelCliente(new Correo("Cliente@EJEMPLO.co")))
				.extracting(Proyecto::id).containsExactly(delCliente.id());
	}

	@Test
	void encuentraElProyectoDeUnaCotizacionYElBloqueoLeeLoMismo() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(new DatosDelCliente("Café Valle", CLIENTE, null, null),
				new Porcentaje(19), AHORA.minusSeconds(3600), AHORA.plusSeconds(86_400), null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Todo", 1, Dinero.de(5_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 99), AHORA.minusSeconds(60));
		cotizacion.aceptar(AHORA.minusSeconds(30));
		cotizaciones.guardar(cotizacion);
		Proyecto proyecto = Proyecto.desdeCotizacion(cotizacion, new DescripcionDelProyecto("Tienda", null,
				LocalDate.of(2026, 10, 1), null), List.of(new NuevaFase(new DatosDeFase("Única", null, null, null),
						List.of(new DatosDeEntregable("Todo", null, Dinero.de(5_000), MomentoDeCobro.AL_INICIAR)))),
				AHORA);
		repositorio.guardar(proyecto);
		entityManager.flush();
		entityManager.clear();

		assertThat(repositorio.buscarPorCotizacion(cotizacion.id())).map(Proyecto::id).contains(proyecto.id());
		assertThat(repositorio.buscarPorIdParaModificar(proyecto.id()).orElseThrow().origen())
				.isEqualTo(cotizacion.id());
	}

}
