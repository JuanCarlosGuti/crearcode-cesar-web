package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentificadoresDeProyectoTest {

	@Test
	void ningunIdentificadorAceptaNulo() {
		assertThatThrownBy(() -> new ProyectoId(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new EntregableId(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new PagoId(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void cadaIdentificadorNuevoEsDistinto() {
		assertThat(ProyectoId.nuevo()).isNotEqualTo(ProyectoId.nuevo());
		assertThat(EntregableId.nuevo()).isNotEqualTo(EntregableId.nuevo());
		assertThat(PagoId.nuevo()).isNotEqualTo(PagoId.nuevo());
	}

}
