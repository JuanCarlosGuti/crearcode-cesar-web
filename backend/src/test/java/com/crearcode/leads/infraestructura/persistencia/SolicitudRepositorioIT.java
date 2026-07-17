package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.SolicitudRepositorio;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class SolicitudRepositorioIT {

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", "Empresa S.A.S.", new Correo("nombre@empresa.com"), new Telefono("3001234567"));

	@Autowired
	private SolicitudRepositorio repositorio;

	private SolicitudDeContacto registrar() {
		return SolicitudDeContacto.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje",
				new ConsentimientoDatos(true, Instant.now(), "v1"), Instant.now());
	}

	@Test
	void guardarYBuscarPorIdDevuelveLaMismaSolicitud() {
		SolicitudDeContacto solicitud = registrar();

		repositorio.guardar(solicitud);

		SolicitudDeContacto encontrada = repositorio.buscarPorId(solicitud.id()).orElseThrow();
		assertThat(encontrada.id()).isEqualTo(solicitud.id());
		assertThat(encontrada.datosDeContacto()).isEqualTo(solicitud.datosDeContacto());
		assertThat(encontrada.estado()).isEqualTo(EstadoSolicitud.NUEVA);
	}

	@Test
	void buscarPorIdInexistenteDevuelveVacio() {
		assertThat(repositorio.buscarPorId(SolicitudId.nuevo())).isEmpty();
	}

	@Test
	void listarPorEstadoFiltraCorrectamente() {
		SolicitudDeContacto nueva = registrar();
		SolicitudDeContacto contactada = registrar();
		contactada.cambiarEstado(EstadoSolicitud.CONTACTADA, Instant.now());
		repositorio.guardar(nueva);
		repositorio.guardar(contactada);

		assertThat(repositorio.listarPorEstado(EstadoSolicitud.CONTACTADA))
				.extracting(SolicitudDeContacto::id)
				.containsExactly(contactada.id());
	}

	@Test
	void guardarUnaSolicitudExistenteActualizaEnVezDeDuplicar() {
		SolicitudDeContacto solicitud = registrar();
		repositorio.guardar(solicitud);

		solicitud.cambiarEstado(EstadoSolicitud.CONTACTADA, Instant.now());
		repositorio.guardar(solicitud);

		assertThat(repositorio.buscarPorId(solicitud.id()).orElseThrow().estado())
				.isEqualTo(EstadoSolicitud.CONTACTADA);
	}

}
