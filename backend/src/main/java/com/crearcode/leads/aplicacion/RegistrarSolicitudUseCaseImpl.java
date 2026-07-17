package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.NotificadorPort;
import com.crearcode.leads.dominio.RegistrarSolicitudUseCase;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.SolicitudRepositorio;

@Service
class RegistrarSolicitudUseCaseImpl implements RegistrarSolicitudUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(RegistrarSolicitudUseCaseImpl.class);

	private final SolicitudRepositorio repositorio;
	private final NotificadorPort notificador;
	private final Clock reloj;

	RegistrarSolicitudUseCaseImpl(SolicitudRepositorio repositorio, NotificadorPort notificador, Clock reloj) {
		this.repositorio = repositorio;
		this.notificador = notificador;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public SolicitudId registrar(DatosDeContacto datosDeContacto, ServicioDeInteres servicioDeInteres,
			String mensaje, ConsentimientoDatos consentimiento) {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				datosDeContacto, servicioDeInteres, mensaje, consentimiento, Instant.now(reloj));

		repositorio.guardar(solicitud);
		notificar(solicitud);

		return solicitud.id();
	}

	private void notificar(SolicitudDeContacto solicitud) {
		try {
			notificador.notificarNuevaSolicitud(solicitud);
		} catch (RuntimeException excepcion) {
			// La notificación es best-effort: su fallo no debe revertir el
			// registro ya persistido (ver HU-18, caso triste).
			LOG.warn("No se pudo notificar la solicitud {}", solicitud.id(), excepcion);
		}
	}

}
