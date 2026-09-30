package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.AplicarRetencionDeDatosUseCase;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudRepositorio;

@Service
class AplicarRetencionDeDatosUseCaseImpl implements AplicarRetencionDeDatosUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(AplicarRetencionDeDatosUseCaseImpl.class);

	private final SolicitudRepositorio solicitudes;
	private final Clock reloj;
	private final int mesesDeRetencion;

	AplicarRetencionDeDatosUseCaseImpl(SolicitudRepositorio solicitudes, Clock reloj,
			@Value("${app.legal.retencion-solicitudes-meses}") int mesesDeRetencion) {
		this.solicitudes = solicitudes;
		this.reloj = reloj;
		this.mesesDeRetencion = mesesDeRetencion;
	}

	/**
	 * Un lead CONVERTIDA es un cliente: sus datos son registro comercial
	 * y contable, con un plazo propio de diez años (política v2, §9).
	 * Borrarlo aquí destruiría justo lo que la ley obliga a conservar,
	 * así que el filtro es por estado y no solo por fecha.
	 */
	@Override
	@Transactional
	public int aplicar() {
		Instant limite = Instant.now(reloj).minus(mesesDeRetencion * 30L, ChronoUnit.DAYS);

		List<SolicitudDeContacto> vencidas = solicitudes.listar().stream()
				.filter(solicitud -> solicitud.estado() != EstadoSolicitud.CONVERTIDA)
				.filter(solicitud -> solicitud.fechaUltimaActualizacion().isBefore(limite))
				.toList();

		vencidas.forEach(solicitud -> solicitudes.eliminar(solicitud.id()));

		if (!vencidas.isEmpty()) {
			// Cuántas, nunca quiénes: este log documenta que la retención
			// se aplicó, y no es sitio para datos personales.
			LOG.info("Retención de datos: {} solicitudes borradas por superar los {} meses", vencidas.size(),
					mesesDeRetencion);
		}
		return vencidas.size();
	}

}
