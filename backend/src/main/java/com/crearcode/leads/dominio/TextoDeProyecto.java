package com.crearcode.leads.dominio;

/**
 * Validación de los textos del contexto de proyectos: obligatorios u
 * opcionales, recortados y con un tope. Un texto opcional en blanco se
 * guarda como nulo, no como cadena vacía.
 */
final class TextoDeProyecto {

	private TextoDeProyecto() {
	}

	static String obligatorio(String valor, int maximo, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new ProyectoInvalidoException(campo + " no puede estar vacío");
		}
		return recortado(valor, maximo, campo);
	}

	static String opcional(String valor, int maximo, String campo) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return recortado(valor, maximo, campo);
	}

	private static String recortado(String valor, int maximo, String campo) {
		String recortado = valor.trim();
		if (recortado.length() > maximo) {
			throw new ProyectoInvalidoException(campo + " supera los " + maximo + " caracteres");
		}
		return recortado;
	}

}
