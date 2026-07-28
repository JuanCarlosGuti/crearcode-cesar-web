package com.crearcode.leads.dominio;

/**
 * Rol unico por usuario (decision registrada al arrancar F8, ver
 * docs/03-modelo-de-dominio.md Parte 2 §2): {@code ADMIN} gestiona el
 * panel; {@code CLIENTE} es una persona registrada desde el sitio
 * publico (fase F8, HU-30). La migracion a multiples roles por usuario
 * se evalua en F11, cuando existan los roles internos.
 */
public enum Rol {
	ADMIN,
	CLIENTE
}
