package com.crearcode.leads.dominio;

import java.util.Optional;

/**
 * El resultado del demo de diseño (HU-42): el boceto de la solución
 * —título, funcionalidades y pantallas, que el sitio dibuja (ADR-16)— y,
 * si está encendida, una imagen de referencia que se muestra SOLO como
 * imagen, nunca HTML. Desde ISS-233 la imagen es complementaria: si su
 * proveedor falla, el boceto sale igual y sin ella.
 */
public record BocetoDeDemo(BocetoDeSolucion solucion, Optional<ImagenGenerada> imagen) {

	public BocetoDeDemo {
		if (solucion == null) {
			throw new IllegalArgumentException("El demo necesita su boceto");
		}
		imagen = imagen == null ? Optional.empty() : imagen;
	}

}
