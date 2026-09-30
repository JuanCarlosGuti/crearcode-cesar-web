package com.crearcode.leads.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EliminarMiCuentaUseCase;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

@Service
class EliminarMiCuentaUseCaseImpl implements EliminarMiCuentaUseCase {

	private final UsuarioRepositorio usuarios;
	private final TokenDeUsuarioRepositorio tokens;

	EliminarMiCuentaUseCaseImpl(UsuarioRepositorio usuarios, TokenDeUsuarioRepositorio tokens) {
		this.usuarios = usuarios;
		this.tokens = tokens;
	}

	/**
	 * El correo llega del token de sesión, nunca de la petición: así
	 * nadie puede borrar una cuenta ajena escribiendo otro correo.
	 */
	@Override
	@Transactional
	public void eliminar(String correo) {
		Usuario usuario = usuarios.buscarPorCorreo(new Correo(correo))
				.orElseThrow(UsuarioNoEncontradoException::new);
		if (usuario.rol() == Rol.ADMIN) {
			throw new EliminacionNoPermitidaException();
		}

		// Primero los tokens: son hijos de la cuenta y sin esto quedaría
		// un enlace de recuperación vivo apuntando a nadie.
		tokens.eliminarDe(usuario.id());
		usuarios.eliminar(usuario.id());
	}

}
