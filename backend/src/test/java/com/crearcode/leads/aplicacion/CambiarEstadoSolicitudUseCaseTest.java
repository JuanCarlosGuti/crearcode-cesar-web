package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;
import com.crearcode.leads.dominio.TransicionDeEstadoInvalidaException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CambiarEstadoSolicitudUseCaseTest {

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", null, new Correo("nombre@empresa.com"), new Telefono("3001234567"));

	private FakeSolicitudRepositorio repositorio;
	private CambiarEstadoSolicitudUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		repositorio = new FakeSolicitudRepositorio();
		Clock reloj = Clock.fixed(Instant.parse("2026-07-16T10:00:00Z"), ZoneOffset.UTC);
		useCase = new CambiarEstadoSolicitudUseCaseImpl(repositorio, reloj);
	}

	private SolicitudDeContacto solicitudNueva() {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje",
				new ConsentimientoDatos(true, Instant.now(), "v1"), Instant.now());
		repositorio.guardar(solicitud);
		return solicitud;
	}

	@Test
	void cambiarEstadoConTransicionValidaActualizaYPersiste() {
		SolicitudDeContacto solicitud = solicitudNueva();

		useCase.cambiarEstado(solicitud.id(), EstadoSolicitud.CONTACTADA);

		assertThat(repositorio.buscarPorId(solicitud.id()).orElseThrow().estado())
				.isEqualTo(EstadoSolicitud.CONTACTADA);
	}

	@Test
	void cambiarEstadoConIdInexistenteLanzaSolicitudNoEncontrada() {
		SolicitudId idInexistente = new SolicitudId(UUID.randomUUID());

		assertThatThrownBy(() -> useCase.cambiarEstado(idInexistente, EstadoSolicitud.CONTACTADA))
				.isInstanceOf(SolicitudNoEncontradaException.class);
	}

	@Test
	void cambiarEstadoConTransicionInvalidaPropagaLaExcepcionDeDominio() {
		SolicitudDeContacto solicitud = solicitudNueva();

		assertThatThrownBy(() -> useCase.cambiarEstado(solicitud.id(), EstadoSolicitud.CONVERTIDA))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

}
