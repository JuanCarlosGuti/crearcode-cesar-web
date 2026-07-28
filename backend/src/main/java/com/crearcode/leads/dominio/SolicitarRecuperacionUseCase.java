package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: pedir el enlace de recuperación de contraseña
 * (fase F8, HU-32). Siempre termina igual, exista o no la cuenta
 * (invariante 7).
 */
public interface SolicitarRecuperacionUseCase {

	void solicitar(String correo);

}
