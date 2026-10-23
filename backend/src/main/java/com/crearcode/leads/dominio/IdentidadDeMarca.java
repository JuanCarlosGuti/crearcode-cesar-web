package com.crearcode.leads.dominio;

/**
 * La identidad de marca que la IA propone para el negocio del visitante
 * (decisión 35): nombre, eslogan, paleta y logo. Es una propuesta de
 * referencia; el sitio lo dice y recuerda verificar que el nombre no
 * esté registrado por otra empresa.
 */
public record IdentidadDeMarca(String nombre, String eslogan, PaletaDeBoceto paleta, LogoDeMarca logo) {

	public static final int MAXIMO_NOMBRE = 40;
	public static final int MAXIMO_ESLOGAN = 60;

	public IdentidadDeMarca {
		nombre = TextoDeBoceto.obligatorio(nombre, MAXIMO_NOMBRE, "El nombre de la marca");
		eslogan = TextoDeBoceto.opcional(eslogan, MAXIMO_ESLOGAN, "El eslogan");
		if (paleta == null || logo == null) {
			throw new BocetoInvalidoException("La identidad necesita su paleta y su logo");
		}
	}

}
