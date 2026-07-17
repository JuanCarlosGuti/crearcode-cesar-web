package com.crearcode.leads.infraestructura.seguridad;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.SesionAutenticada;
import com.crearcode.leads.dominio.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

class JwtGeneradorDeTokenTest {

	private static final SecretKey CLAVE = new SecretKeySpec(
			"clave-de-prueba-para-firmar-jwt-1234567890".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	private static final Usuario USUARIO =
			Usuario.crear(new Correo("admin@crearcode-cesar.local"), "hash", Rol.ADMIN);

	private final JwtEncoder jwtEncoder = NimbusJwtEncoder.withSecretKey(CLAVE).algorithm(MacAlgorithm.HS256).build();
	private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(CLAVE).macAlgorithm(MacAlgorithm.HS256).build();
	private final JwtGeneradorDeToken generador = new JwtGeneradorDeToken(jwtEncoder, 480);

	@Test
	void generaUnTokenNoVacioConLaExpiracionConfigurada() {
		Instant ahora = Instant.parse("2026-07-16T10:00:00Z");

		SesionAutenticada sesion = generador.generar(USUARIO, ahora);

		assertThat(sesion.token()).isNotBlank();
		assertThat(sesion.expiraEn()).isEqualTo(Instant.parse("2026-07-16T18:00:00Z"));
	}

	@Test
	void elTokenGeneradoEsVerificableConLaMismaClaveYLlevaElRol() {
		SesionAutenticada sesion = generador.generar(USUARIO, Instant.now());

		Jwt jwt = jwtDecoder.decode(sesion.token());

		assertThat(jwt.getSubject()).isEqualTo("admin@crearcode-cesar.local");
		assertThat(jwt.getClaimAsString("rol")).isEqualTo("ADMIN");
		assertThat(jwt.getId()).isNotBlank();
	}

	@Test
	void cadaTokenGeneradoTieneUnJtiDistinto() {
		String jtiUno = jwtDecoder.decode(generador.generar(USUARIO, Instant.now()).token()).getId();
		String jtiDos = jwtDecoder.decode(generador.generar(USUARIO, Instant.now()).token()).getId();

		assertThat(jtiUno).isNotEqualTo(jtiDos);
	}

}
