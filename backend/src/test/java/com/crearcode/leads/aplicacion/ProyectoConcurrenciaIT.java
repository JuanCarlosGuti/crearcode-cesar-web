package com.crearcode.leads.aplicacion;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.GestionarProyectoUseCase;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Leer, cambiar y guardar el agregado entero tiene un riesgo: si dos
 * peticiones leen a la vez, la segunda en guardar borra lo que hizo la
 * primera. Con pagos de por medio eso es dinero que desaparece del
 * portal. Los casos de uso que modifican bloquean el proyecto al leerlo
 * (SELECT ... FOR UPDATE), y esto lo prueba contra PostgreSQL real.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProyectoConcurrenciaIT {

	@Autowired
	private GestionarProyectoUseCase gestionar;

	@Autowired
	private ProyectoRepositorio proyectos;

	@Test
	void diezPagosSimultaneosAlMismoProyectoQuedanTodosRegistrados() throws Exception {
		Proyecto proyecto = Proyecto.enBlanco(new Correo("concurrencia@ejemplo.co"), "Cliente de prueba",
				new DescripcionDelProyecto("Proyecto concurrido", null, LocalDate.of(2026, 10, 1), null),
				new Porcentaje(0),
				List.of(new NuevaFase(new DatosDeFase("Única", null, null, null), List.of(
						new DatosDeEntregable("Todo", null, Dinero.de(1_000_000), MomentoDeCobro.AL_INICIAR)))),
				java.time.Instant.parse("2026-09-01T15:00:00Z"));
		proyectos.guardar(proyecto);
		EntregableId todo = proyecto.entregables().getFirst().id();
		int pagos = 10;

		try (ExecutorService pool = Executors.newFixedThreadPool(pagos)) {
			List<Callable<Proyecto>> tareas = Collections.nCopies(pagos,
					() -> gestionar.registrarPago(proyecto.id(), todo, Dinero.de(1_000), LocalDate.of(2026, 9, 1),
							MedioDePago.TRANSFERENCIA, null, new Correo("admin@crearcodecesar.com")));
			for (Future<Proyecto> resultado : pool.invokeAll(tareas)) {
				resultado.get();
			}
		}

		Proyecto guardado = proyectos.buscarPorId(proyecto.id()).orElseThrow();
		assertThat(guardado.pagos()).hasSize(pagos);
		assertThat(guardado.totalPagado()).isEqualTo(Dinero.de(10_000));
	}

}
