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
	/**
	 * Hash de un valor cualquiera, calculado una vez al arrancar. Con
	 * un correo que no existe se salia sin verificar nada, y verificar
	 * un hash de BCrypt cuesta del orden de 100 ms: la diferencia de
	 * tiempo entre «este correo no existe» y «existe pero la clave está
	 * mal» se mide desde fuera con un cronómetro, así que la respuesta
	 * genérica no ocultaba nada (auditoría del 28 sep 2026, P3-a).
	 */
	private final String hashSenuelo;

	AutenticarUsuarioUseCaseImpl(UsuarioRepositorio repositorio, CifradorDeContrasenas cifrador,
			GeneradorDeToken generadorDeToken, Clock reloj) {
		this.repositorio = repositorio;
		this.cifrador = cifrador;
		this.generadorDeToken = generadorDeToken;
		this.reloj = reloj;
		this.hashSenuelo = cifrador.hash("contrasena-que-nadie-usa-senuelo-anti-timing");
	}

	@Override
	public SesionAutenticada autenticar(String correo, String contrasenaEnClaro) {
		Optional<Usuario> encontrado = buscarPorCorreo(correo);
		if (encontrado.isEmpty()) {
			// Se verifica igual, contra el señuelo, para gastar el mismo
			// tiempo que si el correo existiera. El resultado se ignora a
			// propósito.
			cifrador.verificar(contrasenaEnClaro, hashSenuelo);
			throw new CredencialesInvalidasException();
		}
		Usuario usuario = encontrado.get();

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
