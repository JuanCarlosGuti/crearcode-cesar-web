package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.Pago;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoInvalidoException;

import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.AHORA;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.EQUIPO;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.HOY;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.RELOJ;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.idDe;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.proyectoConDosEntregables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestionarProyectoUseCaseImplTest {

	private FakeProyectoRepositorio proyectos;
	private List<Object> eventos;
	private GestionarProyectoUseCaseImpl useCase;
	private Proyecto proyecto;

	@BeforeEach
	void configurar() {
		proyectos = new FakeProyectoRepositorio();
		eventos = new ArrayList<>();
		useCase = new GestionarProyectoUseCaseImpl(proyectos, eventos::add, RELOJ);
		proyecto = proyectoConDosEntregables();
		proyectos.guardar(proyecto);
	}

	@Test
	void unProyectoQueNoExisteSeRechaza() {
		assertThatThrownBy(() -> useCase.agregarFase(ProyectoId.nuevo(), new DatosDeFase("Fase 2", null, null, null)))
				.isInstanceOf(ProyectoNoEncontradoException.class);
	}

	@Test
	void cadaCambioSeGuardaConLaHoraDelReloj() {
		Proyecto actualizado = useCase.agregarFase(proyecto.id(), new DatosDeFase("Fase 2", null, null, null));

		assertThat(actualizado.fases()).hasSize(2);
		assertThat(actualizado.actualizadoEn()).isEqualTo(AHORA);
		assertThat(proyectos.buscarPorId(proyecto.id()).orElseThrow().fases()).hasSize(2);
	}

	@Test
	void unCambioQueElDominioRechazaNoSeGuarda() {
		int guardadosAntes = proyectos.vecesGuardado();

		assertThatThrownBy(() -> useCase.quitarFase(proyecto.id(), proyecto.fases().getFirst().id()))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThat(proyectos.vecesGuardado()).isEqualTo(guardadosAntes);
	}

	/** HU-52: pasar a revisión es lo que le avisa al cliente. */
	@Test
	void pasarUnEntregableARevisionPublicaElAviso() {
		EntregableId diseno = idDe(proyecto, "Diseño");
		useCase.cambiarEstadoDeEntregable(proyecto.id(), diseno, EstadoEntregable.EN_CURSO, null);
		assertThat(eventos).isEmpty();

		useCase.cambiarEstadoDeEntregable(proyecto.id(), diseno, EstadoEntregable.EN_REVISION, null);

		assertThat(eventos).singleElement().isEqualTo(new EntregableListoParaRevisar(proyecto, diseno));
	}

	@Test
	void unPagoSeRegistraComoManualConQuienLoRegistroYSePublica() {
		EntregableId diseno = idDe(proyecto, "Diseño");

		Proyecto actualizado = useCase.registrarPago(proyecto.id(), diseno, Dinero.de(500_000), HOY,
				MedioDePago.TRANSFERENCIA, "Transferencia 998", EQUIPO);

		Pago pago = actualizado.pagos().getFirst();
		assertThat(pago.origen()).isEqualTo(OrigenDePago.MANUAL);
		assertThat(pago.registradoPor()).isEqualTo(EQUIPO);
		assertThat(eventos).singleElement().isEqualTo(new PagoRegistrado(actualizado, pago.id()));
	}

	/** Un pago con fecha de mañana es un error de digitación, no un pago. */
	@Test
	void unPagoConFechaFuturaSeRechaza() {
		assertThatThrownBy(() -> useCase.registrarPago(proyecto.id(), idDe(proyecto, "Diseño"), Dinero.de(1_000),
				HOY.plusDays(1), MedioDePago.EFECTIVO, null, EQUIPO))
				.isInstanceOf(ProyectoInvalidoException.class);
		assertThat(eventos).isEmpty();
	}

	@Test
	void seguimientoDelEstadoDelProyecto() {
		assertThat(useCase.pausar(proyecto.id()).estado()).isEqualTo(EstadoProyecto.PAUSADO);
		assertThat(useCase.reanudar(proyecto.id()).estado()).isEqualTo(EstadoProyecto.ACTIVO);
	}

}
