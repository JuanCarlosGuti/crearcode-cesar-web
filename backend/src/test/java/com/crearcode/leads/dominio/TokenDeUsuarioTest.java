package com.crearcode.leads.dominio;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenDeUsuarioTest {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");
	private static final UsuarioId USUARIO = UsuarioId.nuevo();

	@Test
	void generarProduceUnValorEnClaroYPersisteSoloSuHash() {
		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(USUARIO, PropositoDeToken.VERIFICACION, AHORA);

		assertThat(generado.valorEnClaro()).isNotBlank();
		assertThat(generado.token().valorHash()).isEqualTo(TokenDeUsuario.hash(generado.valorEnClaro()));
		assertThat(generado.token().valorHash()).isNotEqualTo(generado.valorEnClaro());
	}

	@Test
	void cadaTokenGeneradoEsDistinto() {
		TokenDeUsuario.TokenGenerado primero = TokenDeUsuario.generar(USUARIO, PropositoDeToken.VERIFICACION, AHORA);
		TokenDeUsuario.TokenGenerado segundo = TokenDeUsuario.generar(USUARIO, PropositoDeToken.VERIFICACION, AHORA);

		assertThat(primero.valorEnClaro()).isNotEqualTo(segundo.valorEnClaro());
	}

	@Test
	void laVerificacionVence24HorasDespues() {
		TokenDeUsuario token = TokenDeUsuario.generar(USUARIO, PropositoDeToken.VERIFICACION, AHORA).token();

		assertThat(token.expiraEn()).isEqualTo(AHORA.plusSeconds(24 * 60 * 60));
	}

	@Test
	void laRecuperacionVence1HoraDespues() {
		TokenDeUsuario token = TokenDeUsuario.generar(USUARIO, PropositoDeToken.RECUPERACION, AHORA).token();

		assertThat(token.expiraEn()).isEqualTo(AHORA.plusSeconds(60 * 60));
	}

	@Test
	void usarUnTokenVigenteDevuelveUnaCopiaUsada() {
		TokenDeUsuario token = TokenDeUsuario.generar(USUARIO, PropositoDeToken.VERIFICACION, AHORA).token();

		TokenDeUsuario usado = token.usar(AHORA.plusSeconds(60));

		assertThat(usado.usadoEn()).isEqualTo(AHORA.plusSeconds(60));
		assertThat(token.usadoEn()).isNull();
	}

	@Test
	void usarUnTokenVencidoLanzaTokenDeCuentaInvalido() {
		TokenDeUsuario token = TokenDeUsuario.generar(USUARIO, PropositoDeToken.RECUPERACION, AHORA).token();
		Instant despuesDeVencer = AHORA.plusSeconds(60 * 60 + 1);

		assertThatThrownBy(() -> token.usar(despuesDeVencer))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void usarUnTokenYaUsadoLanzaTokenDeCuentaInvalido() {
		TokenDeUsuario usado = TokenDeUsuario.generar(USUARIO, PropositoDeToken.VERIFICACION, AHORA).token()
				.usar(AHORA.plusSeconds(60));

		assertThatThrownBy(() -> usado.usar(AHORA.plusSeconds(120)))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void elTokenEsParaElPropositoConElQueSeGenero() {
		TokenDeUsuario token = TokenDeUsuario.generar(USUARIO, PropositoDeToken.RECUPERACION, AHORA).token();

		assertThat(token.esPara(PropositoDeToken.RECUPERACION)).isTrue();
		assertThat(token.esPara(PropositoDeToken.VERIFICACION)).isFalse();
	}

}
