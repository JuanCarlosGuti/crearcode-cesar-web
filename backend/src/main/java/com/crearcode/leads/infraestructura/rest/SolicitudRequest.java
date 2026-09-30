package com.crearcode.leads.infraestructura.rest;

import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record SolicitudRequest(
		@NotBlank(message = "El nombre es obligatorio") String nombre,
		String empresa,
		@NotBlank(message = "El correo es obligatorio") String correo,
		@NotBlank(message = "El teléfono es obligatorio") String telefono,
		@NotNull(message = "El servicio de interés es obligatorio") ServicioDeInteres servicioDeInteres,
		@NotBlank(message = "El mensaje es obligatorio") @Size(max = SolicitudDeContacto.MAXIMO_CARACTERES_MENSAJE,
				message = "El mensaje no puede superar los " + SolicitudDeContacto.MAXIMO_CARACTERES_MENSAJE
						+ " caracteres") String mensaje,
		boolean aceptaConsentimiento,
		/**
		 * Autorización comercial, opcional y aparte de la obligatoria
		 * (política de datos v2, §13).
		 *
		 * {@code Boolean} y no {@code boolean} A PROPÓSITO: Jackson 3 no
		 * convierte un primitivo ausente en {@code false}, lo rechaza con
		 * un 400. Con {@code boolean}, cualquier cliente que no mandara el
		 * campo —un navegador con el JavaScript viejo en caché el día del
		 * despliegue, una integración externa— perdía su solicitud de
		 * contacto. Ausente o nulo cuenta como "no autorizó", que es el
		 * estado correcto por defecto.
		 */
		Boolean aceptaComunicacionesComerciales,
		/**
		 * Campo honeypot: oculto para personas en el formulario público, sin
		 * validación propia a propósito. Si llega con contenido, la
		 * solicitud es de un bot y se descarta antes de llegar al caso de
		 * uso (ver {@code SolicitudController.registrar}).
		 */
		String sitioWeb) {

	boolean pareceSpam() {
		return sitioWeb != null && !sitioWeb.isBlank();
	}

}
