package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: registra una nueva solicitud de contacto. La
 * implementación (fase F2, en {@code aplicacion/}) persiste la solicitud
 * y dispara la notificación al fundador.
 */
public interface RegistrarSolicitudUseCase {

	SolicitudId registrar(DatosDeContacto datosDeContacto, ServicioDeInteres servicioDeInteres,
			String mensaje, ConsentimientoDatos consentimiento);

}
