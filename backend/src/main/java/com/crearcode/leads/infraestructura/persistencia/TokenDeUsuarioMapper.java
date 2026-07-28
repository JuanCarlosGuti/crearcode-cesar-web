package com.crearcode.leads.infraestructura.persistencia;

import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.UsuarioId;

/** Traducción explícita entre {@link TokenDeUsuario} y {@link TokenDeUsuarioJpaEntity}. */
final class TokenDeUsuarioMapper {

	private TokenDeUsuarioMapper() {
	}

	static TokenDeUsuarioJpaEntity aEntidad(TokenDeUsuario token) {
		return new TokenDeUsuarioJpaEntity(
				token.id(),
				token.usuarioId().valor(),
				token.valorHash(),
				token.proposito(),
				token.creadoEn(),
				token.expiraEn(),
				token.usadoEn());
	}

	static TokenDeUsuario aDominio(TokenDeUsuarioJpaEntity entidad) {
		return new TokenDeUsuario(
				entidad.getId(),
				new UsuarioId(entidad.getUsuarioId()),
				entidad.getValorHash(),
				entidad.getProposito(),
				entidad.getCreadoEn(),
				entidad.getExpiraEn(),
				entidad.getUsadoEn());
	}

}
