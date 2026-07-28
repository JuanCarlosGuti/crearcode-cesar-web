package com.crearcode.leads.dominio;

/**
 * Puerto de salida: correos transaccionales de cuentas (fase F8). El
 * adaptador de infraestructura arma el enlace completo (URL del
 * frontend + ruta + token) — la aplicación no conoce rutas del
 * frontend. Copys en docs/08-contenido.md §Cuentas de cliente.
 */
public interface EnviadorDeCorreosDeCuenta {

	void enviarVerificacion(Correo destino, String tokenEnClaro);

	void enviarRecuperacion(Correo destino, String tokenEnClaro);

}
