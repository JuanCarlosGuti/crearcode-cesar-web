package com.crearcode.leads.dominio;

import java.time.Instant;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de los tokens de correo (fase F8).
 */
public interface TokenDeUsuarioRepositorio {

	void guardar(TokenDeUsuario token);

	Optional<TokenDeUsuario> buscarPorValorHash(String valorHash);

	/**
	 * Marca como usados los tokens aún vigentes del usuario para ese
	 * propósito — al reenviar un correo, los enlaces anteriores dejan de
	 * servir (HU-31).
	 */
	void invalidarActivos(UsuarioId usuarioId, PropositoDeToken proposito, Instant ahora);

	/**
	 * Cuántos tokens se han generado para el usuario/propósito desde el
	 * instante dado — soporta el límite de envíos por correo (invariante
	 * 6 del contexto: máximo 3 cada 15 minutos).
	 */
	long contarRecientes(UsuarioId usuarioId, PropositoDeToken proposito, Instant desde);

}
