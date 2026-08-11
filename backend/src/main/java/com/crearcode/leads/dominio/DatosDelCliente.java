package com.crearcode.leads.dominio;

/**
 * Datos del cliente a quien va dirigida la cotización. Teléfono e
 * identificación son opcionales: la app no hace nada fiscal con el NIT
 * (no valida dígito de verificación ni lo reporta), solo lo imprime en
 * el PDF si el cliente lo aporta.
 */
public record DatosDelCliente(String nombre, Correo correo, String telefono, String identificacion) {

	private static final int LONGITUD_MAXIMA_NOMBRE = 120;
	private static final int LONGITUD_MAXIMA_IDENTIFICACION = 40;

	public DatosDelCliente {
		if (nombre == null || nombre.isBlank()) {
			throw new CotizacionInvalidaException("El nombre del cliente no puede estar vacío");
		}
		nombre = nombre.trim();
		if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
			throw new CotizacionInvalidaException("El nombre del cliente supera la longitud máxima permitida");
		}
		if (correo == null) {
			throw new CotizacionInvalidaException("La cotización necesita el correo del cliente");
		}
		telefono = normalizarOpcional(telefono);
		identificacion = normalizarOpcional(identificacion);
		if (identificacion != null && identificacion.length() > LONGITUD_MAXIMA_IDENTIFICACION) {
			throw new CotizacionInvalidaException("La identificación supera la longitud máxima permitida");
		}
	}

	private static String normalizarOpcional(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}

}
