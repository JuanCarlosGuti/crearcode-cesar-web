package com.crearcode.leads.aplicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CrearUsuarioUseCase;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioId;
import com.crearcode.leads.dominio.UsuarioRepositorio;

@Service
class CrearUsuarioUseCaseImpl implements CrearUsuarioUseCase {

	private final UsuarioRepositorio repositorio;
	private final CifradorDeContrasenas cifrador;

	CrearUsuarioUseCaseImpl(UsuarioRepositorio repositorio, CifradorDeContrasenas cifrador) {
		this.repositorio = repositorio;
		this.cifrador = cifrador;
	}

	@Override
	@Transactional
	public UsuarioId crear(Correo correo, String contrasenaEnClaro, Rol rol) {
		if (repositorio.buscarPorCorreo(correo).isPresent()) {
			throw new UsuarioYaExisteException(correo);
		}

		Usuario usuario = Usuario.crear(correo, cifrador.hash(contrasenaEnClaro), rol);
		repositorio.guardar(usuario);

		return usuario.id();
	}

}
