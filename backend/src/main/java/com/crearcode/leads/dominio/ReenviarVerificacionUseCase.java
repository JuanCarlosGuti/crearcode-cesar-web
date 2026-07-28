package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: reenviar el correo de verificación (fase F8,
 * HU-31). Silencioso si el correo no existe o ya está verificado, y
 * con límite por correo (invariante 6) — nunca revela estado de
 * cuentas.
 */
public interface ReenviarVerificacionUseCase {

	void reenviar(String correo);

}
