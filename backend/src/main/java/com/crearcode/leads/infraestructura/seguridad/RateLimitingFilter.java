package com.crearcode.leads.infraestructura.seguridad;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Limita cuántas veces la misma IP puede llamar a
 * {@code POST /api/solicitudes} en una ventana de tiempo, para frenar
 * envíos masivos automatizados (HU-16). Contador en memoria: suficiente
 * para el volumen esperado de un sitio de pyme en v1, sin necesitar
 * infraestructura distribuida. Umbral configurable (ver
 * application.properties) para poder ajustarlo sin recompilar y para
 * que las pruebas de este propio mecanismo no dependan del valor de
 * producción.
 */
@Component
class RateLimitingFilter extends OncePerRequestFilter {

	private final Clock reloj;
	private final int maxSolicitudesPorVentana;
	private final Duration duracionVentana;
	private final Map<String, VentanaDeSolicitudes> ventanasPorIp = new ConcurrentHashMap<>();

	RateLimitingFilter(Clock reloj,
			@Value("${app.rate-limit.max-solicitudes}") int maxSolicitudesPorVentana,
			@Value("${app.rate-limit.ventana-minutos}") long ventanaMinutos) {
		this.reloj = reloj;
		this.maxSolicitudesPorVentana = maxSolicitudesPorVentana;
		this.duracionVentana = Duration.ofMinutes(ventanaMinutos);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (esRutaLimitada(request) && superoElLimite(request.getRemoteAddr())) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean esRutaLimitada(HttpServletRequest request) {
		return "POST".equalsIgnoreCase(request.getMethod()) && "/api/solicitudes".equals(request.getRequestURI());
	}

	private boolean superoElLimite(String ip) {
		Instant ahora = Instant.now(reloj);
		VentanaDeSolicitudes ventana = ventanasPorIp.computeIfAbsent(ip, clave -> new VentanaDeSolicitudes(ahora));

		synchronized (ventana) {
			if (Duration.between(ventana.inicio, ahora).compareTo(duracionVentana) > 0) {
				ventana.inicio = ahora;
				ventana.contador = 0;
			}
			ventana.contador++;
			return ventana.contador > maxSolicitudesPorVentana;
		}
	}

	private static final class VentanaDeSolicitudes {
		private Instant inicio;
		private int contador;

		private VentanaDeSolicitudes(Instant inicio) {
			this.inicio = inicio;
		}
	}

}
