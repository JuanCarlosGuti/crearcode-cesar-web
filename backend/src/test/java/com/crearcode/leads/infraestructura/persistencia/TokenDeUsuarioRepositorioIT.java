package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class TokenDeUsuarioRepositorioIT {

	private static final Instant AHORA = Instant.parse("2026-07-28T12:00:00Z");

	@Autowired
	private TokenDeUsuarioRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	private Usuario usuario;

	@BeforeEach
	void sembrarUsuario() {
		usuario = Usuario.registrarCliente(new Correo("cliente-tokens@crearcode-cesar-test.local"), "hash");
		usuarioRepositorio.guardar(usuario);
	}

	@Test
	void guardarYBuscarPorHashDevuelveElMismoToken() {
		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(usuario.id(), PropositoDeToken.VERIFICACION,
				AHORA);
		repositorio.guardar(generado.token());

		TokenDeUsuario encontrado = repositorio
				.buscarPorValorHash(TokenDeUsuario.hash(generado.valorEnClaro()))
				.orElseThrow();

		assertThat(encontrado.id()).isEqualTo(generado.token().id());
		assertThat(encontrado.usuarioId()).isEqualTo(usuario.id());
		assertThat(encontrado.proposito()).isEqualTo(PropositoDeToken.VERIFICACION);
		assertThat(encontrado.usadoEn()).isNull();
	}

	@Test
	void guardarUnTokenUsadoConservaSuFechaDeUso() {
		TokenDeUsuario token = TokenDeUsuario.generar(usuario.id(), PropositoDeToken.RECUPERACION, AHORA).token();
		repositorio.guardar(token.usar(AHORA.plusSeconds(60)));

		TokenDeUsuario encontrado = repositorio.buscarPorValorHash(token.valorHash()).orElseThrow();

		assertThat(encontrado.usadoEn()).isEqualTo(AHORA.plusSeconds(60));
	}

	@Test
	void invalidarActivosMarcaSoloLosVigentesDelProposito() {
		TokenDeUsuario vigente = TokenDeUsuario.generar(usuario.id(), PropositoDeToken.VERIFICACION, AHORA).token();
		TokenDeUsuario deOtroProposito = TokenDeUsuario.generar(usuario.id(), PropositoDeToken.RECUPERACION, AHORA)
				.token();
		repositorio.guardar(vigente);
		repositorio.guardar(deOtroProposito);

		repositorio.invalidarActivos(usuario.id(), PropositoDeToken.VERIFICACION, AHORA.plusSeconds(60));

		assertThat(repositorio.buscarPorValorHash(vigente.valorHash()).orElseThrow().usadoEn()).isNotNull();
		assertThat(repositorio.buscarPorValorHash(deOtroProposito.valorHash()).orElseThrow().usadoEn()).isNull();
	}

	@Test
	void contarRecientesCuentaSoloDesdeElInstanteDado() {
		repositorio.guardar(TokenDeUsuario.generar(usuario.id(), PropositoDeToken.VERIFICACION, AHORA).token());
		repositorio.guardar(
				TokenDeUsuario.generar(usuario.id(), PropositoDeToken.VERIFICACION, AHORA.minusSeconds(3600)).token());

		long recientes = repositorio.contarRecientes(usuario.id(), PropositoDeToken.VERIFICACION,
				AHORA.minusSeconds(900));

		assertThat(recientes).isEqualTo(1);
	}

}
