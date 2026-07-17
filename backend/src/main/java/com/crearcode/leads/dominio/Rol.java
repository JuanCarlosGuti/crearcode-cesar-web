package com.crearcode.leads.dominio;

/**
 * Un unico valor en v1. Existe como enum (no como {@code boolean
 * esAdmin}) para que agregar un segundo rol el dia que haga falta sea
 * extender el enum y los puntos de autorizacion que lo consultan, no
 * rediseniar el modelo (ver ADR-08 en docs/02-arquitectura.md).
 */
public enum Rol {
	ADMIN
}
