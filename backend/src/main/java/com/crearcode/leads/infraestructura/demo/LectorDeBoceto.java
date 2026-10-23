package com.crearcode.leads.infraestructura.demo;

import java.util.ArrayList;
import java.util.List;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.BocetoInvalidoException;
import com.crearcode.leads.dominio.ElementoDePieza;
import com.crearcode.leads.dominio.PantallaDeBoceto;
import com.crearcode.leads.dominio.PiezaDeBoceto;
import com.crearcode.leads.dominio.TipoDePieza;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Convierte el JSON que devuelve el proveedor en el boceto del dominio
 * (ISS-231). Se lee a mano y no con un mapeo automático porque el
 * {@code tipo} puede llegar con otras mayúsculas —la API no las
 * garantiza en un enum— y porque cualquier cosa ilegible o fuera de las
 * reglas es una falla del proveedor: pasa al respaldo y el visitante
 * nunca la ve como un error suyo.
 */
final class LectorDeBoceto {

	private static final JsonMapper JSON = JsonMapper.shared();

	private LectorDeBoceto() {
	}

	static BocetoDeSolucion leer(String texto) {
		try {
			JsonNode raiz = JSON.readTree(texto);
			return new BocetoDeSolucion(cadena(raiz, "titulo"), cadenas(arreglo(raiz, "funcionalidades")),
					pantallas(arreglo(raiz, "pantallas")));
		} catch (JacksonException | BocetoInvalidoException | IllegalArgumentException excepcion) {
			throw new AsistenteNoDisponibleException("El proveedor devolvió un boceto que no se puede usar", excepcion);
		}
	}

	private static List<PantallaDeBoceto> pantallas(JsonNode arreglo) {
		List<PantallaDeBoceto> pantallas = new ArrayList<>();
		for (JsonNode pantalla : arreglo) {
			List<PiezaDeBoceto> piezas = new ArrayList<>();
			for (JsonNode pieza : arreglo(pantalla, "piezas")) {
				piezas.add(pieza(pieza));
			}
			pantallas.add(new PantallaDeBoceto(cadena(pantalla, "nombre"), piezas));
		}
		return pantallas;
	}

	private static PiezaDeBoceto pieza(JsonNode pieza) {
		String nombreDelTipo = cadena(pieza, "tipo");
		TipoDePieza tipo = TipoDePieza.desde(nombreDelTipo)
				.orElseThrow(() -> new IllegalArgumentException("Tipo de pieza fuera del catálogo: " + nombreDelTipo));
		List<ElementoDePieza> elementos = new ArrayList<>();
		for (JsonNode elemento : arreglo(pieza, "elementos")) {
			elementos.add(new ElementoDePieza(cadena(elemento, "texto"), cadena(elemento, "detalle")));
		}
		return new PiezaDeBoceto(tipo, cadena(pieza, "titulo"), elementos);
	}

	private static List<String> cadenas(JsonNode arreglo) {
		List<String> cadenas = new ArrayList<>();
		for (JsonNode valor : arreglo) {
			if (!valor.isString()) {
				throw new IllegalArgumentException("Se esperaba un texto");
			}
			cadenas.add(valor.stringValue());
		}
		return cadenas;
	}

	private static String cadena(JsonNode nodo, String campo) {
		JsonNode valor = nodo.get(campo);
		if (valor == null || !valor.isString()) {
			throw new IllegalArgumentException("Falta el texto " + campo);
		}
		return valor.stringValue();
	}

	private static JsonNode arreglo(JsonNode nodo, String campo) {
		JsonNode valor = nodo.get(campo);
		if (valor == null || !valor.isArray()) {
			throw new IllegalArgumentException("Falta la lista " + campo);
		}
		return valor;
	}

}
