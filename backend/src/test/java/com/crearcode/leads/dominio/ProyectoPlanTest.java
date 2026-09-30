package com.crearcode.leads.dominio;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import static com.crearcode.leads.dominio.ProyectosDePrueba.AHORA;
import static com.crearcode.leads.dominio.ProyectosDePrueba.CLIENTE;
import static com.crearcode.leads.dominio.ProyectosDePrueba.EQUIPO;
import static com.crearcode.leads.dominio.ProyectosDePrueba.IVA;
import static com.crearcode.leads.dominio.ProyectosDePrueba.OTRO_CLIENTE;
import static com.crearcode.leads.dominio.ProyectosDePrueba.cotizacionAceptada;
import static com.crearcode.leads.dominio.ProyectosDePrueba.descripcion;
import static com.crearcode.leads.dominio.ProyectosDePrueba.enBlancoConDosEntregables;
import static com.crearcode.leads.dominio.ProyectosDePrueba.entregable;
import static com.crearcode.leads.dominio.ProyectosDePrueba.fase;
import static com.crearcode.leads.dominio.ProyectosDePrueba.idDe;
import static com.crearcode.leads.dominio.ProyectosDePrueba.vacio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Creación del proyecto y edición del plan (docs/03 Parte 5,
 * invariantes 5, 6 y decisión 29).
 */
class ProyectoPlanTest {

	// --- Creación ---

	@Test
	void unProyectoEnBlancoNaceActivoYEsDeSuCliente() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.ACTIVO);
		assertThat(proyecto.perteneceA(CLIENTE)).isTrue();
		assertThat(proyecto.perteneceA(OTRO_CLIENTE)).isFalse();
		assertThat(proyecto.origen()).isNull();
		assertThat(proyecto.fases()).hasSize(1);
		assertThat(proyecto.entregables()).extracting(Entregable::estado)
				.containsOnly(EstadoEntregable.PENDIENTE);
		assertThat(proyecto.creadoEn()).isEqualTo(AHORA);
	}

	@Test
	void unProyectoNecesitaCorreoYNombreDelCliente() {
		assertThatThrownBy(() -> Proyecto.enBlanco(null, "Café Valle", descripcion(), IVA, List.of(), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> Proyecto.enBlanco(CLIENTE, "  ", descripcion(), IVA, List.of(), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void laEntregaEstimadaNoPuedeSerAntesDelInicio() {
		assertThatThrownBy(() -> new DescripcionDelProyecto("Tienda", null, LocalDate.of(2026, 10, 10),
				LocalDate.of(2026, 10, 9))).isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unProyectoNecesitaNombreYFechaDeInicio() {
		assertThatThrownBy(() -> new DescripcionDelProyecto(" ", null, LocalDate.of(2026, 10, 1), null))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> new DescripcionDelProyecto("Tienda", null, null, null))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unaFaseNoPuedeTerminarAntesDeEmpezar() {
		assertThatThrownBy(() -> new DatosDeFase("Fase 1", null, LocalDate.of(2026, 10, 10),
				LocalDate.of(2026, 10, 1))).isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unEntregableNecesitaNombreValorYMomentoDeCobro() {
		assertThatThrownBy(() -> new DatosDeEntregable(" ", null, Dinero.de(1), MomentoDeCobro.AL_APROBAR))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> new DatosDeEntregable("Diseño", null, null, MomentoDeCobro.AL_APROBAR))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> new DatosDeEntregable("Diseño", null, Dinero.de(1), null))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	// --- Desde una cotización (invariantes 5 y 6) ---

	@Test
	void desdeUnaCotizacionTomaElClienteElImpuestoYElOrigen() {
		Cotizacion cotizacion = cotizacionAceptada();

		Proyecto proyecto = Proyecto.desdeCotizacion(cotizacion, descripcion(), List.of(new NuevaFase(
				fase("Fase 1"), List.of(
						entregable("Diseño", 2_000_000, MomentoDeCobro.AL_INICIAR),
						entregable("Catálogo", 3_000_000, MomentoDeCobro.AL_APROBAR)))),
				AHORA);

		assertThat(proyecto.perteneceA(CLIENTE)).isTrue();
		assertThat(proyecto.nombreDelCliente()).isEqualTo("Café Valle S.A.S.");
		assertThat(proyecto.impuesto()).isEqualTo(IVA);
		assertThat(proyecto.origen()).isEqualTo(cotizacion.id());
	}

	@Test
	void losEntregablesInicialesTienenQueSumarLoQueElClienteAcepto() {
		assertThatThrownBy(() -> Proyecto.desdeCotizacion(cotizacionAceptada(), descripcion(), List.of(new NuevaFase(
				fase("Fase 1"), List.of(entregable("Todo", 4_000_000, MomentoDeCobro.AL_INICIAR)))), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class)
				.hasMessageContaining("1000000");
	}

	@Test
	void soloSeCreaUnProyectoDesdeUnaCotizacionAceptada() {
		Cotizacion enviada = Cotizacion.abrirBorrador(new DatosDelCliente("Café Valle", CLIENTE, null, null), IVA,
				AHORA.minusSeconds(3600), AHORA.plusSeconds(86_400), null, null);
		enviada.agregarItem(new ItemDeCotizacion("Todo", 1, Dinero.de(5_000_000)));
		enviada.enviar(NumeroDeCotizacion.de(2026, 2), AHORA.minusSeconds(60));

		assertThatThrownBy(() -> Proyecto.desdeCotizacion(enviada, descripcion(), List.of(new NuevaFase(
				fase("Fase 1"), List.of(entregable("Todo", 5_000_000, MomentoDeCobro.AL_INICIAR)))), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void enUnProyectoDeCotizacionLoNuevoSoloEntraComoCambioDeAlcance() {
		Proyecto proyecto = Proyecto.desdeCotizacion(cotizacionAceptada(), descripcion(), List.of(new NuevaFase(
				fase("Fase 1"), List.of(entregable("Todo", 5_000_000, MomentoDeCobro.AL_INICIAR)))), AHORA);
		FaseId faseId = proyecto.fases().getFirst().id();

		assertThatThrownBy(() -> proyecto.agregarEntregable(faseId,
				entregable("Extra", 500_000, MomentoDeCobro.AL_APROBAR), false, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);

		EntregableId extra = proyecto.agregarEntregable(faseId,
				entregable("Extra", 500_000, MomentoDeCobro.AL_APROBAR), true, AHORA);
		assertThat(proyecto.entregable(extra).esCambioDeAlcance()).isTrue();
	}

	@Test
	void enUnProyectoDeCotizacionNoSeCambiaNiSeQuitaLoAceptado() {
		Proyecto proyecto = Proyecto.desdeCotizacion(cotizacionAceptada(), descripcion(), List.of(new NuevaFase(
				fase("Fase 1"), List.of(entregable("Todo", 5_000_000, MomentoDeCobro.AL_INICIAR)))), AHORA);
		EntregableId todo = idDe(proyecto, "Todo");

		assertThatThrownBy(() -> proyecto.editarEntregable(todo,
				entregable("Todo", 4_000_000, MomentoDeCobro.AL_INICIAR), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> proyecto.quitarEntregable(todo, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);

		proyecto.editarEntregable(todo, entregable("Tienda completa", 5_000_000, MomentoDeCobro.AL_INICIAR), AHORA);
		assertThat(proyecto.entregable(todo).nombre()).isEqualTo("Tienda completa");
	}

	// --- Fases ---

	@Test
	void seAgreganReordenanYQuitanFasesVacias() {
		Proyecto proyecto = vacio();
		FaseId primera = proyecto.agregarFase(fase("Descubrimiento"), AHORA);
		FaseId segunda = proyecto.agregarFase(fase("Construcción"), AHORA);

		proyecto.moverFase(segunda, 0, AHORA);
		assertThat(proyecto.fases()).extracting(Fase::id).containsExactly(segunda, primera);

		proyecto.quitarFase(primera, AHORA);
		assertThat(proyecto.fases()).extracting(Fase::id).containsExactly(segunda);
	}

	@Test
	void unaFaseConEntregablesNoSeQuita() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		assertThatThrownBy(() -> proyecto.quitarFase(proyecto.fases().getFirst().id(), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void alCerrarUnaFaseSeEscribeElResumenParaElCliente() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		FaseId faseId = proyecto.fases().getFirst().id();

		proyecto.escribirResumenDeFase(faseId, "Quedó lista la identidad de tu tienda.", AHORA);

		assertThat(proyecto.fase(faseId).resumenParaElCliente()).isEqualTo("Quedó lista la identidad de tu tienda.");
	}

	@Test
	void unaFaseInexistenteSeRechaza() {
		Proyecto proyecto = vacio();

		assertThatThrownBy(() -> proyecto.quitarFase(FaseId.nuevo(), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	// --- Entregables (decisión 29) ---

	@Test
	void unEntregablePendienteSinPagosSeEditaYSeQuita() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId catalogo = idDe(proyecto, "Catálogo");

		proyecto.editarEntregable(catalogo, entregable("Catálogo", 3_500_000, MomentoDeCobro.AL_APROBAR), AHORA);
		assertThat(proyecto.entregable(catalogo).valor()).isEqualTo(Dinero.de(3_500_000));

		proyecto.quitarEntregable(catalogo, AHORA);
		assertThat(proyecto.entregables()).extracting(Entregable::nombre).containsExactly("Diseño");
	}

	@Test
	void unEntregableConPagosNoSeQuitaNiCambiaDeValor() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		proyecto.registrarPago(new NuevoPago(diseno, Dinero.de(500_000), LocalDate.of(2026, 10, 2),
				MedioDePago.TRANSFERENCIA, OrigenDePago.MANUAL, null, EQUIPO), AHORA);

		assertThatThrownBy(() -> proyecto.quitarEntregable(diseno, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> proyecto.editarEntregable(diseno,
				entregable("Diseño", 2_000_000, MomentoDeCobro.AL_INICIAR), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unEntregableConTrabajoNoSeQuita() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId catalogo = idDe(proyecto, "Catálogo");
		proyecto.cambiarEstadoDeEntregable(catalogo, EstadoEntregable.EN_CURSO, null, AHORA);

		assertThatThrownBy(() -> proyecto.quitarEntregable(catalogo, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unEntregableSeMueveAOtraFase() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		FaseId segunda = proyecto.agregarFase(fase("Fase 2"), AHORA);
		EntregableId catalogo = idDe(proyecto, "Catálogo");

		proyecto.moverEntregable(catalogo, segunda, 0, AHORA);

		assertThat(proyecto.fase(segunda).entregables()).extracting(Entregable::id).containsExactly(catalogo);
		assertThat(proyecto.fases().getFirst().entregables()).extracting(Entregable::nombre)
				.containsExactly("Diseño");
	}

	@Test
	void elEnlaceDeLaDemoSePoneYSeQuita() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");

		proyecto.ponerDemo(diseno, new UrlDeDemo("https://figma.com/proto/cafe"), AHORA);
		assertThat(proyecto.entregable(diseno).demo()).isEqualTo(new UrlDeDemo("https://figma.com/proto/cafe"));

		proyecto.ponerDemo(diseno, null, AHORA);
		assertThat(proyecto.entregable(diseno).demo()).isNull();
	}

	@Test
	void cadaCambioActualizaLaFechaDeActualizacion() {
		Proyecto proyecto = vacio();

		proyecto.agregarFase(fase("Fase 1"), ProyectosDePrueba.DESPUES);

		assertThat(proyecto.actualizadoEn()).isEqualTo(ProyectosDePrueba.DESPUES);
	}

}
