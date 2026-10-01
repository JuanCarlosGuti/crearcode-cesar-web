package com.crearcode.leads.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.Usuario;

import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.CLIENTE;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.OTRO_CLIENTE;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.proyectoConDosEntregables;
import static org.assertj.core.api.Assertions.assertThat;

class ConsultarProyectosUseCaseImplTest {

	private FakeProyectoRepositorio proyectos;
	private FakeUsuarioRepositorio usuarios;
	private ConsultarProyectosUseCaseImpl useCase;
	private Proyecto proyecto;

	@BeforeEach
	void configurar() {
		proyectos = new FakeProyectoRepositorio();
		usuarios = new FakeUsuarioRepositorio();
		useCase = new ConsultarProyectosUseCaseImpl(proyectos, usuarios);
		proyecto = proyectoConDosEntregables();
		proyectos.guardar(proyecto);
	}

	@Test
	void elClienteSoloVeLosSuyos() {
		assertThat(useCase.listarDe(CLIENTE)).containsExactly(proyecto);
		assertThat(useCase.listarDe(OTRO_CLIENTE)).isEmpty();
	}

	@Test
	void unProyectoAjenoSeComportaComoSiNoExistiera() {
		assertThat(useCase.obtenerDe(proyecto.id(), CLIENTE)).contains(proyecto);
		assertThat(useCase.obtenerDe(proyecto.id(), OTRO_CLIENTE)).isEmpty();
	}

	@Test
	void elEquipoVeTodos() {
		assertThat(useCase.listar()).containsExactly(proyecto);
		assertThat(useCase.obtener(proyecto.id())).contains(proyecto);
	}

	@Test
	void sabeSiElCorreoDelClienteYaTieneCuenta() {
		assertThat(useCase.clienteTieneCuenta(CLIENTE)).isFalse();

		usuarios.guardar(Usuario.registrarCliente(new Correo("Cliente@Ejemplo.co"), "hash"));

		assertThat(useCase.clienteTieneCuenta(CLIENTE)).isTrue();
	}

}
