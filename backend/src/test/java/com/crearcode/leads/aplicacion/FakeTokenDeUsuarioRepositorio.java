package com.crearcode.leads.aplicacion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.UsuarioId;

/** Fake en memoria de {@link TokenDeUsuarioRepositorio} para tests de casos de uso. */
class FakeTokenDeUsuarioRepositorio implements TokenDeUsuarioRepositorio {

	private final List<TokenDeUsuario> tokens = new ArrayList<>();

	@Override
	public void guardar(TokenDeUsuario token) {
		tokens.removeIf(existente -> existente.id().equals(token.id()));
		tokens.add(token);
	}

	@Override
	public Optional<TokenDeUsuario> buscarPorValorHash(String valorHash) {
		return tokens.stream().filter(t -> t.valorHash().equals(valorHash)).findFirst();
	}

	@Override
	public void invalidarActivos(UsuarioId usuarioId, PropositoDeToken proposito, Instant ahora) {
		List<TokenDeUsuario> invalidados = tokens.stream()
				.filter(t -> t.usuarioId().equals(usuarioId) && t.esPara(proposito) && t.esVigente(ahora))
				.map(t -> t.usar(ahora))
				.toList();
		invalidados.forEach(this::guardar);
	}

	@Override
	public long contarRecientes(UsuarioId usuarioId, PropositoDeToken proposito, Instant desde) {
		return tokens.stream()
				.filter(t -> t.usuarioId().equals(usuarioId) && t.esPara(proposito)
						&& !t.creadoEn().isBefore(desde))
				.count();
	}

	List<TokenDeUsuario> guardados() {
		return List.copyOf(tokens);
	}

}
