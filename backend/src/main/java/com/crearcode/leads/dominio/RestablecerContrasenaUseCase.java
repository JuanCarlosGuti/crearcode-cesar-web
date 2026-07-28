package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: consumir un token de recuperación y fijar la
 * contraseña nueva (fase F8, HU-32). También marca la cuenta como
 * verificada — quien restablece probó ser dueño del correo.
 */
public interface RestablecerContrasenaUseCase {

	void restablecer(String tokenEnClaro, ContrasenaPlana contrasenaNueva);

}
