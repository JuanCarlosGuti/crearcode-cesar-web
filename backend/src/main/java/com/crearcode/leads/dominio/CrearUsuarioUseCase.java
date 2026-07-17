package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: crea un nuevo usuario. En v1 solo lo usa el
 * <em>seed</em> del único admin al arrancar la aplicación (ver ADR-08
 * en docs/02-arquitectura.md); no hay pantalla de alta de usuarios
 * todavía.
 */
public interface CrearUsuarioUseCase {

	UsuarioId crear(Correo correo, String contrasenaEnClaro, Rol rol);

}
