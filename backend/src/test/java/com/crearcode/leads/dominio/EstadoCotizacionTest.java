package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoCotizacionTest {

	@Test
	void unBorradorSePuedeEnviarOCancelar() {
		assertThat(EstadoCotizacion.BORRADOR.puedeTransicionarA(EstadoCotizacion.ENVIADA)).isTrue();
		assertThat(EstadoCotizacion.BORRADOR.puedeTransicionarA(EstadoCotizacion.CANCELADA)).isTrue();
	}

	@Test
	void unBorradorNoSePuedeAceptarNiRechazarNiVencer() {
		assertThat(EstadoCotizacion.BORRADOR.puedeTransicionarA(EstadoCotizacion.ACEPTADA)).isFalse();
		assertThat(EstadoCotizacion.BORRADOR.puedeTransicionarA(EstadoCotizacion.RECHAZADA)).isFalse();
		assertThat(EstadoCotizacion.BORRADOR.puedeTransicionarA(EstadoCotizacion.VENCIDA)).isFalse();
	}

	@Test
	void unaEnviadaSePuedeResponderVencerOCancelar() {
		assertThat(EstadoCotizacion.ENVIADA.puedeTransicionarA(EstadoCotizacion.ACEPTADA)).isTrue();
		assertThat(EstadoCotizacion.ENVIADA.puedeTransicionarA(EstadoCotizacion.RECHAZADA)).isTrue();
		assertThat(EstadoCotizacion.ENVIADA.puedeTransicionarA(EstadoCotizacion.VENCIDA)).isTrue();
		assertThat(EstadoCotizacion.ENVIADA.puedeTransicionarA(EstadoCotizacion.CANCELADA)).isTrue();
	}

	@Test
	void unaEnviadaNoVuelveASerBorrador() {
		assertThat(EstadoCotizacion.ENVIADA.puedeTransicionarA(EstadoCotizacion.BORRADOR)).isFalse();
	}

	@Test
	void losEstadosFinalesNoTransicionanANingunLado() {
		for (EstadoCotizacion terminal : new EstadoCotizacion[] { EstadoCotizacion.ACEPTADA,
				EstadoCotizacion.RECHAZADA, EstadoCotizacion.VENCIDA, EstadoCotizacion.CANCELADA }) {
			for (EstadoCotizacion destino : EstadoCotizacion.values()) {
				assertThat(terminal.puedeTransicionarA(destino)).isFalse();
			}
		}
	}

	@Test
	void ningunEstadoTransicionaHaciaSiMismo() {
		for (EstadoCotizacion estado : EstadoCotizacion.values()) {
			assertThat(estado.puedeTransicionarA(estado)).isFalse();
		}
	}

}
