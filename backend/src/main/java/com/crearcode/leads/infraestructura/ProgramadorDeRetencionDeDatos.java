package com.crearcode.leads.infraestructura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.crearcode.leads.dominio.AplicarRetencionDeDatosUseCase;

/**
 * Dispara la retención de datos una vez al día.
 *
 * <p>
 * La política v2 (§9) promete que las solicitudes que no se convierten
 * en clientes se borran a los 24 meses. Prometerlo y no hacerlo es peor
 * que no prometerlo: el dato sigue ahí y además hay un documento
 * público diciendo que no.
 *
 * <p>
 * Corre en la propia aplicación y no como un cron del servidor porque
 * la regla —qué se borra y qué no— es de dominio, no de operación: un
 * {@code DELETE} escrito en un crontab no sabe que un lead convertido
 * es un cliente cuyos datos hay que conservar diez años.
 *
 * <p>
 * Con una sola instancia esto es suficiente. El día que haya dos, dos
 * procesos correrían la misma limpieza a la misma hora: como el borrado
 * es idempotente no rompe nada, pero habría que coordinarlos.
 */
@Configuration
@EnableScheduling
class ProgramadorDeRetencionDeDatos {

	private static final Logger LOG = LoggerFactory.getLogger(ProgramadorDeRetencionDeDatos.class);

	private final AplicarRetencionDeDatosUseCase retencion;

	ProgramadorDeRetencionDeDatos(AplicarRetencionDeDatosUseCase retencion) {
		this.retencion = retencion;
	}

	@Scheduled(cron = "${app.legal.retencion-cron}", zone = "America/Bogota")
	void limpiarLoQueYaCumplioSuPlazo() {
		try {
			retencion.aplicar();
		} catch (RuntimeException fallo) {
			// Una tarea programada que revienta se muere en silencio y no
			// vuelve a avisar: el WARN es lo único que quedaría.
			LOG.warn("No se pudo aplicar la retención de datos; se reintentará mañana", fallo);
		}
	}

}
