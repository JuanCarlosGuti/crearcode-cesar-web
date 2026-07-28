package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: consumir un token de verificación (fase F8,
 * HU-31). Marca la cuenta verificada y el token como usado.
 */
public interface VerificarCorreoUseCase {

	void verificar(String tokenEnClaro);

}
