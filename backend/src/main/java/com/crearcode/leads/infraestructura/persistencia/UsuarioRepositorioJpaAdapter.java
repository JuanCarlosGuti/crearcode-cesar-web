package com.crearcode.leads.infraestructura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioId;
import com.crearcode.leads.dominio.UsuarioRepositorio;

@Component
class UsuarioRepositorioJpaAdapter implements UsuarioRepositorio {

	private final UsuarioJpaRepository jpaRepository;

	UsuarioRepositorioJpaAdapter(UsuarioJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void guardar(Usuario usuario) {
		jpaRepository.save(UsuarioMapper.aEntidad(usuario));
	}

	@Override
	public Optional<Usuario> buscarPorCorreo(Correo correo) {
		return jpaRepository.findByCorreoIgnoreCase(correo.valor()).map(UsuarioMapper::aDominio);
	}

	@Override
	public Optional<Usuario> buscarPorId(UsuarioId id) {
		return jpaRepository.findById(id.valor()).map(UsuarioMapper::aDominio);
	}

}
