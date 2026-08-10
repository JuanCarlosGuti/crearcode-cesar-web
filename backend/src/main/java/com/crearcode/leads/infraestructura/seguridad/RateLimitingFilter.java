package com.crearcode.leads.infraestructura.seguridad;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
 * Limita cuántas veces la misma IP puede llamar a un conjunto de rutas
 * sensibles en una ventana de tiempo (HU-16 para
 * {@code POST /api/solicitudes}; fuerza bruta de login para
 * {@code POST /api/auth/login}, con su propio umbral más estricto).
 * Contador en memoria: suficiente para el volumen esperado de un sitio
 * de pyme en v1, sin necesitar infraestructura distribuida. Cada regla
 * lleva su propio umbral configurable (ver application.properties) para
 * poder ajustarlo sin recompilar y para que las pruebas de este propio
 * mecanismo no dependan del valor de producción.
 */
@Component
class RateLimitingFilter extends OncePerRequestFilter {

	private final Clock reloj;
	private final List<Regla> reglas;

	RateLimitingFilter(Clock reloj,
			@Value("${app.rate-limit.max-solicitudes}") int maxSolicitudesContacto,
			@Value("${app.rate-limit.ventana-minutos}") long ventanaContactoMinutos,
			@Value("${app.rate-limit.login.max-intentos}") int maxIntentosLogin,
			@Value("${app.rate-limit.login.ventana-minutos}") long ventanaLoginMinutos,
			@Value("${app.rate-limit.registro.max-intentos}") int maxRegistros,
			@Value("${app.rate-limit.registro.ventana-minutos}") long ventanaRegistroMinutos,
			@Value("${app.rate-limit.verificacion.max-intentos}") int maxVerificaciones,
			@Value("${app.rate-limit.verificacion.ventana-minutos}") long ventanaVerificacionMinutos,
			@Value("${app.rate-limit.reenvio-verificacion.max-intentos}") int maxReenvios,
			@Value("${app.rate-limit.reenvio-verificacion.ventana-minutos}") long ventanaReenvioMinutos,
			@Value("${app.rate-limit.recuperacion.max-intentos}") int maxRecuperaciones,
			@Value("${app.rate-limit.recuperacion.ventana-minutos}") long ventanaRecuperacionMinutos,
			@Value("${app.rate-limit.restablecimiento.max-intentos}") int maxRestablecimientos,
			@Value("${app.rate-limit.restablecimiento.ventana-minutos}") long ventanaRestablecimientoMinutos,
			@Value("${app.rate-limit.asistente.max-intentos}") int maxMensajesAsistente,
			@Value("${app.rate-limit.asistente.ventana-minutos}") long ventanaAsistenteMinutos) {
		this.reloj = reloj;
		this.reglas = List.of(
				new Regla("POST", "/api/solicitudes", maxSolicitudesContacto, Duration.ofMinutes(ventanaContactoMinutos)),
				new Regla("POST", "/api/auth/login", maxIntentosLogin, Duration.ofMinutes(ventanaLoginMinutos)),
				new Regla("POST", "/api/auth/registro", maxRegistros, Duration.ofMinutes(ventanaRegistroMinutos)),
				new Regla("POST", "/api/auth/verificacion", maxVerificaciones,
						Duration.ofMinutes(ventanaVerificacionMinutos)),
				new Regla("POST", "/api/auth/reenvio-verificacion", maxReenvios,
						Duration.ofMinutes(ventanaReenvioMinutos)),
				new Regla("POST", "/api/auth/recuperacion", maxRecuperaciones,
						Duration.ofMinutes(ventanaRecuperacionMinutos)),
				new Regla("POST", "/api/auth/restablecimiento", maxRestablecimientos,
						Duration.ofMinutes(ventanaRestablecimientoMinutos)),
				new Regla("POST", "/api/asistente/demo-diseno", maxMensajesAsistente,
						Duration.ofMinutes(ventanaAsistenteMinutos)),
				new Regla("POST", "/api/asistente/diagnostico", maxMensajesAsistente,
						Duration.ofMinutes(ventanaAsistenteMinutos)),
				new Regla("POST", "/api/asistente/simulador", maxMensajesAsistente,
						Duration.ofMinutes(ventanaAsistenteMinutos)),
				new Regla("POST", "/api/asistente/mensajes", maxMensajesAsistente,
						Duration.ofMinutes(ventanaAsistenteMinutos)));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		for (Regla regla : reglas) {
			if (regla.aplicaA(request) && regla.superoElLimite(request.getRemoteAddr(), reloj)) {
				response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private static final class Regla {

		private final String metodo;
		private final String ruta;
		private final int maxSolicitudesPorVentana;
		private final Duration duracionVentana;
		private final Map<String, VentanaDeSolicitudes> ventanasPorIp = new ConcurrentHashMap<>();

		private Regla(String metodo, String ruta, int maxSolicitudesPorVentana, Duration duracionVentana) {
			this.metodo = metodo;
			this.ruta = ruta;
			this.maxSolicitudesPorVentana = maxSolicitudesPorVentana;
			this.duracionVentana = duracionVentana;
		}

		private boolean aplicaA(HttpServletRequest request) {
			return metodo.equalsIgnoreCase(request.getMethod()) && ruta.equals(request.getRequestURI());
		}

		private boolean superoElLimite(String ip, Clock reloj) {
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
	}

	private static final class VentanaDeSolicitudes {
		private Instant inicio;
		private int contador;

		private VentanaDeSolicitudes(Instant inicio) {
			this.inicio = inicio;
		}
	}

}
