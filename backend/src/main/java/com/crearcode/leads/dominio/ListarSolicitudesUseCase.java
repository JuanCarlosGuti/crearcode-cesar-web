package com.crearcode.leads.dominio;

import java.util.List;

/**
 * Puerto de entrada: lista solicitudes para el panel admin, con filtro
 * opcional por estado.
 */
public interface ListarSolicitudesUseCase {

	List<SolicitudDeContacto> listar();

	List<SolicitudDeContacto> listarPorEstado(EstadoSolicitud estado);

}
