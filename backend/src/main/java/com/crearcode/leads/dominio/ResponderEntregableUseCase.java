package com.crearcode.leads.dominio;

/**
 * El cliente aprueba o pide ajustes de un entregable desde su cuenta
 * (HU-57, decisión 27). El correo sale del token, nunca de la petición.
 */
public interface ResponderEntregableUseCase {

	Proyecto aprobar(ProyectoId id, EntregableId entregable, Correo cliente);

	Proyecto pedirAjustes(ProyectoId id, EntregableId entregable, Correo cliente, String nota);

}
