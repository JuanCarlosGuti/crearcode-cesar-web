package com.crearcode.leads.dominio;

/**
 * Puerto de salida hacia la notificación. Implementado en
 * {@code infraestructura/notificacion} con correo en la fase F2.
 */
public interface NotificadorPort {

	void notificarNuevaSolicitud(SolicitudDeContacto solicitud);

}
