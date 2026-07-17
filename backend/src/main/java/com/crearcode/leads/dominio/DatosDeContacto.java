package com.crearcode.leads.dominio;

/**
 * Value object que agrupa los datos de contacto de un lead. Correo y
 * teléfono llegan ya validados como VOs propios; aquí solo se valida lo
 * que les es propio a este VO: nombre y empresa.
 */
public record DatosDeContacto(String nombre, String empresa, Correo correo, Telefono telefono) {

	private static final int LONGITUD_MAXIMA = 120;

	public DatosDeContacto {
		if (nombre == null || nombre.isBlank()) {
			throw new DatosDeContactoInvalidosException("El nombre no puede estar vacío");
		}
		if (nombre.length() > LONGITUD_MAXIMA) {
			throw new DatosDeContactoInvalidosException("El nombre supera la longitud máxima permitida");
		}
		if (empresa != null && empresa.length() > LONGITUD_MAXIMA) {
			throw new DatosDeContactoInvalidosException("La empresa supera la longitud máxima permitida");
		}
		if (correo == null) {
			throw new DatosDeContactoInvalidosException("El correo es obligatorio");
		}
		if (telefono == null) {
			throw new DatosDeContactoInvalidosException("El teléfono es obligatorio");
		}
	}

}
