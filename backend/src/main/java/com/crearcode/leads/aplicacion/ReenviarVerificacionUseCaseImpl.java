package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContactoInvalidosException;
import com.crearcode.leads.dominio.EnviadorDeCorreosDeCuenta;
import com.crearcode.leads.dominio.PropositoDeToken;
import com.crearcode.leads.dominio.ReenviarVerificacionUseCase;
import com.crearcode.leads.dominio.TokenDeUsuario;
import com.crearcode.leads.dominio.TokenDeUsuarioRepositorio;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

/**
 * Deliberadamente silencioso en todos los caminos tristes (correo
 * inexistente, malformado, ya verificado, límite alcanzado): nunca
 * revela el estado de una cuenta (invariantes 6 y 7 del contexto).
 */
@Service
class ReenviarVerificacionUseCaseImpl implements ReenviarVerificacionUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(ReenviarVerificacionUseCaseImpl.class);
	static final int MAX_ENVIOS_RECIENTES = 3;
	static final Duration VENTANA_DE_ENVIOS = Duration.ofMinutes(15);

	private final UsuarioRepositorio usuarios;
	private final TokenDeUsuarioRepositorio tokens;
	private final EnviadorDeCorreosDeCuenta enviador;
	private final Clock reloj;

	ReenviarVerificacionUseCaseImpl(UsuarioRepositorio usuarios, TokenDeUsuarioRepositorio tokens,
			EnviadorDeCorreosDeCuenta enviador, Clock reloj) {
		this.usuarios = usuarios;
		this.tokens = tokens;
		this.enviador = enviador;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public void reenviar(String correoTexto) {
		Optional<Usuario> encontrado = buscarPorCorreo(correoTexto);
		if (encontrado.isEmpty() || encontrado.get().verificado()) {
			return;
		}
		Usuario usuario = encontrado.get();
		Instant ahora = Instant.now(reloj);

		if (tokens.contarRecientes(usuario.id(), PropositoDeToken.VERIFICACION,
				ahora.minus(VENTANA_DE_ENVIOS)) >= MAX_ENVIOS_RECIENTES) {
			return;
		}

		tokens.invalidarActivos(usuario.id(), PropositoDeToken.VERIFICACION, ahora);
		TokenDeUsuario.TokenGenerado generado = TokenDeUsuario.generar(usuario.id(), PropositoDeToken.VERIFICACION,
				ahora);
		tokens.guardar(generado.token());

		try {
			enviador.enviarVerificacion(usuario.correo(), generado.valorEnClaro());
		} catch (RuntimeException excepcion) {
			LOG.warn("No se pudo reenviar el correo de verificación al usuario {}", usuario.id().valor(), excepcion);
		}
	}

	private Optional<Usuario> buscarPorCorreo(String correoTexto) {
		try {
			return usuarios.buscarPorCorreo(new Correo(correoTexto));
		} catch (DatosDeContactoInvalidosException formatoInvalido) {
			return Optional.empty();
		}
	}

}
