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

class SolicitarRecuperacionUseCaseTest {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");
	private static final Correo CORREO = new Correo("cliente@correo-de-prueba.com");

	private FakeUsuarioRepositorio usuarios;
	private FakeTokenDeUsuarioRepositorio tokens;
	private FakeEnviadorDeCorreosDeCuenta enviador;
	private SolicitarRecuperacionUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		usuarios = new FakeUsuarioRepositorio();
		tokens = new FakeTokenDeUsuarioRepositorio();
		enviador = new FakeEnviadorDeCorreosDeCuenta();
		useCase = new SolicitarRecuperacionUseCaseImpl(usuarios, tokens, enviador,
				Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	@Test
	void generaUnTokenDeRecuperacionYEnviaElCorreo() {
		Usuario cliente = Usuario.registrarCliente(CORREO, "hash");
		usuarios.guardar(cliente);

		useCase.solicitar(CORREO.valor());

		assertThat(tokens.guardados()).hasSize(1);
		TokenDeUsuario token = tokens.guardados().get(0);
		assertThat(token.esPara(PropositoDeToken.RECUPERACION)).isTrue();
		assertThat(enviador.recuperacionesEnviadas()).hasSize(1);
		assertThat(TokenDeUsuario.hash(enviador.recuperacionesEnviadas().get(0).tokenEnClaro()))
				.isEqualTo(token.valorHash());
	}

	@Test
	void esSilenciosoSiElCorreoNoExiste() {
		assertThatCode(() -> useCase.solicitar("no-existe@correo-de-prueba.com")).doesNotThrowAnyException();
		assertThat(enviador.recuperacionesEnviadas()).isEmpty();
	}

	@Test
	void esSilenciosoSiElCorreoTieneFormatoInvalido() {
		assertThatCode(() -> useCase.solicitar("esto-no-es-un-correo")).doesNotThrowAnyException();
		assertThat(enviador.recuperacionesEnviadas()).isEmpty();
	}

	@Test
	void funcionaTambienParaCuentasSinVerificar() {
		usuarios.guardar(Usuario.registrarCliente(CORREO, "hash"));

		useCase.solicitar(CORREO.valor());

		assertThat(enviador.recuperacionesEnviadas()).hasSize(1);
	}

	@Test
	void noEnviaMasDeTresCorreosEnQuinceMinutos() {
		usuarios.guardar(Usuario.registrarCliente(CORREO, "hash"));

		useCase.solicitar(CORREO.valor());
		useCase.solicitar(CORREO.valor());
		useCase.solicitar(CORREO.valor());
		useCase.solicitar(CORREO.valor());

		assertThat(enviador.recuperacionesEnviadas()).hasSize(3);
	}

}
