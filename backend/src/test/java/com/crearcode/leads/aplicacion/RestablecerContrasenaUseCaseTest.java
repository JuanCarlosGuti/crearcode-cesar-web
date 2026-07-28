package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ContrasenaPlana;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeCuentaInvalidoException;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestablecerContrasenaUseCaseTest {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");
	private static final ContrasenaPlana NUEVA = new ContrasenaPlana("contrasena-nueva");

	private FakeUsuarioRepositorio usuarios;
	private FakeTokenDeUsuarioRepositorio tokens;
	private FakeCifradorDeContrasenas cifrador;
	private RestablecerContrasenaUseCaseImpl useCase;
	private Usuario cliente;

	@BeforeEach
	void configurar() {
		usuarios = new FakeUsuarioRepositorio();
		tokens = new FakeTokenDeUsuarioRepositorio();
		cifrador = new FakeCifradorDeContrasenas();
		useCase = new RestablecerContrasenaUseCaseImpl(usuarios, tokens, cifrador,
				Clock.fixed(AHORA, ZoneOffset.UTC));
		cliente = Usuario.registrarCliente(new Correo("cliente@correo-de-prueba.com"), "hash-viejo");
		usuarios.guardar(cliente);
	}

	private String generarToken(PropositoDeToken proposito, Instant creadoEn) {
		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(cliente.id(), proposito, creadoEn);
		tokens.guardar(generado.token());
		return generado.valorEnClaro();
	}

	@Test
	void unTokenVigenteCambiaLaContrasenaYQuedaUsado() {
		String tokenEnClaro = generarToken(PropositoDeToken.RECUPERACION, AHORA.minusSeconds(60));

		useCase.restablecer(tokenEnClaro, NUEVA);

		Usuario actualizado = usuarios.buscarPorId(cliente.id()).orElseThrow();
		assertThat(actualizado.contrasenaHash()).isEqualTo(cifrador.hash(NUEVA.valor()));
		assertThat(tokens.buscarPorValorHash(TokenDeUsuario.hash(tokenEnClaro)).orElseThrow().usadoEn())
				.isEqualTo(AHORA);
	}

	@Test
	void restablecerTambienVerificaLaCuenta() {
		String tokenEnClaro = generarToken(PropositoDeToken.RECUPERACION, AHORA.minusSeconds(60));

		useCase.restablecer(tokenEnClaro, NUEVA);

		assertThat(usuarios.buscarPorId(cliente.id()).orElseThrow().verificado()).isTrue();
	}

	@Test
	void unTokenInexistenteLanzaTokenInvalido() {
		assertThatThrownBy(() -> useCase.restablecer("token-que-no-existe", NUEVA))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void unTokenDeOtroPropositoLanzaTokenInvalido() {
		String tokenDeVerificacion = generarToken(PropositoDeToken.VERIFICACION, AHORA.minusSeconds(60));

		assertThatThrownBy(() -> useCase.restablecer(tokenDeVerificacion, NUEVA))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

	@Test
	void unTokenVencidoLanzaTokenInvalidoYNoCambiaLaContrasena() {
		String vencido = generarToken(PropositoDeToken.RECUPERACION, AHORA.minusSeconds(2 * 60 * 60));

		assertThatThrownBy(() -> useCase.restablecer(vencido, NUEVA))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
		assertThat(usuarios.buscarPorId(cliente.id()).orElseThrow().contrasenaHash()).isEqualTo("hash-viejo");
	}

	@Test
	void unTokenYaUsadoLanzaTokenInvalidoYNoSePuedeReusar() {
		String tokenEnClaro = generarToken(PropositoDeToken.RECUPERACION, AHORA.minusSeconds(60));
		useCase.restablecer(tokenEnClaro, NUEVA);

		assertThatThrownBy(() -> useCase.restablecer(tokenEnClaro, new ContrasenaPlana("otra-contrasena")))
				.isInstanceOf(TokenDeCuentaInvalidoException.class);
	}

}
