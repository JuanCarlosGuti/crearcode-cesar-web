package com.crearcode.leads.infraestructura.documento;

import java.util.List;

import com.crearcode.leads.dominio.Proyecto;

/**
 * Las condiciones del pie del PDF de cotización (decisión 30 de docs/10),
 * que resumen las decisiones 21 a 23: se paga por entregables, todo
 * cambio de alcance es un entregable nuevo, y hay garantía. Es un
 * compromiso comercial con quien firma, así que su texto vive aquí y en
 * ningún otro sitio del backend; el portal del cliente lo cuenta con sus
 * propias palabras en {@code contenido/proyectos.ts}.
 *
 * <p>Los días de garantía salen de {@link Proyecto#GARANTIA}: si un día
 * cambian, el PDF los cambia solo.
 */
final class CondicionesComerciales {

	private CondicionesComerciales() {
	}

	/** Una condición: su etiqueta (en negrita en el PDF) y lo que dice. */
	record Condicion(String etiqueta, String texto) {
	}

	static List<Condicion> lineas() {
		return List.of(
				new Condicion("Forma de pago:", "se paga por entregables. El primero al aceptar esta propuesta y cada "
						+ "uno de los siguientes al aprobar su entrega, salvo que se acuerde otra cosa por escrito."),
				new Condicion("Cambios de alcance:", "lo que se pida y no esté en esta cotización se cotiza aparte, "
						+ "como un entregable nuevo con su propio valor."),
				new Condicion("Garantía:", Proyecto.GARANTIA.toDays() + " días de corrección de errores a partir de "
						+ "la entrega final."));
	}

}
