package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import com.crearcode.leads.dominio.BocetoDeDemo;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.IdentidadDeMarca;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * El boceto tal como lo dibuja el sitio (ADR-16): solo datos, que el
 * frontend interpola en piezas propias. La imagen es opcional desde
 * ISS-233: {@code imagenBase64} y {@code tipoMime} van en null cuando
 * no se pidió o su proveedor falló.
 */
record BocetoDemoResponse(IdentidadResponse identidad, String titulo, List<String> funcionalidades,
		List<PantallaResponse> pantallas, String imagenBase64, String tipoMime) {

	/** Decisión 35: la marca que propone la IA, que el sitio dibuja. */
	record IdentidadResponse(String nombre, String eslogan, String paleta, LogoResponse logo) {
	}

	record LogoResponse(String forma, String simbolo, String letra) {
	}

	record PantallaResponse(String nombre, List<PiezaResponse> piezas) {
	}

	record PiezaResponse(String tipo, String titulo, List<ElementoResponse> elementos) {
	}

	record ElementoResponse(String texto, String detalle, String icono) {
	}

	static BocetoDemoResponse desde(BocetoDeDemo demo) {
		BocetoDeSolucion solucion = demo.solucion();
		List<PantallaResponse> pantallas = solucion.pantallas().stream()
				.map(pantalla -> new PantallaResponse(pantalla.nombre(), pantalla.piezas().stream()
						.map(pieza -> new PiezaResponse(pieza.tipo().name(), pieza.titulo(), pieza.elementos().stream()
								.map(elemento -> new ElementoResponse(elemento.texto(), elemento.detalle(), elemento.icono()))
								.toList()))
						.toList()))
				.toList();
		IdentidadDeMarca marca = solucion.identidad();
		IdentidadResponse identidad = new IdentidadResponse(marca.nombre(), marca.eslogan(), marca.paleta().name(),
				new LogoResponse(marca.logo().forma().name(), marca.logo().simbolo(), marca.logo().letra().name()));
		return new BocetoDemoResponse(identidad, solucion.titulo(), solucion.funcionalidades(), pantallas,
				demo.imagen().map(ImagenGenerada::base64).orElse(null),
				demo.imagen().map(ImagenGenerada::tipoMime).orElse(null));
	}

}
