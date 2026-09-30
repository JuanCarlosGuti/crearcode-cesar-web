package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.RegistrarSolicitudUseCase;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.SolicitudRepositorio;

/**
 * Registra el lead del formulario de contacto (HU-18). El aviso por
 * correo NO se manda aqui: se publica un evento que se despacha
 * despues del commit y fuera de esta peticion (ver
 * {@link SolicitudRegistrada}), para que un SMTP lento no tenga al
 * visitante mirando «Enviando…» por un correo que no es suyo.
 */
@Service
class RegistrarSolicitudUseCaseImpl implements RegistrarSolicitudUseCase {

	private final SolicitudRepositorio repositorio;
	private final ApplicationEventPublisher eventos;
	private final Clock reloj;

	RegistrarSolicitudUseCaseImpl(SolicitudRepositorio repositorio, ApplicationEventPublisher eventos, Clock reloj) {
		this.repositorio = repositorio;
		this.eventos = eventos;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public SolicitudId registrar(DatosDeContacto datosDeContacto, ServicioDeInteres servicioDeInteres,
			String mensaje, ConsentimientoDatos consentimiento) {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				datosDeContacto, servicioDeInteres, mensaje, consentimiento, Instant.now(reloj));

		repositorio.guardar(solicitud);
		eventos.publishEvent(new SolicitudRegistrada(solicitud));

		return solicitud.id();
	}

}
