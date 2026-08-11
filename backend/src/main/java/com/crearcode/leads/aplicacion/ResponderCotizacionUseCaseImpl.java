package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ResponderCotizacionUseCase;
import com.crearcode.leads.dominio.SolicitudRepositorio;
import com.crearcode.leads.dominio.TransicionDeEstadoInvalidaException;

/**
 * El cliente responde su cotización desde la cuenta (HU-46). Aceptar
 * arrastra el lead de origen a CONVERTIDA, pero **sin romperse si esa
 * transición ya no aplica** (invariante 7): el pipeline comercial no
 * puede quedar bloqueado por el estado de un lead viejo.
 */
@Service
class ResponderCotizacionUseCaseImpl implements ResponderCotizacionUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(ResponderCotizacionUseCaseImpl.class);

	private final CotizacionRepositorio cotizaciones;
	private final SolicitudRepositorio solicitudes;
	private final Clock reloj;

	ResponderCotizacionUseCaseImpl(CotizacionRepositorio cotizaciones, SolicitudRepositorio solicitudes,
			Clock reloj) {
		this.cotizaciones = cotizaciones;
		this.solicitudes = solicitudes;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public Cotizacion aceptar(CotizacionId id, Correo correoDelCliente) {
		Cotizacion cotizacion = responder(id, correoDelCliente, Cotizacion::aceptar);
		convertirElLead(cotizacion);
		return cotizacion;
	}

	@Override
	@Transactional
	public Cotizacion rechazar(CotizacionId id, Correo correoDelCliente) {
		return responder(id, correoDelCliente, Cotizacion::rechazar);
	}

	private Cotizacion responder(CotizacionId id, Correo correoDelCliente,
			BiConsumer<Cotizacion, Instant> respuesta) {
		Cotizacion cotizacion = cotizaciones.buscarPorId(id)
				.filter(c -> ConsultarCotizacionesUseCaseImpl.esDe(c, correoDelCliente))
				.orElseThrow(() -> new CotizacionNoEncontradaException(id));

		respuesta.accept(cotizacion, Instant.now(reloj));

		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	private void convertirElLead(Cotizacion cotizacion) {
		if (cotizacion.origen() == null) {
			return;
		}
		solicitudes.buscarPorId(cotizacion.origen()).ifPresent(lead -> {
			try {
				lead.cambiarEstado(EstadoSolicitud.CONVERTIDA, Instant.now(reloj));
				solicitudes.guardar(lead);
			} catch (TransicionDeEstadoInvalidaException transicionQueYaNoAplica) {
				LOG.info("La cotización se aceptó pero su lead ya estaba en {}", lead.estado());
			}
		});
	}

}
