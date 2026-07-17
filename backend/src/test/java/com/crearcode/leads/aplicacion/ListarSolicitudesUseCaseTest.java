package com.crearcode.leads.aplicacion;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;

class ListarSolicitudesUseCaseTest {

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", null, new Correo("nombre@empresa.com"), new Telefono("3001234567"));

	private FakeSolicitudRepositorio repositorio;
	private ListarSolicitudesUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		repositorio = new FakeSolicitudRepositorio();
		useCase = new ListarSolicitudesUseCaseImpl(repositorio);

		SolicitudDeContacto nueva = registrar();
		SolicitudDeContacto contactada = registrar();
		contactada.cambiarEstado(EstadoSolicitud.CONTACTADA, Instant.now());
		repositorio.guardar(nueva);
		repositorio.guardar(contactada);
	}

	private SolicitudDeContacto registrar() {
		return SolicitudDeContacto.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje",
				new ConsentimientoDatos(true, Instant.now(), "v1"), Instant.now());
	}

	@Test
	void listarDevuelveTodasLasSolicitudes() {
		assertThat(useCase.listar()).hasSize(2);
	}

	@Test
	void listarPorEstadoDelegaElFiltroAlRepositorio() {
		assertThat(useCase.listarPorEstado(EstadoSolicitud.CONTACTADA)).hasSize(1);
		assertThat(useCase.listarPorEstado(EstadoSolicitud.DESCARTADA)).isEmpty();
	}

}
