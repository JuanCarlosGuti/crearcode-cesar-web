package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.EnviadorDeCotizaciones;
import com.crearcode.leads.dominio.EnviarCotizacionUseCase;
import com.crearcode.leads.dominio.GeneradorDeNumeroDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;

/**
 * Envía la cotización (HU-45): le asigna su consecutivo, la congela y
 * avisa al cliente. El correo es **best-effort**: si falla, la
 * cotización queda igualmente enviada y el fundador puede descargar el
 * PDF y compartirlo por su cuenta (mismo criterio que las
 * notificaciones de F2 y los correos de cuenta de F8).
 */
@Service
class EnviarCotizacionUseCaseImpl implements EnviarCotizacionUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(EnviarCotizacionUseCaseImpl.class);

	private final CotizacionRepositorio cotizaciones;
	private final GeneradorDeNumeroDeCotizacion numeros;
	private final EnviadorDeCotizaciones enviador;
	private final Clock reloj;

	EnviarCotizacionUseCaseImpl(CotizacionRepositorio cotizaciones, GeneradorDeNumeroDeCotizacion numeros,
			EnviadorDeCotizaciones enviador, Clock reloj) {
		this.cotizaciones = cotizaciones;
		this.numeros = numeros;
		this.enviador = enviador;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public Cotizacion enviar(CotizacionId id) {
		Cotizacion cotizacion = cotizaciones.buscarPorId(id)
				.orElseThrow(() -> new CotizacionNoEncontradaException(id));

		Instant ahora = Instant.now(reloj);
		NumeroDeCotizacion numero = numeros.siguiente(ahora.atZone(ZoneOffset.UTC).getYear());

		cotizacion.enviar(numero, ahora);
		cotizaciones.guardar(cotizacion);

		try {
			enviador.enviar(cotizacion);
		} catch (RuntimeException fallaDelCorreo) {
			// Sin datos del cliente en el log (política de F2).
			LOG.warn("No se pudo enviar el correo de la cotización {}", numero.valor(), fallaDelCorreo);
		}
		return cotizacion;
	}

}
