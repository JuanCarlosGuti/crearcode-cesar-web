package com.crearcode.leads.infraestructura.persistencia;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;

/** Traducción explícita entre {@link SolicitudDeContacto} y {@link SolicitudJpaEntity}. */
final class SolicitudMapper {

	private SolicitudMapper() {
	}

	static SolicitudJpaEntity aEntidad(SolicitudDeContacto solicitud) {
		DatosDeContacto datos = solicitud.datosDeContacto();
		ConsentimientoDatos consentimiento = solicitud.consentimiento();

		return new SolicitudJpaEntity(
				solicitud.id().valor(),
				datos.nombre(),
				datos.empresa(),
				datos.correo().valor(),
				datos.telefono().valor(),
				solicitud.servicioDeInteres(),
				solicitud.mensaje(),
				solicitud.estado(),
				consentimiento.aceptado(),
				consentimiento.fechaAceptacion(),
				consentimiento.versionPoliticaAceptada(),
				solicitud.fechaCreacion(),
				solicitud.fechaUltimaActualizacion());
	}

	static SolicitudDeContacto aDominio(SolicitudJpaEntity entidad) {
		DatosDeContacto datos = new DatosDeContacto(
				entidad.getNombre(),
				entidad.getEmpresa(),
				new Correo(entidad.getCorreo()),
				new Telefono(entidad.getTelefono()));

		ConsentimientoDatos consentimiento = new ConsentimientoDatos(
				entidad.isConsentimientoAceptado(),
				entidad.getConsentimientoFechaAceptacion(),
				entidad.getConsentimientoVersionPolitica());

		return SolicitudDeContacto.reconstruir(
				new SolicitudId(entidad.getId()),
				datos,
				entidad.getServicioDeInteres(),
				entidad.getMensaje(),
				entidad.getEstado(),
				consentimiento,
				entidad.getFechaCreacion(),
				entidad.getFechaUltimaActualizacion());
	}

}
