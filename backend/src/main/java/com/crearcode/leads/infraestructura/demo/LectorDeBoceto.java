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
 *
 * <p>
 * Lo que solo se pasa de largo se ajusta aquí en vez de perder el boceto
 * entero (comparación del 23 oct 2026: Opus escribió un mensaje de chat
 * de más de 60 caracteres y se perdió todo). Un texto largo se corta en
 * una palabra completa con «…» y lo que sobra de una lista se descarta.
 * Lo que rompe una regla de fondo —una cifra de dinero, un tipo fuera
 * del catálogo, algo que falta— sigue siendo falla del proveedor.
 */
final class LectorDeBoceto {

	private static final JsonMapper JSON = JsonMapper.shared();

	private LectorDeBoceto() {
	}

	static BocetoDeSolucion leer(String texto) {
		try {
			JsonNode raiz = JSON.readTree(texto);
			return new BocetoDeSolucion(ajustado(cadena(raiz, "titulo"), BocetoDeSolucion.MAXIMO_TITULO),
					cadenas(arreglo(raiz, "funcionalidades")), pantallas(arreglo(raiz, "pantallas")));
		} catch (JacksonException | BocetoInvalidoException | IllegalArgumentException excepcion) {
			throw new AsistenteNoDisponibleException("El proveedor devolvió un boceto que no se puede usar", excepcion);
		}
	}

	private static List<PantallaDeBoceto> pantallas(JsonNode arreglo) {
		List<PantallaDeBoceto> pantallas = new ArrayList<>();
		for (JsonNode pantalla : arreglo) {
			if (pantallas.size() == BocetoDeSolucion.MAXIMO_PANTALLAS) {
				break;
			}
			List<PiezaDeBoceto> piezas = new ArrayList<>();
			for (JsonNode pieza : arreglo(pantalla, "piezas")) {
				if (piezas.size() == PantallaDeBoceto.MAXIMO_PIEZAS) {
					break;
				}
				piezas.add(pieza(pieza));
			}
			pantallas.add(new PantallaDeBoceto(ajustado(cadena(pantalla, "nombre"), PantallaDeBoceto.MAXIMO_NOMBRE),
					piezas));
		}
		return pantallas;
	}

	private static PiezaDeBoceto pieza(JsonNode pieza) {
		String nombreDelTipo = cadena(pieza, "tipo");
		TipoDePieza tipo = TipoDePieza.desde(nombreDelTipo)
				.orElseThrow(() -> new IllegalArgumentException("Tipo de pieza fuera del catálogo: " + nombreDelTipo));
		List<ElementoDePieza> elementos = new ArrayList<>();
		for (JsonNode elemento : arreglo(pieza, "elementos")) {
			if (elementos.size() == PiezaDeBoceto.MAXIMO_ELEMENTOS) {
				break;
			}
			elementos.add(new ElementoDePieza(ajustado(cadena(elemento, "texto"), ElementoDePieza.MAXIMO_TEXTO),
					ajustado(cadena(elemento, "detalle"), ElementoDePieza.MAXIMO_DETALLE)));
		}
		return new PiezaDeBoceto(tipo, ajustado(cadena(pieza, "titulo"), PiezaDeBoceto.MAXIMO_TITULO), elementos);
	}

	private static List<String> cadenas(JsonNode arreglo) {
		List<String> cadenas = new ArrayList<>();
		for (JsonNode valor : arreglo) {
			if (!valor.isString()) {
				throw new IllegalArgumentException("Se esperaba un texto");
			}
			if (cadenas.size() < BocetoDeSolucion.MAXIMO_FUNCIONALIDADES) {
				cadenas.add(ajustado(valor.stringValue(), BocetoDeSolucion.MAXIMO_FUNCIONALIDAD));
			}
		}
		return cadenas;
	}

	/** Corta en la última palabra completa que cabe, con «…» al final. */
	static String ajustado(String texto, int maximo) {
		String limpio = texto.strip();
		if (limpio.length() <= maximo) {
			return limpio;
		}
		int corte = limpio.lastIndexOf(' ', maximo - 1);
		String base = corte > 0 ? limpio.substring(0, corte) : limpio.substring(0, maximo - 1);
		return base.stripTrailing() + "…";
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
