package com.crearcode.leads.infraestructura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.CerrarGarantiasVencidasUseCase;

/**
 * Cierra una vez al día los proyectos cuya garantía de 60 días venció
 * (decisión 23). La programación la habilita
 * {@code ProgramadorDeRetencionDeDatos} con {@code @EnableScheduling}.
 *
 * <p>
 * Corre en la aplicación y no como cron del servidor por la misma razón
 * que la retención: la regla de cuándo vence una garantía es de dominio.
 */
@Component
class ProgramadorDeGarantias {

	private static final Logger LOG = LoggerFactory.getLogger(ProgramadorDeGarantias.class);

	private final CerrarGarantiasVencidasUseCase garantias;

	ProgramadorDeGarantias(CerrarGarantiasVencidasUseCase garantias) {
		this.garantias = garantias;
	}

	@Scheduled(cron = "${app.proyectos.garantias-cron}", zone = "America/Bogota")
	void cerrarLasVencidas() {
		try {
			garantias.cerrar();
		} catch (RuntimeException fallo) {
			// Una tarea programada que revienta se muere en silencio: el
			// WARN es lo único que quedaría. Mañana se reintenta sola.
			LOG.warn("No se pudieron cerrar las garantías vencidas; se reintentará mañana", fallo);
		}
	}

}
