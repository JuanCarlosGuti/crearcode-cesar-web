package com.crearcode.leads.infraestructura;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Entregable;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El proyecto de demostración de F12 (ISS-214): lo que el dueño recorre
 * en local para ver el portal como lo vería un cliente. Nada de datos de
 * personas reales, y nunca en producción.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.demo.proyecto-de-demostracion=true")
class CargadorDeProyectoDeDemostracionIT {

	@Autowired
	private CargadorDeProyectoDeDemostracion cargador;

	@Autowired
	private ProyectoRepositorio proyectos;

	@Autowired
	private UsuarioRepositorio usuarios;

	private static final Correo CLIENTE_DEMO = new Correo(CargadorDeProyectoDeDemostracion.CORREO_DEL_CLIENTE);

	@Test
	void cargaUnProyectoConEstadosVariadosYUnClienteQuePuedeEntrar() {
		Proyecto proyecto = proyectos.listarPorCorreoDelCliente(CLIENTE_DEMO).getFirst();

		assertThat(proyecto.fases()).hasSize(3);
		assertThat(proyecto.entregables()).hasSize(7);
		assertThat(proyecto.pagos()).hasSize(2);
		assertThat(proyecto.entregables()).extracting(Entregable::estado)
				.contains(EstadoEntregable.APROBADO, EstadoEntregable.EN_REVISION, EstadoEntregable.EN_CURSO,
						EstadoEntregable.CON_AJUSTES, EstadoEntregable.PENDIENTE);
		assertThat(proyecto.entregables()).anyMatch(Entregable::esCambioDeAlcance);
		assertThat(proyecto.avance()).isBetween(1, 99);

		Usuario cliente = usuarios.buscarPorCorreo(CLIENTE_DEMO).orElseThrow();
		assertThat(cliente.rol()).isEqualTo(Rol.CLIENTE);
		assertThat(cliente.verificado()).isTrue();
	}

	@Test
	void correrloOtraVezNoDuplicaNada() throws Exception {
		cargador.run(null);

		assertThat(proyectos.listarPorCorreoDelCliente(CLIENTE_DEMO)).hasSize(1);
	}

	/**
	 * La cuenta de demostración tiene una contraseña que está en el
	 * repositorio. El despliegue no puede encender la carga, y el valor
	 * por defecto de la aplicación es apagado.
	 */
	@Test
	void elDespliegueNoLaEnciendeYPorDefectoEstaApagada() throws Exception {
		assertThat(Files.readString(Path.of("..", "config", "deploy.api.yml")))
				.doesNotContainIgnoringCase("demostracion");
		assertThat(Files.readString(Path.of("src", "main", "resources", "application.properties")))
				.contains("app.demo.proyecto-de-demostracion=${CARGAR_PROYECTO_DE_DEMOSTRACION:false}");
	}

}
