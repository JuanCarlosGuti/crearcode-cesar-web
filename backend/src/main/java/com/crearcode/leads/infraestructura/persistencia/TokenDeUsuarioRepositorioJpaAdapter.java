package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.UsuarioId;

@Component
class TokenDeUsuarioRepositorioJpaAdapter implements TokenDeUsuarioRepositorio {

	private final TokenDeUsuarioJpaRepository jpaRepository;

	TokenDeUsuarioRepositorioJpaAdapter(TokenDeUsuarioJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void guardar(TokenDeUsuario token) {
		jpaRepository.save(TokenDeUsuarioMapper.aEntidad(token));
	}

	@Override
	public Optional<TokenDeUsuario> buscarPorValorHash(String valorHash) {
		return jpaRepository.findByValorHash(valorHash).map(TokenDeUsuarioMapper::aDominio);
	}

	@Override
	public void invalidarActivos(UsuarioId usuarioId, PropositoDeToken proposito, Instant ahora) {
		jpaRepository.invalidarActivos(usuarioId.valor(), proposito, ahora);
	}

	@Override
	public long contarRecientes(UsuarioId usuarioId, PropositoDeToken proposito, Instant desde) {
		return jpaRepository.countByUsuarioIdAndPropositoAndCreadoEnGreaterThanEqual(usuarioId.valor(), proposito,
				desde);
	}

}
