package com.crearcode.leads.dominio;

/**
 * Quién registró un pago. En F12 solo existe {@code MANUAL} (decisión 24
 * de docs/10); {@code PASARELA} está para que el día que llegue Wompi lo
 * único nuevo sea quién registra el pago, no el modelo.
 */
public enum OrigenDePago {
	MANUAL,
	PASARELA
}
