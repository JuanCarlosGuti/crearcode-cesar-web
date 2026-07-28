package com.crearcode.leads.dominio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * Token de un solo uso enviado por correo (verificación de cuenta o
 * recuperación de contraseña, fase F8). El valor en claro solo viaja en
 * el enlace del correo: aquí se persiste únicamente su hash SHA-256,
 * para que una fuga de la base de datos no permita tomar cuentas
 * (invariante 5 del contexto usuarios). Solo usa {@code java.base}
 * (SecureRandom/MessageDigest) — el dominio sigue sin Spring ni JPA.
 */
public record TokenDeUsuario(
		UUID id,
		UsuarioId usuarioId,
		String valorHash,
		PropositoDeToken proposito,
		Instant creadoEn,
		Instant expiraEn,
		Instant usadoEn) {

	private static final SecureRandom ALEATORIO = new SecureRandom();
	private static final int BYTES_DE_ENTROPIA = 32;

	public TokenDeUsuario {
		Objects.requireNonNull(id, "El id del token no puede ser nulo");
		Objects.requireNonNull(usuarioId, "El usuario del token no puede ser nulo");
		if (valorHash == null || valorHash.isBlank()) {
			throw new IllegalArgumentException("El hash del token no puede estar vacío");
		}
		Objects.requireNonNull(proposito, "El propósito del token no puede ser nulo");
		Objects.requireNonNull(creadoEn, "La fecha de creación no puede ser nula");
		Objects.requireNonNull(expiraEn, "La fecha de expiración no puede ser nula");
	}

	/** Resultado de {@link #generar}: la entidad y el valor que va en el correo. */
	public record TokenGenerado(TokenDeUsuario token, String valorEnClaro) {
	}

	public static TokenGenerado generar(UsuarioId usuarioId, PropositoDeToken proposito, Instant ahora) {
		byte[] bytes = new byte[BYTES_DE_ENTROPIA];
		ALEATORIO.nextBytes(bytes);
		String valorEnClaro = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		TokenDeUsuario token = new TokenDeUsuario(
				UUID.randomUUID(),
				usuarioId,
				hash(valorEnClaro),
				proposito,
				ahora,
				ahora.plus(proposito.vigencia()),
				null);
		return new TokenGenerado(token, valorEnClaro);
	}

	/** SHA-256 en hexadecimal — también lo usan los casos de uso para buscar. */
	public static String hash(String valorEnClaro) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(valorEnClaro.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException imposible) {
			throw new IllegalStateException("SHA-256 no disponible en esta JVM", imposible);
		}
	}

	public boolean esVigente(Instant ahora) {
		return usadoEn == null && ahora.isBefore(expiraEn);
	}

	public boolean esPara(PropositoDeToken propositoEsperado) {
		return proposito == propositoEsperado;
	}

	/** Consume el token (un solo uso). Lanza si ya no es vigente. */
	public TokenDeUsuario usar(Instant ahora) {
		if (!esVigente(ahora)) {
			throw new TokenDeCuentaInvalidoException();
		}
		return new TokenDeUsuario(id, usuarioId, valorHash, proposito, creadoEn, expiraEn, ahora);
	}

}
