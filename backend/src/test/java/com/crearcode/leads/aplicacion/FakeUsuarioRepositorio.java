package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

/** Fake en memoria de {@link UsuarioRepositorio} para tests de casos de uso. */
class FakeUsuarioRepositorio implements UsuarioRepositorio {

	private final List<Usuario> usuarios = new ArrayList<>();

	@Override
	public void guardar(Usuario usuario) {
		usuarios.removeIf(existente -> existente.id().equals(usuario.id()));
		usuarios.add(usuario);
	}

	@Override
	public Optional<Usuario> buscarPorCorreo(Correo correo) {
		return usuarios.stream()
				.filter(u -> u.correo().valor().equalsIgnoreCase(correo.valor()))
				.findFirst();
	}

}
