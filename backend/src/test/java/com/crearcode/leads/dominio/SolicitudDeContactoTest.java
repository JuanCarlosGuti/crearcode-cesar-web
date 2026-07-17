package com.crearcode.leads.dominio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolicitudDeContactoTest {

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", "Empresa S.A.S.", new Correo("nombre@empresa.com"), new Telefono("3001234567"));
	private static final ConsentimientoDatos CONSENTIMIENTO_ACEPTADO =
			new ConsentimientoDatos(true, Instant.now(), "v1");

	@Test
	void registrarConDatosValidosCreaSolicitudEnEstadoNueva() {
		Instant ahora = Instant.now();

		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.IA_Y_AUTOMATIZACION, "Quiero automatizar mi negocio",
				CONSENTIMIENTO_ACEPTADO, ahora);

		assertThat(solicitud.id()).isNotNull();
		assertThat(solicitud.datosDeContacto()).isEqualTo(DATOS);
		assertThat(solicitud.servicioDeInteres()).isEqualTo(ServicioDeInteres.IA_Y_AUTOMATIZACION);
		assertThat(solicitud.mensaje()).isEqualTo("Quiero automatizar mi negocio");
		assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.NUEVA);
		assertThat(solicitud.consentimiento()).isEqualTo(CONSENTIMIENTO_ACEPTADO);
		assertThat(solicitud.fechaCreacion()).isEqualTo(ahora);
		assertThat(solicitud.fechaUltimaActualizacion()).isEqualTo(ahora);
	}

	@Test
	void cadaRegistroGeneraUnIdDistinto() {
		SolicitudDeContacto primera = SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "mensaje", CONSENTIMIENTO_ACEPTADO, Instant.now());
		SolicitudDeContacto segunda = SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "mensaje", CONSENTIMIENTO_ACEPTADO, Instant.now());

		assertThat(primera.id()).isNotEqualTo(segunda.id());
	}

	@Test
	void rechazaRegistroSinConsentimientoAceptado() {
		ConsentimientoDatos noAceptado = new ConsentimientoDatos(false, Instant.now(), "v1");

		assertThatThrownBy(() -> SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "mensaje", noAceptado, Instant.now()))
				.isInstanceOf(ConsentimientoRequeridoException.class);
	}

	@Test
	void rechazaMensajeNulo() {
		assertThatThrownBy(() -> SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, null, CONSENTIMIENTO_ACEPTADO, Instant.now()))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void rechazaMensajeVacioOSoloEspacios() {
		assertThatThrownBy(() -> SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "   ", CONSENTIMIENTO_ACEPTADO, Instant.now()))
				.isInstanceOf(DatosDeContactoInvalidosException.class);
	}

	@Test
	void cambiarEstadoConTransicionValidaActualizaEstadoYFecha() {
		Instant creacion = Instant.now();
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "mensaje", CONSENTIMIENTO_ACEPTADO, creacion);
		Instant actualizacion = creacion.plus(1, ChronoUnit.HOURS);

		solicitud.cambiarEstado(EstadoSolicitud.CONTACTADA, actualizacion);

		assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.CONTACTADA);
		assertThat(solicitud.fechaUltimaActualizacion()).isEqualTo(actualizacion);
		assertThat(solicitud.fechaCreacion()).isEqualTo(creacion);
	}

	@Test
	void cambiarEstadoConTransicionInvalidaLanzaExcepcionYNoModificaElEstado() {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "mensaje", CONSENTIMIENTO_ACEPTADO, Instant.now());

		assertThatThrownBy(() -> solicitud.cambiarEstado(EstadoSolicitud.CONVERTIDA, Instant.now()))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
		assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.NUEVA);
	}

	@Test
	void cambiarEstadoDesdeUnEstadoTerminalLanzaExcepcion() {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				DATOS, ServicioDeInteres.OTRO, "mensaje", CONSENTIMIENTO_ACEPTADO, Instant.now());
		solicitud.cambiarEstado(EstadoSolicitud.CONTACTADA, Instant.now());
		solicitud.cambiarEstado(EstadoSolicitud.CONVERTIDA, Instant.now());

		assertThatThrownBy(() -> solicitud.cambiarEstado(EstadoSolicitud.DESCARTADA, Instant.now()))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	@Test
	void reconstruirRecreaExactamenteElEstadoPersistido() {
		SolicitudId id = SolicitudId.nuevo();
		Instant creacion = Instant.parse("2026-01-01T00:00:00Z");
		Instant actualizacion = Instant.parse("2026-01-02T00:00:00Z");

		SolicitudDeContacto solicitud = SolicitudDeContacto.reconstruir(id, DATOS,
				ServicioDeInteres.SOLUCIONES_TECNOLOGICAS, "mensaje", EstadoSolicitud.CONTACTADA,
				CONSENTIMIENTO_ACEPTADO, creacion, actualizacion);

		assertThat(solicitud.id()).isEqualTo(id);
		assertThat(solicitud.datosDeContacto()).isEqualTo(DATOS);
		assertThat(solicitud.servicioDeInteres()).isEqualTo(ServicioDeInteres.SOLUCIONES_TECNOLOGICAS);
		assertThat(solicitud.mensaje()).isEqualTo("mensaje");
		assertThat(solicitud.estado()).isEqualTo(EstadoSolicitud.CONTACTADA);
		assertThat(solicitud.consentimiento()).isEqualTo(CONSENTIMIENTO_ACEPTADO);
		assertThat(solicitud.fechaCreacion()).isEqualTo(creacion);
		assertThat(solicitud.fechaUltimaActualizacion()).isEqualTo(actualizacion);
	}

}
