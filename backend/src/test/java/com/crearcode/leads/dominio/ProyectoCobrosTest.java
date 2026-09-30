package com.crearcode.leads.dominio;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import static com.crearcode.leads.dominio.ProyectosDePrueba.AHORA;
import static com.crearcode.leads.dominio.ProyectosDePrueba.CLIENTE;
import static com.crearcode.leads.dominio.ProyectosDePrueba.EQUIPO;
import static com.crearcode.leads.dominio.ProyectosDePrueba.IVA;
import static com.crearcode.leads.dominio.ProyectosDePrueba.SIN_IMPUESTO;
import static com.crearcode.leads.dominio.ProyectosDePrueba.aprobarComoEquipo;
import static com.crearcode.leads.dominio.ProyectosDePrueba.descripcion;
import static com.crearcode.leads.dominio.ProyectosDePrueba.enBlancoConDosEntregables;
import static com.crearcode.leads.dominio.ProyectosDePrueba.entregable;
import static com.crearcode.leads.dominio.ProyectosDePrueba.fase;
import static com.crearcode.leads.dominio.ProyectosDePrueba.idDe;
import static com.crearcode.leads.dominio.ProyectosDePrueba.vacio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Avance, cobros y pagos (docs/03 Parte 5, invariantes 1-4 y 8; ADR-15).
 * El proyecto de prueba tiene "Diseño" (1 M, se cobra al iniciar) y
 * "Catálogo" (3 M, se cobra al aprobar).
 */
class ProyectoCobrosTest {

	private static NuevoPago pago(EntregableId entregable, long monto) {
		return new NuevoPago(entregable, Dinero.de(monto), LocalDate.of(2026, 10, 2), MedioDePago.NEQUI_DAVIPLATA,
				OrigenDePago.MANUAL, "Comprobante 123", EQUIPO);
	}

	// --- Avance (invariante 1) ---

	@Test
	void elAvanceSeMidePorValorNoPorCantidad() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		aprobarComoEquipo(proyecto, idDe(proyecto, "Diseño"));

		assertThat(proyecto.avance()).isEqualTo(25);
	}

	@Test
	void elAvanceRedondeaHaciaAbajoParaNoMostrarCienSinTerminar() {
		Proyecto proyecto = Proyecto.enBlanco(CLIENTE, "Café Valle", descripcion(), IVA,
				List.of(new NuevaFase(fase("Fase 1"), List.of(
						entregable("Uno", 1, MomentoDeCobro.AL_APROBAR),
						entregable("Dos", 999, MomentoDeCobro.AL_APROBAR)))),
				AHORA);

		aprobarComoEquipo(proyecto, idDe(proyecto, "Dos"));

		assertThat(proyecto.avance()).isEqualTo(99);
	}

	@Test
	void unProyectoSinEntregablesEstaEnCero() {
		assertThat(vacio().avance()).isZero();
	}

	// --- Lo que se cobra (invariante 3) ---

	@Test
	void cadaEntregableSeCobraConElImpuestoDelProyectoSeparado() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");

		assertThat(proyecto.impuestoDe(diseno)).isEqualTo(Dinero.de(190_000));
		assertThat(proyecto.cobroDe(diseno)).isEqualTo(Dinero.de(1_190_000));
		assertThat(proyecto.total()).isEqualTo(Dinero.de(4_760_000));
	}

	@Test
	void sinImpuestoSeCobraElValorTalCual() {
		Proyecto proyecto = enBlancoConDosEntregables(SIN_IMPUESTO);

		assertThat(proyecto.cobroDe(idDe(proyecto, "Catálogo"))).isEqualTo(Dinero.de(3_000_000));
	}

	// --- Cuándo es cobrable (invariante 2, decisión 21) ---

	@Test
	void elAnticipoEsCobrableDesdeQueExisteElProyecto() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		assertThat(proyecto.esCobrable(idDe(proyecto, "Diseño"))).isTrue();
		assertThat(proyecto.esCobrable(idDe(proyecto, "Catálogo"))).isFalse();
		assertThat(proyecto.pendienteDePago()).isEqualTo(Dinero.de(1_190_000));
	}

	@Test
	void unEntregableQueSeCobraAlAprobarQuedaPendienteAlAprobarse() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId catalogo = idDe(proyecto, "Catálogo");

		aprobarComoEquipo(proyecto, catalogo);

		assertThat(proyecto.esCobrable(catalogo)).isTrue();
		assertThat(proyecto.pendienteDe(catalogo)).isEqualTo(Dinero.de(3_570_000));
		assertThat(proyecto.pendienteDePago()).isEqualTo(Dinero.de(4_760_000));
	}

	@Test
	void loQueTodaviaNoEsCobrableNoApareceComoPendiente() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		assertThat(proyecto.pendienteDe(idDe(proyecto, "Catálogo"))).isEqualTo(Dinero.CERO);
	}

	// --- Pagos (invariante 4) ---

	@Test
	void unPagoParcialBajaLoPendienteYSubeLoPagado() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");

		PagoId pagoId = proyecto.registrarPago(pago(diseno, 590_000), AHORA);

		assertThat(proyecto.pagadoDe(diseno)).isEqualTo(Dinero.de(590_000));
		assertThat(proyecto.pendienteDe(diseno)).isEqualTo(Dinero.de(600_000));
		assertThat(proyecto.totalPagado()).isEqualTo(Dinero.de(590_000));
		assertThat(proyecto.saldo()).isEqualTo(Dinero.de(4_170_000));
		assertThat(proyecto.pagos()).extracting(Pago::id).containsExactly(pagoId);
		Pago registrado = proyecto.pagos().getFirst();
		assertThat(registrado.medio()).isEqualTo(MedioDePago.NEQUI_DAVIPLATA);
		assertThat(registrado.origen()).isEqualTo(OrigenDePago.MANUAL);
		assertThat(registrado.registradoPor()).isEqualTo(EQUIPO);
	}

	@Test
	void losPagosDeUnEntregableNuncaSumanMasDeLoQueSeCobraPorEl() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		proyecto.registrarPago(pago(diseno, 1_000_000), AHORA);

		assertThatThrownBy(() -> proyecto.registrarPago(pago(diseno, 190_001), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class)
				.hasMessageContaining("190000");

		proyecto.registrarPago(pago(diseno, 190_000), AHORA);
		assertThat(proyecto.pendienteDe(diseno)).isEqualTo(Dinero.CERO);
	}

	/**
	 * Un cliente puede adelantar el pago de algo que aún no se aprueba;
	 * lo que no puede es pasarse de lo que vale.
	 */
	@Test
	void sePuedePagarPorAdelantadoLoQueTodaviaNoEsCobrable() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId catalogo = idDe(proyecto, "Catálogo");

		proyecto.registrarPago(pago(catalogo, 3_570_000), AHORA);

		assertThat(proyecto.pagadoDe(catalogo)).isEqualTo(Dinero.de(3_570_000));
	}

	@Test
	void unPagoEnCeroNoSeRegistra() {
		assertThatThrownBy(() -> pago(EntregableId.nuevo(), 0)).isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unPagoNecesitaFechaMedioOrigenYQuienLoRegistro() {
		EntregableId id = EntregableId.nuevo();
		assertThatThrownBy(() -> new NuevoPago(id, Dinero.de(1), null, MedioDePago.EFECTIVO, OrigenDePago.MANUAL,
				null, EQUIPO)).isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> new NuevoPago(id, Dinero.de(1), LocalDate.of(2026, 10, 1), null,
				OrigenDePago.MANUAL, null, EQUIPO)).isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> new NuevoPago(id, Dinero.de(1), LocalDate.of(2026, 10, 1), MedioDePago.EFECTIVO,
				null, null, EQUIPO)).isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> new NuevoPago(id, Dinero.de(1), LocalDate.of(2026, 10, 1), MedioDePago.EFECTIVO,
				OrigenDePago.MANUAL, null, null)).isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void unPagoAUnEntregableQueNoEsDelProyectoSeRechaza() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		assertThatThrownBy(() -> proyecto.registrarPago(pago(EntregableId.nuevo(), 1_000), AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	/** Invariante 8: el dinero llega cuando llega, aunque el proyecto esté pausado. */
	@Test
	void unPagoSeRegistraAunqueElProyectoEsteEnPausa() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		proyecto.pausar(AHORA);

		proyecto.registrarPago(pago(idDe(proyecto, "Diseño"), 1_000), AHORA);

		assertThat(proyecto.totalPagado()).isEqualTo(Dinero.de(1_000));
	}

}
