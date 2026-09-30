package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.Proyecto;

import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.AHORA;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.BOGOTA;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.idDe;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.llevarARevision;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.proyectoConDosEntregables;
import static org.assertj.core.api.Assertions.assertThat;

/** Decisión 23: a los 60 días de la entrega final, la garantía se cierra sola. */
class CerrarGarantiasVencidasUseCaseImplTest {

	private FakeProyectoRepositorio proyectos;

	@BeforeEach
	void configurar() {
		proyectos = new FakeProyectoRepositorio();
	}

	private Proyecto enGarantiaDesde(Instant aprobado) {
		Proyecto proyecto = proyectoConDosEntregables();
		for (String nombre : new String[] { "Diseño", "Catálogo" }) {
			EntregableId id = idDe(proyecto, nombre);
			llevarARevision(proyecto, id);
			proyecto.cambiarEstadoDeEntregable(id, EstadoEntregable.APROBADO, null, aprobado);
		}
		proyectos.guardar(proyecto);
		return proyecto;
	}

	private CerrarGarantiasVencidasUseCaseImpl useCaseEn(Instant ahora) {
		return new CerrarGarantiasVencidasUseCaseImpl(proyectos, Clock.fixed(ahora, BOGOTA));
	}

	@Test
	void cierraSoloLasGarantiasQueYaVencieron() {
		Proyecto vencida = enGarantiaDesde(AHORA.minus(Duration.ofDays(61)));
		Proyecto vigente = enGarantiaDesde(AHORA.minus(Duration.ofDays(10)));
		Proyecto activo = proyectoConDosEntregables();
		proyectos.guardar(activo);

		int cerradas = useCaseEn(AHORA).cerrar();

		assertThat(cerradas).isEqualTo(1);
		assertThat(proyectos.buscarPorId(vencida.id()).orElseThrow().estado()).isEqualTo(EstadoProyecto.CERRADO);
		assertThat(proyectos.buscarPorId(vigente.id()).orElseThrow().estado()).isEqualTo(EstadoProyecto.EN_GARANTIA);
		assertThat(proyectos.buscarPorId(activo.id()).orElseThrow().estado()).isEqualTo(EstadoProyecto.ACTIVO);
	}

	@Test
	void correrloDosVecesNoCambiaNadaLaSegunda() {
		enGarantiaDesde(AHORA.minus(Duration.ofDays(61)));
		useCaseEn(AHORA).cerrar();
		int guardadosAntes = proyectos.vecesGuardado();

		assertThat(useCaseEn(AHORA).cerrar()).isZero();
		assertThat(proyectos.vecesGuardado()).isEqualTo(guardadosAntes);
	}

}
