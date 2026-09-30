package com.crearcode.leads.dominio;

/**
 * Quién aprobó un entregable (decisión 27 de docs/10): el cliente desde
 * su cuenta, o el equipo desde el panel cuando el cliente respondió por
 * WhatsApp. Es la constancia de que ese entregable ya se puede cobrar.
 */
public enum QuienResponde {
	CLIENTE,
	EQUIPO
}
