package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitudMapperTest {

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", "Empresa S.A.S.", new Correo("nombre@empresa.com"), new Telefono("3001234567"));
	private static final ConsentimientoDatos CONSENTIMIENTO = new ConsentimientoDatos(true, Instant.now(), "v1");

	@Test
	void aEntidadMapeaTodosLosCampos() {
		SolicitudId id = SolicitudId.nuevo();
		Instant creacion = Instant.parse("2026-01-01T00:00:00Z");
		Instant actualizacion = Instant.parse("2026-01-02T00:00:00Z");
		SolicitudDeContacto solicitud = SolicitudDeContacto.reconstruir(id, DATOS,
				ServicioDeInteres.IA_Y_AUTOMATIZACION, "mensaje", EstadoSolicitud.CONTACTADA, CONSENTIMIENTO,
				creacion, actualizacion);

		SolicitudJpaEntity entidad = SolicitudMapper.aEntidad(solicitud);

		assertThat(entidad.getId()).isEqualTo(id.valor());
		assertThat(entidad.getNombre()).isEqualTo("Juan Pérez");
		assertThat(entidad.getEmpresa()).isEqualTo("Empresa S.A.S.");
		assertThat(entidad.getCorreo()).isEqualTo("nombre@empresa.com");
		assertThat(entidad.getTelefono()).isEqualTo("3001234567");
		assertThat(entidad.getServicioDeInteres()).isEqualTo(ServicioDeInteres.IA_Y_AUTOMATIZACION);
		assertThat(entidad.getMensaje()).isEqualTo("mensaje");
		assertThat(entidad.getEstado()).isEqualTo(EstadoSolicitud.CONTACTADA);
		assertThat(entidad.isConsentimientoAceptado()).isTrue();
		assertThat(entidad.getConsentimientoVersionPolitica()).isEqualTo("v1");
		assertThat(entidad.getFechaCreacion()).isEqualTo(creacion);
		assertThat(entidad.getFechaUltimaActualizacion()).isEqualTo(actualizacion);
	}

	@Test
	void mapeoIdaYVueltaPreservaLosDatos() {
		SolicitudDeContacto original = SolicitudDeContacto.reconstruir(SolicitudId.nuevo(), DATOS,
				ServicioDeInteres.OTRO, "mensaje", EstadoSolicitud.DESCARTADA, CONSENTIMIENTO,
				Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-03T00:00:00Z"));

		SolicitudDeContacto reconstruida = SolicitudMapper.aDominio(SolicitudMapper.aEntidad(original));

		assertThat(reconstruida.id()).isEqualTo(original.id());
		assertThat(reconstruida.datosDeContacto()).isEqualTo(original.datosDeContacto());
		assertThat(reconstruida.servicioDeInteres()).isEqualTo(original.servicioDeInteres());
		assertThat(reconstruida.mensaje()).isEqualTo(original.mensaje());
		assertThat(reconstruida.estado()).isEqualTo(original.estado());
		assertThat(reconstruida.consentimiento()).isEqualTo(original.consentimiento());
		assertThat(reconstruida.fechaCreacion()).isEqualTo(original.fechaCreacion());
		assertThat(reconstruida.fechaUltimaActualizacion()).isEqualTo(original.fechaUltimaActualizacion());
	}

}
