package com.crearcode.leads.dominio;

import java.time.Instant;

/**
 * Puerto de salida para la emisión de tokens de sesión. El dominio sabe
 * que existe un token, no cómo se firma ni valida (JWT en la
 * implementación real, ver ADR-08 en docs/02-arquitectura.md).
 */
public interface GeneradorDeToken {

	SesionAutenticada generar(Usuario usuario, Instant ahora);

}
