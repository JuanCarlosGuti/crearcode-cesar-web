package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.ContrasenaPlana;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.RestablecerContrasenaUseCase;
import com.crearcode.leads.dominio.TokenDeCuentaInvalidoException;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

/**
 * Usar el enlace de recuperación prueba ser dueño del correo, así que
 * además de cambiar la contraseña deja la cuenta verificada
 * (invariante 5 del contexto).
 */
@Service
class RestablecerContrasenaUseCaseImpl implements RestablecerContrasenaUseCase {

	private final UsuarioRepositorio usuarios;
	private final TokenDeUsuarioRepositorio tokens;
	private final CifradorDeContrasenas cifrador;
	private final Clock reloj;

	RestablecerContrasenaUseCaseImpl(UsuarioRepositorio usuarios, TokenDeUsuarioRepositorio tokens,
			CifradorDeContrasenas cifrador, Clock reloj) {
		this.usuarios = usuarios;
		this.tokens = tokens;
		this.cifrador = cifrador;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public void restablecer(String tokenEnClaro, ContrasenaPlana nuevaContrasena) {
		TokenDeUsuario token = tokens.buscarPorValorHash(TokenDeUsuario.hash(tokenEnClaro))
				.filter(t -> t.esPara(PropositoDeToken.RECUPERACION))
				.orElseThrow(TokenDeCuentaInvalidoException::new);

		TokenDeUsuario usado = token.usar(Instant.now(reloj));

		Usuario usuario = usuarios.buscarPorId(token.usuarioId())
				.orElseThrow(TokenDeCuentaInvalidoException::new);

		usuarios.guardar(usuario.conContrasena(cifrador.hash(nuevaContrasena.valor())).verificar());
		tokens.guardar(usado);
	}

}
