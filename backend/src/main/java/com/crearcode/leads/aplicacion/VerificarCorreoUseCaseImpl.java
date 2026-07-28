package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.TokenDeCuentaInvalidoException;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;
import com.crearcode.leads.dominio.VerificarCorreoUseCase;

@Service
class VerificarCorreoUseCaseImpl implements VerificarCorreoUseCase {

	private final UsuarioRepositorio usuarios;
	private final TokenDeUsuarioRepositorio tokens;
	private final Clock reloj;

	VerificarCorreoUseCaseImpl(UsuarioRepositorio usuarios, TokenDeUsuarioRepositorio tokens, Clock reloj) {
		this.usuarios = usuarios;
		this.tokens = tokens;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public void verificar(String tokenEnClaro) {
		TokenDeUsuario token = tokens.buscarPorValorHash(TokenDeUsuario.hash(tokenEnClaro))
				.filter(t -> t.esPara(PropositoDeToken.VERIFICACION))
				.orElseThrow(TokenDeCuentaInvalidoException::new);

		TokenDeUsuario usado = token.usar(Instant.now(reloj));

		Usuario usuario = usuarios.buscarPorId(token.usuarioId())
				.orElseThrow(TokenDeCuentaInvalidoException::new);

		usuarios.guardar(usuario.verificar());
		tokens.guardar(usado);
	}

}
