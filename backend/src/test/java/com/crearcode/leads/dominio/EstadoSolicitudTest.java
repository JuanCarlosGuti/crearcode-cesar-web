package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static com.crearcode.leads.dominio.EstadoSolicitud.CONTACTADA;
import static com.crearcode.leads.dominio.EstadoSolicitud.CONVERTIDA;
import static com.crearcode.leads.dominio.EstadoSolicitud.DESCARTADA;
import static com.crearcode.leads.dominio.EstadoSolicitud.NUEVA;
import static org.assertj.core.api.Assertions.assertThat;

class EstadoSolicitudTest {

	@Test
	void nuevaPuedeTransicionarAContactada() {
		assertThat(NUEVA.puedeTransicionarA(CONTACTADA)).isTrue();
	}

	@Test
	void nuevaPuedeTransicionarADescartada() {
		assertThat(NUEVA.puedeTransicionarA(DESCARTADA)).isTrue();
	}

	@Test
	void contactadaPuedeTransicionarAConvertida() {
		assertThat(CONTACTADA.puedeTransicionarA(CONVERTIDA)).isTrue();
	}

	@Test
	void contactadaPuedeTransicionarADescartada() {
		assertThat(CONTACTADA.puedeTransicionarA(DESCARTADA)).isTrue();
	}

	@Test
	void nuevaNoPuedeSaltarDirectoAConvertida() {
		assertThat(NUEVA.puedeTransicionarA(CONVERTIDA)).isFalse();
	}

	@Test
	void contactadaNoPuedeVolverANueva() {
		assertThat(CONTACTADA.puedeTransicionarA(NUEVA)).isFalse();
	}

	@Test
	void ningunEstadoPuedeTransicionarASiMismo() {
		for (EstadoSolicitud estado : EstadoSolicitud.values()) {
			assertThat(estado.puedeTransicionarA(estado))
					.as("%s no debería poder transicionar a sí mismo", estado)
					.isFalse();
		}
	}

	@Test
	void convertidaEsTerminal() {
		for (EstadoSolicitud destino : EstadoSolicitud.values()) {
			assertThat(CONVERTIDA.puedeTransicionarA(destino)).isFalse();
		}
	}

	@Test
	void descartadaEsTerminal() {
		for (EstadoSolicitud destino : EstadoSolicitud.values()) {
			assertThat(DESCARTADA.puedeTransicionarA(destino)).isFalse();
		}
	}

}
