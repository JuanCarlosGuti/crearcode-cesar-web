package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.ContrasenaPlana;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EnviadorDeCorreosDeCuenta;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.RegistrarClienteUseCase;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioId;
import com.crearcode.leads.dominio.UsuarioRepositorio;

@Service
class RegistrarClienteUseCaseImpl implements RegistrarClienteUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(RegistrarClienteUseCaseImpl.class);

	private final UsuarioRepositorio usuarios;
	private final TokenDeUsuarioRepositorio tokens;
	private final CifradorDeContrasenas cifrador;
	private final EnviadorDeCorreosDeCuenta enviador;
	private final Clock reloj;

	RegistrarClienteUseCaseImpl(UsuarioRepositorio usuarios, TokenDeUsuarioRepositorio tokens,
			CifradorDeContrasenas cifrador, EnviadorDeCorreosDeCuenta enviador, Clock reloj) {
		this.usuarios = usuarios;
		this.tokens = tokens;
		this.cifrador = cifrador;
		this.enviador = enviador;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public UsuarioId registrar(Correo correo, ContrasenaPlana contrasena) {
		if (usuarios.buscarPorCorreo(correo).isPresent()) {
			throw new UsuarioYaExisteException(correo);
		}

		Usuario cliente = Usuario.registrarCliente(correo, cifrador.hash(contrasena.valor()));
		usuarios.guardar(cliente);

		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(cliente.id(), PropositoDeToken.VERIFICACION,
				Instant.now(reloj));
		tokens.guardar(generado.token());

		enviarVerificacion(correo, generado.valorEnClaro(), cliente.id());
		return cliente.id();
	}

	/**
	 * Best-effort (mismo patrón que la notificación de solicitudes): un
	 * SMTP caído no debe impedir el registro — el cliente puede pedir el
	 * reenvío después (HU-31).
	 */
	private void enviarVerificacion(Correo correo, String tokenEnClaro, UsuarioId usuarioId) {
		try {
			enviador.enviarVerificacion(correo, tokenEnClaro);
		} catch (RuntimeException excepcion) {
			LOG.warn("No se pudo enviar el correo de verificación al usuario {}", usuarioId.valor(), excepcion);
		}
	}

}
