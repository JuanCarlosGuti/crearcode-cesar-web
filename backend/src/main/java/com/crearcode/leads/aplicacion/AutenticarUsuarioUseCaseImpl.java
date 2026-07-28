package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.crearcode.leads.dominio.AutenticarUsuarioUseCase;
import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContactoInvalidosException;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.SesionAutenticada;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

@Service
class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

	private final UsuarioRepositorio repositorio;
	private final CifradorDeContrasenas cifrador;
	private final GeneradorDeToken generadorDeToken;
	private final Clock reloj;

	AutenticarUsuarioUseCaseImpl(UsuarioRepositorio repositorio, CifradorDeContrasenas cifrador,
			GeneradorDeToken generadorDeToken, Clock reloj) {
		this.repositorio = repositorio;
		this.cifrador = cifrador;
		this.generadorDeToken = generadorDeToken;
		this.reloj = reloj;
	}

	@Override
	public SesionAutenticada autenticar(String correo, String contrasenaEnClaro) {
		Usuario usuario = buscarPorCorreo(correo).orElseThrow(CredencialesInvalidasException::new);

		if (!cifrador.verificar(contrasenaEnClaro, usuario.contrasenaHash())) {
			throw new CredencialesInvalidasException();
		}

		if (!usuario.verificado()) {
			throw new CuentaNoVerificadaException();
		}

		return generadorDeToken.generar(usuario, Instant.now(reloj));
	}

	private Optional<Usuario> buscarPorCorreo(String correo) {
		try {
			return repositorio.buscarPorCorreo(new Correo(correo));
		} catch (DatosDeContactoInvalidosException correoConFormatoInvalido) {
			// Un correo con formato invalido tampoco existe: mismo mensaje
			// generico que "usuario no encontrado" (ver clase de la excepcion).
			return Optional.empty();
		}
	}

}
