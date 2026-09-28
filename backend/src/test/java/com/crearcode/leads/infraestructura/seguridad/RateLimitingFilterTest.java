package com.crearcode.leads.infraestructura.seguridad;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El contador vive en memoria y guarda una entrada por IP y por regla.
 * Con once reglas y sin purga, el proceso acumulaba una entrada por
 * cada IP que hubiera tocado el sitio desde el último despliegue y no
 * la soltaba nunca (auditoría del 28 sep 2026, P0-2e). No se nota en un
 * sitio de pyme hasta que un escáner recorre el rango de un proveedor.
 */
class RateLimitingFilterTest {

	private final RelojMovible reloj = new RelojMovible(Instant.parse("2026-09-28T10:00:00Z"));

	private RateLimitingFilter filtro() {
		// Ventanas de 10 minutos en todas las reglas; solo importa la de
		// /api/solicitudes, que es la que ejercita la prueba.
		return new RateLimitingFilter(reloj, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10);
	}

	private void pedir(RateLimitingFilter filtro, String ip) throws Exception {
		MockHttpServletRequest peticion = new MockHttpServletRequest("POST", "/api/solicitudes");
		peticion.setRemoteAddr(ip);
		filtro.doFilter(peticion, new MockHttpServletResponse(), new MockFilterChain());
	}

	@Test
	void lasVentanasVencidasSeSueltanEnVezDeAcumularseParaSiempre() throws Exception {
		RateLimitingFilter filtro = filtro();
		for (int i = 0; i < 1500; i++) {
			pedir(filtro, "10.0." + (i / 256) + "." + (i % 256));
		}
		int enLaVentana = filtro.ventanasEnMemoria();

		// Pasa la ventana entera: esas 1500 IPs ya no significan nada.
		reloj.avanzarA(Instant.parse("2026-09-28T10:30:00Z"));
		pedir(filtro, "190.24.0.1");

		assertThat(enLaVentana).isGreaterThan(1000);
		assertThat(filtro.ventanasEnMemoria()).isLessThan(50);
	}

	@Test
	void unaIpActivaNoPierdeSuVentanaCuandoSePurgaElResto() throws Exception {
		RateLimitingFilter filtro = filtro();
		for (int i = 0; i < 1200; i++) {
			pedir(filtro, "10.0." + (i / 256) + "." + (i % 256));
		}

		// El atacante sigue pidiendo mientras se purgan los vencidos.
		for (int i = 0; i < 5; i++) {
			pedir(filtro, "190.24.0.1");
		}
		MockHttpServletRequest sexta = new MockHttpServletRequest("POST", "/api/solicitudes");
		sexta.setRemoteAddr("190.24.0.1");
		MockHttpServletResponse respuesta = new MockHttpServletResponse();
		filtro.doFilter(sexta, respuesta, new MockFilterChain());

		assertThat(respuesta.getStatus()).isEqualTo(429);
	}

	private static final class RelojMovible extends Clock {

		private Instant ahora;

		private RelojMovible(Instant ahora) {
			this.ahora = ahora;
		}

		private void avanzarA(Instant nuevo) {
			this.ahora = nuevo;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zona) {
			return this;
		}

		@Override
		public Instant instant() {
			return ahora;
		}

	}

}
