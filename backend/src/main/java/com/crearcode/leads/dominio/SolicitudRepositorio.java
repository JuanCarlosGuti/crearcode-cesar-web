package com.crearcode.leads.dominio;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida hacia la persistencia. Implementado en
 * {@code infraestructura/persistencia} con JPA en la fase F2.
 */
public interface SolicitudRepositorio {

	void guardar(SolicitudDeContacto solicitud);

	Optional<SolicitudDeContacto> buscarPorId(SolicitudId id);

	List<SolicitudDeContacto> listar();

	List<SolicitudDeContacto> listarPorEstado(EstadoSolicitud estado);

}
