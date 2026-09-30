package com.crearcode.leads.infraestructura.rest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

/**
 * Identificador efímero de la red desde la que llega una petición, para
 * contar cupo de IA sin guardar la IP.
 *
 * <p>
 * Hasta la auditoría del 28 sep 2026 el cupo anónimo se contaba contra
 * el header {@code X-Sesion-Anonima}, que lo genera el propio
 * navegador: borrar {@code sessionStorage} devolvía el cupo entero,
 * cuantas veces se quisiera. La IP no se puede falsear igual de barato
 * — con un solo salto de proxy y
 * {@code server.forward-headers-strategy=native}, la que ve Tomcat es
 * la real (ISS-136).
 *
 * <p>
 * Pero la IP es un dato personal, así que no se guarda: se guarda
 * {@code SHA-256(sal del día + IP)} truncado. La sal es aleatoria y se
 * renueva cada día, de modo que la huella sirve para decir «estas dos
 * peticiones vienen de la misma red hoy» y para nada más: al día
 * siguiente no se puede reconstruir ni cruzar, y de la huella no se
 * vuelve a la IP ni con un diccionario de las 4 mil millones posibles,
 * porque la sal no está almacenada en ninguna parte (vive en memoria y
 * se pierde al reiniciar).
 */
@Component
class HuellaDeRed {

	private static final String SIN_RED_CONOCIDA = "red-desconocida";
	private static final int CARACTERES_DE_HUELLA = 24;

	private final Clock reloj;
	private final SecureRandom aleatorio = new SecureRandom();

	private volatile LocalDate dia = LocalDate.MIN;
	private volatile String sal = "";

	HuellaDeRed(Clock reloj) {
		this.reloj = reloj;
	}

	String de(String ip) {
		if (ip == null || ip.isBlank()) {
			return SIN_RED_CONOCIDA;
		}
		return resumir(salDeHoy() + '|' + ip.trim());
	}

	private synchronized String salDeHoy() {
		LocalDate hoy = LocalDate.now(reloj);
		if (!hoy.equals(dia)) {
			byte[] bytes = new byte[32];
			aleatorio.nextBytes(bytes);
			sal = Base64.getEncoder().encodeToString(bytes);
			dia = hoy;
		}
		return sal;
	}

	private static String resumir(String texto) {
		try {
			byte[] resumen = MessageDigest.getInstance("SHA-256")
					.digest(texto.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(resumen).substring(0, CARACTERES_DE_HUELLA);
		} catch (NoSuchAlgorithmException imposible) {
			throw new IllegalStateException("SHA-256 siempre está disponible en la JVM", imposible);
		}
	}

}
