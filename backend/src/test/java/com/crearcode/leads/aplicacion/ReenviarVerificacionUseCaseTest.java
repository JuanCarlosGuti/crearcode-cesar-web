package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReenviarVerificacionUseCaseTest {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");
	private static final Correo CORREO = new Correo("cliente@correo-de-prueba.com");

	private FakeUsuarioRepositorio usuarios;
	private FakeTokenDeUsuarioRepositorio tokens;
	private FakeEnviadorDeCorreosDeCuenta enviador;
	private ReenviarVerificacionUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		usuarios = new FakeUsuarioRepositorio();
		tokens = new FakeTokenDeUsuarioRepositorio();
		enviador = new FakeEnviadorDeCorreosDeCuenta();
		useCase = new ReenviarVerificacionUseCaseImpl(usuarios, tokens, enviador,
				Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	@Test
	void reenviaConUnTokenNuevoEInvalidaLosAnteriores() {
		Usuario cliente = Usuario.registrarCliente(CORREO, "hash");
		usuarios.guardar(cliente);
		TokenDeUsuario anterior = TokenDeUsuario
				.generar(cliente.id(), PropositoDeToken.VERIFICACION, AHORA.minusSeconds(3600)).token();
		tokens.guardar(anterior);

		useCase.reenviar(CORREO.valor());

		assertThat(enviador.verificacionesEnviadas()).hasSize(1);
		assertThat(tokens.buscarPorValorHash(anterior.valorHash()).orElseThrow().usadoEn()).isNotNull();
		assertThat(tokens.guardados()).hasSize(2);
	}

	@Test
	void esSilenciosoSiElCorreoNoExiste() {
		assertThatCode(() -> useCase.reenviar("no-existe@correo-de-prueba.com")).doesNotThrowAnyException();
		assertThat(enviador.verificacionesEnviadas()).isEmpty();
	}

	@Test
	void esSilenciosoSiElCorreoTieneFormatoInvalido() {
		assertThatCode(() -> useCase.reenviar("esto-no-es-un-correo")).doesNotThrowAnyException();
		assertThat(enviador.verificacionesEnviadas()).isEmpty();
	}

	@Test
	void esSilenciosoSiLaCuentaYaEstaVerificada() {
		usuarios.guardar(Usuario.registrarCliente(CORREO, "hash").verificar());

		useCase.reenviar(CORREO.valor());

		assertThat(enviador.verificacionesEnviadas()).isEmpty();
	}

	@Test
	void noEnviaMasDeTresCorreosEnQuinceMinutos() {
		Usuario cliente = Usuario.registrarCliente(CORREO, "hash");
		usuarios.guardar(cliente);

		useCase.reenviar(CORREO.valor());
		useCase.reenviar(CORREO.valor());
		useCase.reenviar(CORREO.valor());
		useCase.reenviar(CORREO.valor());

		assertThat(enviador.verificacionesEnviadas()).hasSize(3);
	}

}
