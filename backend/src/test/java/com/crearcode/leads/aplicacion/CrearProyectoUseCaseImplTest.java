package com.crearcode.leads.aplicacion;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Proyecto;

import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.AHORA;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.CLIENTE;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.IVA;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.RELOJ;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.cotizacionAceptada;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.descripcion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrearProyectoUseCaseImplTest {

	private FakeCotizacionRepositorio cotizaciones;
	private FakeProyectoRepositorio proyectos;
	private CrearProyectoUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		cotizaciones = new FakeCotizacionRepositorio();
		proyectos = new FakeProyectoRepositorio();
		useCase = new CrearProyectoUseCaseImpl(proyectos, cotizaciones, RELOJ);
	}

	private Cotizacion guardada(Cotizacion cotizacion) {
		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	/** Decisión 21 como valor por defecto: el primero al iniciar, el resto al aprobar. */
	@Test
	void proponeUnEntregablePorItemConElAnticipoEnElPrimero() {
		Cotizacion cotizacion = guardada(cotizacionAceptada());

		List<NuevaFase> plan = useCase.proponerPlan(cotizacion.id());

		assertThat(plan).hasSize(1);
		assertThat(plan.getFirst().entregables())
				.extracting(DatosDeEntregable::nombre, DatosDeEntregable::valor, DatosDeEntregable::cobro)
				.containsExactly(
						org.assertj.core.groups.Tuple.tuple("Módulo de diseño", Dinero.de(2_000_000),
								MomentoDeCobro.AL_INICIAR),
						org.assertj.core.groups.Tuple.tuple("Catálogo de productos", Dinero.de(3_000_000),
								MomentoDeCobro.AL_APROBAR));
	}

	/** Un ítem admite 200 caracteres y un entregable 120: la propuesta no puede romperse por eso. */
	@Test
	void recortaLasDescripcionesLargasDeLosItems() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(new DatosDelCliente("Café Valle", CLIENTE, null, null), IVA,
				AHORA.minusSeconds(3600), AHORA.plusSeconds(86_400), null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("x".repeat(200), 1, Dinero.de(1_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 3), AHORA.minusSeconds(60));
		cotizacion.aceptar(AHORA.minusSeconds(30));
		guardada(cotizacion);

		List<NuevaFase> plan = useCase.proponerPlan(cotizacion.id());

		assertThat(plan.getFirst().entregables().getFirst().nombre()).hasSize(120);
	}

	@Test
	void unaCotizacionQueNoExisteSeRechaza() {
		CotizacionId inexistente = CotizacionId.nuevo();

		assertThatThrownBy(() -> useCase.proponerPlan(inexistente))
				.isInstanceOf(CotizacionNoEncontradaException.class);
		assertThatThrownBy(() -> useCase.desdeCotizacion(inexistente, descripcion(), List.of()))
				.isInstanceOf(CotizacionNoEncontradaException.class);
	}

	@Test
	void creaYGuardaElProyectoDesdeLaCotizacion() {
		Cotizacion cotizacion = guardada(cotizacionAceptada());

		Proyecto proyecto = useCase.desdeCotizacion(cotizacion.id(), descripcion(),
				useCase.proponerPlan(cotizacion.id()));

		assertThat(proyectos.buscarPorId(proyecto.id())).isPresent();
		assertThat(proyecto.origen()).isEqualTo(cotizacion.id());
		assertThat(proyecto.creadoEn()).isEqualTo(AHORA);
	}

	/** Invariante 6: un solo proyecto por cotización. */
	@Test
	void unaCotizacionNoDaParaDosProyectos() {
		Cotizacion cotizacion = guardada(cotizacionAceptada());
		useCase.desdeCotizacion(cotizacion.id(), descripcion(), useCase.proponerPlan(cotizacion.id()));

		assertThatThrownBy(() -> useCase.desdeCotizacion(cotizacion.id(), descripcion(),
				useCase.proponerPlan(cotizacion.id())))
				.isInstanceOf(ProyectoYaExisteParaLaCotizacionException.class);
		assertThat(proyectos.listar()).hasSize(1);
	}

	@Test
	void creaYGuardaUnProyectoEnBlanco() {
		Proyecto proyecto = useCase.enBlanco(CLIENTE, "Café Valle S.A.S.", descripcion(), IVA, List.of());

		assertThat(proyectos.buscarPorId(proyecto.id())).isPresent();
		assertThat(proyecto.origen()).isNull();
	}

}
