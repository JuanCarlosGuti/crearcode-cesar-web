package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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
	private List<Object> eventosPublicados;
	private RegistrarSolicitudUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		repositorio = new FakeSolicitudRepositorio();
		eventosPublicados = new ArrayList<>();
		Clock reloj = Clock.fixed(Instant.parse("2026-07-16T10:00:00Z"), ZoneOffset.UTC);
		useCase = new RegistrarSolicitudUseCaseImpl(repositorio, eventosPublicados::add, reloj);
	}

	private List<SolicitudRegistrada> solicitudesAnunciadas() {
		return eventosPublicados.stream().filter(SolicitudRegistrada.class::isInstance)
				.map(SolicitudRegistrada.class::cast).toList();
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

	/**
	 * El caso de uso ya no manda el correo: anuncia el registro y sigue.
	 * El aviso sale despues del commit y fuera de esta peticion (ver
	 * NotificarSolicitudRegistrada), para que un SMTP lento no tenga al
	 * visitante esperando por un correo que no es suyo.
	 */
	@Test
	void registrarAnunciaLaNuevaSolicitudSinMandarElCorreoEnLinea() {
		SolicitudId id = useCase.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje", consentimientoAceptado());

		assertThat(solicitudesAnunciadas()).hasSize(1);
		assertThat(solicitudesAnunciadas().getFirst().solicitud().id()).isEqualTo(id);
	}

	@Test
	void sinConsentimientoAceptadoNoPersisteNiNotifica() {
		ConsentimientoDatos noAceptado = new ConsentimientoDatos(false, Instant.now(), "v1");

		assertThatThrownBy(() -> useCase.registrar(DATOS, ServicioDeInteres.OTRO, "mensaje", noAceptado))
				.isInstanceOf(ConsentimientoRequeridoException.class);

		assertThat(repositorio.listar()).isEmpty();
		assertThat(solicitudesAnunciadas()).isEmpty();
	}

}
