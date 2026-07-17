package com.crearcode.leads.dominio;

/**
 * Puerto de entrada: cambia el estado de una solicitud existente,
 * validando la transición según {@link EstadoSolicitud}.
 */
public interface CambiarEstadoSolicitudUseCase {

	void cambiarEstado(SolicitudId id, EstadoSolicitud nuevoEstado);

}
