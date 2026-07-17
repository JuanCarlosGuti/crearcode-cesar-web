package com.crearcode.leads.infraestructura.seguridad;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Claves de firma/verificación de los JWT del panel admin: este backend
 * es a la vez emisor y único validador de sus propios tokens (HS256,
 * sin Authorization Server externo, ver ADR-08 en
 * docs/02-arquitectura.md). El secreto debe tener al menos 32 bytes —
 * lo exige la firma HMAC — o falla en runtime al primer login, no al
 * arrancar.
 */
@Configuration
class JwtConfig {

	@Bean
	SecretKey claveDeFirmaJwt(@Value("${app.jwt.secreto}") String secreto) {
		return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey claveDeFirmaJwt) {
		return NimbusJwtEncoder.withSecretKey(claveDeFirmaJwt).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey claveDeFirmaJwt) {
		return NimbusJwtDecoder.withSecretKey(claveDeFirmaJwt).macAlgorithm(MacAlgorithm.HS256).build();
	}

}
