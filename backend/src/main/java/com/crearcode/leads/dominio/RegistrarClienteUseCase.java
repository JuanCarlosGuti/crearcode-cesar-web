package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: registro público de clientes (fase F8, HU-30).
 * Crea la cuenta sin verificar, genera el token de verificación y
 * dispara el correo (best-effort).
 */
public interface RegistrarClienteUseCase {

	UsuarioId registrar(Correo correo, ContrasenaPlana contrasena);

}
