package com.crearcode.leads.dominio;

import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia del contexto {@code usuarios}.
 * Implementado en {@code infraestructura/persistencia} con JPA.
 */
public interface UsuarioRepositorio {

	void guardar(Usuario usuario);

	/**
	 * Búsqueda case-insensitive: el adaptador normaliza mayúsculas/
	 * minúsculas, no el dominio (ver docs/03-modelo-de-dominio.md, Parte
	 * 2, §3).
	 */
	Optional<Usuario> buscarPorCorreo(Correo correo);

	/**
	 * Desde F8: los tokens de correo referencian al usuario por id, y
	 * verificar/restablecer necesitan cargarlo.
	 */
	Optional<Usuario> buscarPorId(UsuarioId id);

	/**
	 * Borra la cuenta. Derecho de supresión de la Ley 1581, que la
	 * política v2 promete: no basta con que exista el derecho si
	 * ejercerlo depende de que alguien atienda un correo a mano.
	 */
	void eliminar(UsuarioId id);

}
