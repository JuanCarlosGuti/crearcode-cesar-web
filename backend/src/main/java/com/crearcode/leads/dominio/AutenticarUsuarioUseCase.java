package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: valida credenciales y, si son correctas, emite una
 * sesión autenticada. La implementación (en {@code aplicacion/}) nunca
 * revela si la causa del fallo fue "el correo no existe" o "la
 * contraseña no coincide" — mismo mensaje genérico en ambos casos (ver
 * docs/03-modelo-de-dominio.md, Parte 2, invariante 1).
 */
public interface AutenticarUsuarioUseCase {

	SesionAutenticada autenticar(String correo, String contrasenaEnClaro);

}
