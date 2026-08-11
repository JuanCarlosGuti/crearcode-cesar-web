package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

/**
 * Crea un cliente ya verificado y devuelve su token, emitiéndolo con el
 * mismo generador que usa el login real.
 *
 * <p>No pasa por {@code POST /api/auth/login} a propósito: ese endpoint
 * tiene su propio rate limit estricto (5/15 min por IP, ISS-060) y una
 * clase de test necesita varios clientes distintos. El token resultante
 * es igual de real —mismo firmante, mismos claims—, y el flujo de login
 * ya tiene sus ITs en F8. Además cachea por correo, como haría una
 * sesión de verdad.
 */
final class ClienteDePrueba {

	private static final Map<String, String> TOKENS_POR_CORREO = new ConcurrentHashMap<>();

	private ClienteDePrueba() {
	}

	static String registrarYAutenticar(UsuarioRepositorio usuarios, GeneradorDeToken generadorDeToken,
			String correo) {
		return TOKENS_POR_CORREO.computeIfAbsent(correo, clave -> {
			Usuario cliente = usuarios.buscarPorCorreo(new Correo(clave))
					.orElseGet(() -> {
						Usuario nuevo = Usuario.crear(new Correo(clave), "hash-que-no-se-usa", Rol.CLIENTE);
						usuarios.guardar(nuevo);
						return nuevo;
					});
			return generadorDeToken.generar(cliente, Instant.now()).token();
		});
	}

}
