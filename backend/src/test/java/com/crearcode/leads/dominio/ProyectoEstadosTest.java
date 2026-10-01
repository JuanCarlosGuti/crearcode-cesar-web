package com.crearcode.leads.dominio;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static com.crearcode.leads.dominio.ProyectosDePrueba.AHORA;
import static com.crearcode.leads.dominio.ProyectosDePrueba.CLIENTE;
import static com.crearcode.leads.dominio.ProyectosDePrueba.DESPUES;
import static com.crearcode.leads.dominio.ProyectosDePrueba.IVA;
import static com.crearcode.leads.dominio.ProyectosDePrueba.OTRO_CLIENTE;
import static com.crearcode.leads.dominio.ProyectosDePrueba.aprobarComoEquipo;
import static com.crearcode.leads.dominio.ProyectosDePrueba.enBlancoConDosEntregables;
import static com.crearcode.leads.dominio.ProyectosDePrueba.entregable;
import static com.crearcode.leads.dominio.ProyectosDePrueba.idDe;
import static com.crearcode.leads.dominio.ProyectosDePrueba.llevarARevision;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Estados de los entregables y del proyecto: lo que mueve el equipo, lo
 * que responde el cliente (decisión 27) y la garantía (decisión 23).
 */
class ProyectoEstadosTest {

	// --- El equipo mueve los entregables ---

	@Test
	void elEquipoSoloHaceTransicionesValidas() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");

		assertThatThrownBy(() -> proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.APROBADO, null, AHORA))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);

		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_CURSO, null, AHORA);
		assertThat(proyecto.entregable(diseno).estado()).isEqualTo(EstadoEntregable.EN_CURSO);
	}

	@Test
	void devolverConAjustesExigeLaNotaYLaGuarda() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		llevarARevision(proyecto, diseno);

		assertThatThrownBy(() -> proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.CON_AJUSTES, "  ", AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);

		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.CON_AJUSTES, "El logo más grande", AHORA);
		assertThat(proyecto.entregable(diseno).notaDeAjustes()).isEqualTo("El logo más grande");
	}

	@Test
	void alAprobarQuedaQuienYCuando() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");

		llevarARevision(proyecto, diseno);
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.APROBADO, null, DESPUES);

		Entregable aprobado = proyecto.entregable(diseno);
		assertThat(aprobado.aprobadoEn()).isEqualTo(DESPUES);
		assertThat(aprobado.aprobadoPor()).isEqualTo(QuienResponde.EQUIPO);
	}

	// --- El cliente responde (decisión 27, invariante 10) ---

	@Test
	void elClienteApruebaLoQueEstaEnRevision() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		llevarARevision(proyecto, diseno);

		proyecto.aprobarComoCliente(diseno, CLIENTE, DESPUES);

		Entregable aprobado = proyecto.entregable(diseno);
		assertThat(aprobado.estado()).isEqualTo(EstadoEntregable.APROBADO);
		assertThat(aprobado.aprobadoPor()).isEqualTo(QuienResponde.CLIENTE);
		assertThat(aprobado.aprobadoEn()).isEqualTo(DESPUES);
	}

	@Test
	void elClientePideAjustesSoloConNota() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		llevarARevision(proyecto, diseno);

		assertThatThrownBy(() -> proyecto.pedirAjustesComoCliente(diseno, CLIENTE, null, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);

		proyecto.pedirAjustesComoCliente(diseno, CLIENTE, "Cambiar los colores", AHORA);
		assertThat(proyecto.entregable(diseno).estado()).isEqualTo(EstadoEntregable.CON_AJUSTES);
		assertThat(proyecto.entregable(diseno).notaDeAjustes()).isEqualTo("Cambiar los colores");
	}

	@Test
	void elClienteNoRespondeLoQueNoEstaEnRevision() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_CURSO, null, AHORA);

		assertThatThrownBy(() -> proyecto.aprobarComoCliente(diseno, CLIENTE, AHORA))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	@Test
	void unClienteNoRespondeElProyectoDeOtro() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		llevarARevision(proyecto, diseno);

		assertThatThrownBy(() -> proyecto.aprobarComoCliente(diseno, OTRO_CLIENTE, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThat(proyecto.entregable(diseno).estado()).isEqualTo(EstadoEntregable.EN_REVISION);
	}

	@Test
	void elClienteNoRespondeMientrasElProyectoEstaEnPausa() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		llevarARevision(proyecto, diseno);
		proyecto.pausar(AHORA);

		assertThatThrownBy(() -> proyecto.aprobarComoCliente(diseno, CLIENTE, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	// --- Pausa (invariante 8) ---

	@Test
	void enPausaLosEntregablesNoSeMuevenHastaReanudar() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		EntregableId diseno = idDe(proyecto, "Diseño");
		proyecto.pausar(AHORA);

		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.PAUSADO);
		assertThatThrownBy(() -> proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_CURSO, null, AHORA))
				.isInstanceOf(ProyectoInvalidoException.class);

		proyecto.reanudar(AHORA);
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_CURSO, null, AHORA);
		assertThat(proyecto.entregable(diseno).estado()).isEqualTo(EstadoEntregable.EN_CURSO);
	}

	// --- Garantía (decisión 23) ---

	@Test
	void alAprobarseElUltimoEntregableEmpiezanSesentaDiasDeGarantia() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		aprobarComoEquipo(proyecto, idDe(proyecto, "Diseño"));
		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.ACTIVO);

		EntregableId catalogo = idDe(proyecto, "Catálogo");
		llevarARevision(proyecto, catalogo);
		proyecto.aprobarComoCliente(catalogo, CLIENTE, DESPUES);

		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.EN_GARANTIA);
		assertThat(proyecto.finDeGarantia()).isEqualTo(DESPUES.plus(Duration.ofDays(60)));
		assertThat(proyecto.avance()).isEqualTo(100);
	}

	@Test
	void quitarElUnicoPendienteConTodoLoDemasAprobadoTambienEntraEnGarantia() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		aprobarComoEquipo(proyecto, idDe(proyecto, "Diseño"));

		proyecto.quitarEntregable(idDe(proyecto, "Catálogo"), DESPUES);

		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.EN_GARANTIA);
	}

	@Test
	void unCambioDeAlcanceEnGarantiaVuelveAActivarElProyecto() {
		Proyecto proyecto = enGarantia();

		proyecto.agregarEntregable(proyecto.fases().getFirst().id(),
				entregable("Pasarela de pagos", 2_000_000, MomentoDeCobro.AL_APROBAR), true, DESPUES);

		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.ACTIVO);
		assertThat(proyecto.finDeGarantia()).isNull();
	}

	@Test
	void enGarantiaSoloEntraTrabajoNuevoComoCambioDeAlcance() {
		Proyecto proyecto = enGarantia();

		assertThatThrownBy(() -> proyecto.agregarEntregable(proyecto.fases().getFirst().id(),
				entregable("Otra cosa", 1_000, MomentoDeCobro.AL_APROBAR), false, DESPUES))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void laGarantiaSeCierraSolaAlVencerYNoAntes() {
		Proyecto proyecto = enGarantia();
		Instant vence = proyecto.finDeGarantia();

		assertThat(proyecto.cerrarSiVencioLaGarantia(vence.minusSeconds(1))).isFalse();
		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.EN_GARANTIA);

		assertThat(proyecto.cerrarSiVencioLaGarantia(vence)).isTrue();
		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.CERRADO);

		assertThat(proyecto.cerrarSiVencioLaGarantia(vence.plusSeconds(1))).isFalse();
	}

	@Test
	void elEquipoPuedeCerrarAntesDeQueVenzaLaGarantia() {
		Proyecto proyecto = enGarantia();

		proyecto.cerrar(DESPUES);

		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.CERRADO);
	}

	@Test
	void unProyectoConTrabajoPendienteNoSeCierra() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);

		assertThatThrownBy(() -> proyecto.cerrar(AHORA)).isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	@Test
	void unProyectoCerradoYaNoCambiaSuPlan() {
		Proyecto proyecto = enGarantia();
		proyecto.cerrar(DESPUES);

		assertThatThrownBy(() -> proyecto.agregarFase(ProyectosDePrueba.fase("Otra"), DESPUES))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThatThrownBy(() -> proyecto.agregarEntregable(proyecto.fases().getFirst().id(),
				entregable("Extra", 1_000, MomentoDeCobro.AL_APROBAR), true, DESPUES))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	private static Proyecto enGarantia() {
		Proyecto proyecto = enBlancoConDosEntregables(IVA);
		aprobarComoEquipo(proyecto, idDe(proyecto, "Diseño"));
		aprobarComoEquipo(proyecto, idDe(proyecto, "Catálogo"));
		assertThat(proyecto.estado()).isEqualTo(EstadoProyecto.EN_GARANTIA);
		return proyecto;
	}

}
