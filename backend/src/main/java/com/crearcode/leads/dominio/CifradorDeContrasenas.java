package com.crearcode.leads.dominio;

/**
 * Puerto de salida para cifrado/verificación de contraseñas. Existe
 * como puerto porque el algoritmo real (BCrypt) es una dependencia de
 * Spring Security que {@code dominio/} no puede tocar (ver regla de
 * dependencias en docs/02-arquitectura.md).
 */
public interface CifradorDeContrasenas {

	String hash(String contrasenaEnClaro);

	boolean verificar(String contrasenaEnClaro, String hash);

}
