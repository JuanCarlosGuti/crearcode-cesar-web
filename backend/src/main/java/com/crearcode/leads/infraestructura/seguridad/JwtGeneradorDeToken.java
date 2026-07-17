package com.crearcode.leads.infraestructura.seguridad;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.SesionAutenticada;
import com.crearcode.leads.dominio.Usuario;

@Component
class JwtGeneradorDeToken implements GeneradorDeToken {

	private final JwtEncoder jwtEncoder;
	private final long expiracionMinutos;

	JwtGeneradorDeToken(JwtEncoder jwtEncoder, @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
		this.jwtEncoder = jwtEncoder;
		this.expiracionMinutos = expiracionMinutos;
	}

	@Override
	public SesionAutenticada generar(Usuario usuario, Instant ahora) {
		Instant expiraEn = ahora.plus(Duration.ofMinutes(expiracionMinutos));

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuedAt(ahora)
				.expiresAt(expiraEn)
				.subject(usuario.correo().valor())
				.id(UUID.randomUUID().toString())
				.claim("rol", usuario.rol().name())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		return new SesionAutenticada(token, expiraEn);
	}

}
