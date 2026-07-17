package com.crearcode.leads.infraestructura.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.crearcode.leads.aplicacion.UsuarioYaExisteException;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CrearUsuarioUseCase;
import com.crearcode.leads.dominio.Rol;

/**
 * Crea el único usuario admin al arrancar, si todavía no existe.
 * Idempotente: tolera que dos instancias arranquen a la vez (el índice
 * único sobre el correo hace fallar la segunda inserción, se ignora
 * igual que un "ya existe" detectado de forma normal). Mantiene la
 * invariante de "credenciales solo por variable de entorno, nunca en el
 * repositorio" que ya regía con el usuario hardcodeado de la fase F2.
 */
@Component
class SembradorDeUsuarioAdmin implements ApplicationRunner {

	private final CrearUsuarioUseCase crearUsuarioUseCase;
	private final String correoAdmin;
	private final String contrasenaAdmin;

	SembradorDeUsuarioAdmin(CrearUsuarioUseCase crearUsuarioUseCase,
			@Value("${app.admin.username}") String correoAdmin,
			@Value("${app.admin.password}") String contrasenaAdmin) {
		this.crearUsuarioUseCase = crearUsuarioUseCase;
		this.correoAdmin = correoAdmin;
		this.contrasenaAdmin = contrasenaAdmin;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			crearUsuarioUseCase.crear(new Correo(correoAdmin), contrasenaAdmin, Rol.ADMIN);
		} catch (UsuarioYaExisteException | DataIntegrityViolationException yaSembrado) {
			// Caso normal despues del primer arranque, o dos instancias
			// arrancando a la vez: no es un error.
		}
	}

}
