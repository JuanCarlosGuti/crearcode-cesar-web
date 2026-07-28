package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ContrasenaPlana;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrarClienteUseCaseTest {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");
	private static final Correo CORREO = new Correo("cliente@correo-de-prueba.com");
	private static final ContrasenaPlana CONTRASENA = new ContrasenaPlana("clave-larga-segura");

	private FakeUsuarioRepositorio usuarios;
	private FakeTokenDeUsuarioRepositorio tokens;
	private FakeEnviadorDeCorreosDeCuenta enviador;
	private FakeCifradorDeContrasenas cifrador;
	private RegistrarClienteUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		usuarios = new FakeUsuarioRepositorio();
		tokens = new FakeTokenDeUsuarioRepositorio();
		enviador = new FakeEnviadorDeCorreosDeCuenta();
		cifrador = new FakeCifradorDeContrasenas();
		Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
		useCase = new RegistrarClienteUseCaseImpl(usuarios, tokens, cifrador, enviador, reloj);
	}

	@Test
	void registraUnClienteSinVerificarConLaContrasenaHasheada() {
		UsuarioId id = useCase.registrar(CORREO, CONTRASENA);

		Usuario guardado = usuarios.buscarPorId(id).orElseThrow();
		assertThat(guardado.rol()).isEqualTo(Rol.CLIENTE);
		assertThat(guardado.verificado()).isFalse();
		assertThat(guardado.contrasenaHash()).isEqualTo(cifrador.hash("clave-larga-segura"));
	}

	@Test
	void generaUnTokenDeVerificacionYEnviaElCorreoConElValorEnClaro() {
		useCase.registrar(CORREO, CONTRASENA);

		assertThat(tokens.guardados()).hasSize(1);
		TokenDeUsuario token = tokens.guardados().get(0);
		assertThat(token.esPara(PropositoDeToken.VERIFICACION)).isTrue();

		assertThat(enviador.verificacionesEnviadas()).hasSize(1);
		FakeEnviadorDeCorreosDeCuenta.Envio envio = enviador.verificacionesEnviadas().get(0);
		assertThat(envio.destino()).isEqualTo(CORREO);
		assertThat(TokenDeUsuario.hash(envio.tokenEnClaro())).isEqualTo(token.valorHash());
	}

	@Test
	void rechazaUnCorreoYaRegistrado() {
		useCase.registrar(CORREO, CONTRASENA);

		assertThatThrownBy(() -> useCase.registrar(CORREO, new ContrasenaPlana("otra-clave-larga")))
				.isInstanceOf(UsuarioYaExisteException.class);
	}

	@Test
	void unFalloDelCorreoNoImpideElRegistro() {
		enviador.simularFallo();

		UsuarioId id = useCase.registrar(CORREO, CONTRASENA);

		assertThat(usuarios.buscarPorId(id)).isPresent();
		assertThat(tokens.guardados()).hasSize(1);
	}

}
