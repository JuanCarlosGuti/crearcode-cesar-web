package com.crearcode.leads.dominio;

import java.time.Duration;

/**
 * Propósito de un {@link TokenDeUsuario}, con su vigencia propia
 * (HU-31: verificación 24 h; HU-32: recuperación 1 h).
 */
public enum PropositoDeToken {

	VERIFICACION(Duration.ofHours(24)),
	RECUPERACION(Duration.ofHours(1));

	private final Duration vigencia;

	PropositoDeToken(Duration vigencia) {
		this.vigencia = vigencia;
	}

	public Duration vigencia() {
		return vigencia;
	}

}
