package com.crearcode.leads.dominio;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsentimientoDatosTest {

	@Test
	void rechazaFechaAceptacionNula() {
		assertThatThrownBy(() -> new ConsentimientoDatos(true, null, "v1"))
				.isInstanceOf(ConsentimientoRequeridoException.class);
	}

	@Test
	void rechazaVersionPoliticaNulaOVacia() {
		assertThatThrownBy(() -> new ConsentimientoDatos(true, Instant.now(), " "))
				.isInstanceOf(ConsentimientoRequeridoException.class);
	}

	@Test
	void creaConAceptadoTrue() {
		Instant ahora = Instant.now();

		ConsentimientoDatos consentimiento = new ConsentimientoDatos(true, ahora, "v1");

		assertThat(consentimiento.aceptado()).isTrue();
		assertThat(consentimiento.fechaAceptacion()).isEqualTo(ahora);
		assertThat(consentimiento.versionPoliticaAceptada()).isEqualTo("v1");
	}

	@Test
	void permiteRepresentarConsentimientoNoAceptado() {
		ConsentimientoDatos consentimiento = new ConsentimientoDatos(false, Instant.now(), "v1");

		assertThat(consentimiento.aceptado()).isFalse();
	}


	/**
	 * La finalidad comercial es una autorizacion distinta de la de
	 * atender la solicitud, y la politica v2 (seccion 13) promete que
	 * tiene su propia casilla. Modelarla aparte es lo que permite
	 * retirarla "sin que afecte lo demas": un solo booleano para las dos
	 * haria imposible distinguir quien acepto que.
	 */
	@Test
	void laFinalidadComercialEsUnaAutorizacionSeparadaDeLaObligatoria() {
		ConsentimientoDatos soloLoObligatorio = new ConsentimientoDatos(true, Instant.now(), "v2", false);

		assertThat(soloLoObligatorio.aceptado()).isTrue();
		assertThat(soloLoObligatorio.comunicacionesComerciales()).isFalse();
	}

	/**
	 * Sin esto se podria guardar "quiero novedades comerciales" de
	 * alguien que no autorizo ni el tratamiento basico.
	 */
	@Test
	void noSePuedeAutorizarLoComercialSinAutorizarElTratamiento() {
		assertThatThrownBy(() -> new ConsentimientoDatos(false, Instant.now(), "v2", true))
				.isInstanceOf(ConsentimientoRequeridoException.class);
	}

}
