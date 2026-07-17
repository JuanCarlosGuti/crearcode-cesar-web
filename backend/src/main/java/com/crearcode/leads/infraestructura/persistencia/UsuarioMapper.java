package com.crearcode.leads.infraestructura.persistencia;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioId;

/** Traducción explícita entre {@link Usuario} y {@link UsuarioJpaEntity}. */
final class UsuarioMapper {

	private UsuarioMapper() {
	}

	static UsuarioJpaEntity aEntidad(Usuario usuario) {
		return new UsuarioJpaEntity(
				usuario.id().valor(),
				usuario.correo().valor(),
				usuario.contrasenaHash(),
				usuario.rol());
	}

	static Usuario aDominio(UsuarioJpaEntity entidad) {
		return new Usuario(
				new UsuarioId(entidad.getId()),
				new Correo(entidad.getCorreo()),
				entidad.getContrasenaHash(),
				entidad.getRol());
	}

}
