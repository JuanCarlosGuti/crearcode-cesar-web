package com.crearcode.leads.infraestructura.seguridad;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Comprueba al arrancar que las credenciales sensibles no son las de
 * desarrollo.
 *
 * <p>
 * {@code ADMIN_PASSWORD} y {@code JWT_SECRET} traen un valor por
 * defecto para que el proyecto arranque recién clonado, y eso está
 * bien. Lo que no está bien es que ese valor pueda llegar a producción
 * sin que nadie se entere: un secreto que no se carga no rompe nada
 * visible — la app arranca, el login funciona— y cualquiera que haya
 * leído el repositorio puede firmar un token de administrador válido.
 * Es la clase de fallo que solo se descubre cuando ya lo usaron.
 *
 * <p>
 * La bandera {@code app.seguridad.permitir-credenciales-de-desarrollo}
 * viene en {@code true} para no romper el arranque local, los tests ni
 * los e2e —este proyecto no usa perfiles de Spring, así que no hay
 * otra señal de «esto es producción»— y el despliegue la pone en
 * {@code false} ({@code config/deploy.api.yml}). Con ella en
 * {@code false}, arrancar con los valores por defecto es un error de
 * arranque y no una nota al pie.
 *
 * <p>
 * Aun con la bandera en {@code true} se registra un WARN: si algún día
 * un entorno nuevo se olvida de ponerla en {@code false}, el aviso
 * sigue estando en la primera pantalla de logs.
 */
@Component
class CredencialesDeArranque {

	private static final Logger LOG = LoggerFactory.getLogger(CredencialesDeArranque.class);

	private static final String ADMIN_POR_DEFECTO = "cambiar-en-produccion";
	private static final String JWT_POR_DEFECTO = "cambiar-este-secreto-en-produccion-1234567890";
	/** HS256 firma con 256 bits: una clave más corta se puede buscar. */
	private static final int LONGITUD_MINIMA_DEL_SECRETO = 32;

	private final boolean permitirCredencialesDeDesarrollo;
	private final String contrasenaDeAdmin;
	private final String secretoJwt;

	CredencialesDeArranque(
			@Value("${app.seguridad.permitir-credenciales-de-desarrollo}") boolean permitirCredencialesDeDesarrollo,
			@Value("${app.admin.password}") String contrasenaDeAdmin,
			@Value("${app.jwt.secreto}") String secretoJwt) {
		this.permitirCredencialesDeDesarrollo = permitirCredencialesDeDesarrollo;
		this.contrasenaDeAdmin = contrasenaDeAdmin;
		this.secretoJwt = secretoJwt;
	}

	@PostConstruct
	void verificar() {
		List<String> faltantes = new ArrayList<>();
		if (esInservible(contrasenaDeAdmin, ADMIN_POR_DEFECTO, 1)) {
			faltantes.add("ADMIN_PASSWORD");
		}
		if (esInservible(secretoJwt, JWT_POR_DEFECTO, LONGITUD_MINIMA_DEL_SECRETO)) {
			faltantes.add("JWT_SECRET");
		}
		if (faltantes.isEmpty()) {
			return;
		}
		if (permitirCredencialesDeDesarrollo) {
			LOG.warn("Arrancando con credenciales de desarrollo en {}. En producción esto no debe pasar: "
					+ "cárgalas por entorno y pon "
					+ "app.seguridad.permitir-credenciales-de-desarrollo=false", faltantes);
			return;
		}
		// Sin valores en el mensaje: va a los logs del servidor.
		throw new IllegalStateException("Faltan credenciales reales por variable de entorno: " + faltantes
				+ ". Cárgalas antes de arrancar, o pon "
				+ "app.seguridad.permitir-credenciales-de-desarrollo=true si esto es un entorno local.");
	}

	private static boolean esInservible(String valor, String porDefecto, int longitudMinima) {
		return valor == null || valor.isBlank() || valor.equals(porDefecto) || valor.length() < longitudMinima;
	}

}
