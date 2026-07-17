package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.ConsentimientoRequeridoException;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrarSolicitudUseCaseTest {

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", "Empresa S.A.S.", new Correo("nombre@empresa.com"), new Telefono("3001234567"));

	private FakeSolicitudRepositorio repositorio;
	private FakeNotificadorPort notificador;
	private RegistrarSolicitudUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		repositorio = new FakeSolicitudRepositorio();
		notificador = new FakeNotificadorPort();
		Clock reloj = Clock.fixed(Instant.parse("2026-07-16T10:00:00Z"), ZoneOffset.UTC);
		useCase = new RegistrarSolicitudUseCaseImpl(repositorio, notificador, reloj);
	}

	private ConsentimientoDatos consentimientoAceptado() {
		return new ConsentimientoDatos(true, Instant.now(), "v1");
	}

	@Test
	void registrarPersisteLaSolicitud() {
		SolicitudId id = useCase.registrar(DATOS, ServicioDeInteres.IA_Y_AUTOMATIZACION,
				"Quiero automatizar mi negocio", consentimientoAceptado());

		assertThat(repositorio.buscarPorId(id)).isPresent();
		assertThat(repositorio.buscarPorId(id).orElseThrow().estado()).isEqualTo(EstadoSolicitud.NUEVA);
	}

	@Test
	void registrarNotificaLaNuevaSolicitud() {
		SolicitudId id = useCase.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje", consentimientoAceptado());

		assertThat(notificador.notificadas).hasSize(1);
		assertThat(notificador.notificadas.get(0).id()).isEqualTo(id);
	}

	@Test
	void siFallaLaNotificacionLaSolicitudQuedaPersistida() {
		notificador.fallarAlNotificar = true;

		SolicitudId id = useCase.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje", consentimientoAceptado());

		assertThat(repositorio.buscarPorId(id)).isPresent();
	}

	@Test
	void sinConsentimientoAceptadoNoPersisteNiNotifica() {
		ConsentimientoDatos noAceptado = new ConsentimientoDatos(false, Instant.now(), "v1");

		assertThatThrownBy(() -> useCase.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje", noAceptado))
				.isInstanceOf(ConsentimientoRequeridoException.class);

		assertThat(repositorio.listar()).isEmpty();
		assertThat(notificador.notificadas).isEmpty();
	}

}
