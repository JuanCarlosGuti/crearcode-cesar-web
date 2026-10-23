package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import com.crearcode.leads.dominio.BocetoDeDemo;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * El boceto tal como lo dibuja el sitio (ADR-16): solo datos, que el
 * frontend interpola en piezas propias. La imagen es opcional desde
 * ISS-233: {@code imagenBase64} y {@code tipoMime} van en null cuando
 * no se pidió o su proveedor falló.
 */
record BocetoDemoResponse(String titulo, List<String> funcionalidades, List<PantallaResponse> pantallas,
		String imagenBase64, String tipoMime) {

	record PantallaResponse(String nombre, List<PiezaResponse> piezas) {
	}

	record PiezaResponse(String tipo, String titulo, List<ElementoResponse> elementos) {
	}

	record ElementoResponse(String texto, String detalle) {
	}

	static BocetoDemoResponse desde(BocetoDeDemo demo) {
		BocetoDeSolucion solucion = demo.solucion();
		List<PantallaResponse> pantallas = solucion.pantallas().stream()
				.map(pantalla -> new PantallaResponse(pantalla.nombre(), pantalla.piezas().stream()
						.map(pieza -> new PiezaResponse(pieza.tipo().name(), pieza.titulo(), pieza.elementos().stream()
								.map(elemento -> new ElementoResponse(elemento.texto(), elemento.detalle()))
								.toList()))
						.toList()))
				.toList();
		return new BocetoDemoResponse(solucion.titulo(), solucion.funcionalidades(), pantallas,
				demo.imagen().map(ImagenGenerada::base64).orElse(null),
				demo.imagen().map(ImagenGenerada::tipoMime).orElse(null));
	}

}
