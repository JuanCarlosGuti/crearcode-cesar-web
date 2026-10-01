package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.QuienResponde;

import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.AHORA;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.CLIENTE;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.OTRO_CLIENTE;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.RELOJ;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.idDe;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.llevarARevision;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.proyectoConDosEntregables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HU-57, decisión 27: el cliente aprueba o pide ajustes desde su cuenta. */
class ResponderEntregableUseCaseImplTest {

	private FakeProyectoRepositorio proyectos;
	private List<Object> eventos;
	private ResponderEntregableUseCaseImpl useCase;
	private Proyecto proyecto;
	private EntregableId diseno;

	@BeforeEach
	void configurar() {
		proyectos = new FakeProyectoRepositorio();
		eventos = new ArrayList<>();
		useCase = new ResponderEntregableUseCaseImpl(proyectos, eventos::add, RELOJ);
		proyecto = proyectoConDosEntregables();
		diseno = idDe(proyecto, "Diseño");
		llevarARevision(proyecto, diseno);
		proyectos.guardar(proyecto);
	}

	@Test
	void elClienteApruebaYElEquipoSeEntera() {
		Proyecto actualizado = useCase.aprobar(proyecto.id(), diseno, CLIENTE);

		assertThat(actualizado.entregable(diseno).estado()).isEqualTo(EstadoEntregable.APROBADO);
		assertThat(actualizado.entregable(diseno).aprobadoPor()).isEqualTo(QuienResponde.CLIENTE);
		assertThat(actualizado.entregable(diseno).aprobadoEn()).isEqualTo(AHORA);
		assertThat(eventos).singleElement()
				.isEqualTo(new EntregableRespondidoPorElCliente(actualizado, diseno, true));
	}

	@Test
	void elClientePideAjustesYElEquipoSeEntera() {
		Proyecto actualizado = useCase.pedirAjustes(proyecto.id(), diseno, CLIENTE, "El logo más grande");

		assertThat(actualizado.entregable(diseno).estado()).isEqualTo(EstadoEntregable.CON_AJUSTES);
		assertThat(eventos).singleElement()
				.isEqualTo(new EntregableRespondidoPorElCliente(actualizado, diseno, false));
	}

	/** Invariante 7: un proyecto ajeno se comporta como si no existiera (404, no 403). */
	@Test
	void unClienteNoRespondeElProyectoDeOtroNiSabeQueExiste() {
		assertThatThrownBy(() -> useCase.aprobar(proyecto.id(), diseno, OTRO_CLIENTE))
				.isInstanceOf(ProyectoNoEncontradoException.class);

		assertThat(proyecto.entregable(diseno).estado()).isEqualTo(EstadoEntregable.EN_REVISION);
		assertThat(eventos).isEmpty();
	}

}
