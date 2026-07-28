package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeCuentaInvalidoException;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificarCorreoUseCaseTest {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");

	private FakeUsuarioRepositorio usuarios;
	private FakeTokenDeUsuarioRepositorio tokens;
	private VerificarCorreoUseCaseImpl useCase;
	private Usuario cliente;

	@BeforeEach
	void configurar() {
		usuarios = new FakeUsuarioRepositorio();
		tokens = new FakeTokenDeUsuarioRepositorio();
		useCase = new VerificarCorreoUseCaseImpl(usuarios, tokens, Clock.fixed(AHORA, ZoneOffset.UTC));
		cliente = Usuario.registrarCliente(new Correo("cliente@correo-de-prueba.com"), "hash");
		usuarios.guardar(cliente);
	}

	private String generarToken(PropositoDeToken proposito, Instant creadoEn) {
		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(cliente.id(), proposito, creadoEn);
		tokens.guardar(generado.token());
		return generado.valorEnClaro();
	}

	@Test
	void unTokenVigenteVerificaLaCuentaYQuedaUsado() {
		String tokenEnClaro = generarToken(PropositoDeToken.VERIFICACION, AHORA.minusSeconds(60));

		useCase.verificar(tokenEnClaro);

		assertThat(usuarios.buscarPorId(cliente.id()).orElseThrow().verificado()).isTrue();
		assertThat(tokens.buscarPorValorHash(TokenDeUsuario.hash(tokenEnClaro)).orElseThrow().usadoEn())
				.isEqualTo(AHORA);
	}

	@Test
	void unTokenInexistenteLanzaTokenInvalido() {
		assertThatThrownBy(() -> useCase.verificar("token-que-no-existe"))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void unTokenDeOtroPropositoLanzaTokenInvalido() {
		String tokenDeRecuperacion = generarToken(PropositoDeToken.RECUPERACION, AHORA.minusSeconds(60));

		assertThatThrownBy(() -> useCase.verificar(tokenDeRecuperacion))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void unTokenVencidoLanzaTokenInvalido() {
		String vencido = generarToken(PropositoDeToken.VERIFICACION, AHORA.minusSeconds(25 * 60 * 60));

		assertThatThrownBy(() -> useCase.verificar(vencido))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void unTokenYaUsadoLanzaTokenInvalidoYNoSePuedeReusar() {
		String tokenEnClaro = generarToken(PropositoDeToken.VERIFICACION, AHORA.minusSeconds(60));
		useCase.verificar(tokenEnClaro);

		assertThatThrownBy(() -> useCase.verificar(tokenEnClaro))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

}
