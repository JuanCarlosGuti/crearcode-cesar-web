package com.crearcode.leads.infraestructura.rest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La huella sustituye a la IP para contar cupo sin guardar la IP: hay
 * que poder decir "estas dos peticiones vienen de la misma red hoy"
 * sin poder decir de qué red. La sal cambia cada día, así que la huella
 * de ayer no sirve para rastrear la de hoy.
 */
class HuellaDeRedTest {

	private static final Clock LUNES = Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneOffset.UTC);

	@Test
	void laMismaIpElMismoDiaDaLaMismaHuella() {
		HuellaDeRed huellas = new HuellaDeRed(LUNES);

		assertThat(huellas.de("190.24.1.7")).isEqualTo(huellas.de("190.24.1.7"));
	}

	@Test
	void ipsDistintasDanHuellasDistintas() {
		HuellaDeRed huellas = new HuellaDeRed(LUNES);

		assertThat(huellas.de("190.24.1.7")).isNotEqualTo(huellas.de("190.24.1.8"));
	}

	@Test
	void laHuellaNoContieneLaIpEnClaro() {
		HuellaDeRed huellas = new HuellaDeRed(LUNES);

		assertThat(huellas.de("190.24.1.7")).doesNotContain("190.24.1.7").doesNotContain("190.24");
	}

	/**
	 * Sin esto la huella sería un identificador estable del visitante y
	 * no un contador del día: exactamente lo que no queremos guardar.
	 * Se prueba sobre la MISMA instancia, que es como corre en
	 * producción — dos instancias darían huellas distintas por tener
	 * sales distintas, y el test pasaría sin probar nada.
	 */
	@Test
	void alCambiarElDiaCambiaLaHuellaDeLaMismaIp() {
		RelojMovible reloj = new RelojMovible(Instant.parse("2026-09-28T10:00:00Z"));
		HuellaDeRed huellas = new HuellaDeRed(reloj);
		String deHoy = huellas.de("190.24.1.7");

		reloj.avanzarA(Instant.parse("2026-09-29T10:00:00Z"));

		assertThat(huellas.de("190.24.1.7")).isNotEqualTo(deHoy);
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

	@Test
	void sinIpConocidaDevuelveUnaHuellaComunYNoRevienta() {
		HuellaDeRed huellas = new HuellaDeRed(LUNES);

		assertThat(huellas.de(null)).isEqualTo(huellas.de("  "));
	}

}
