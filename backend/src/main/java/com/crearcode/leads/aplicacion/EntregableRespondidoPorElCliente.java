package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.Proyecto;

/** El cliente aprobó o pidió ajustes desde su cuenta (HU-57): al equipo le toca enterarse. */
record EntregableRespondidoPorElCliente(Proyecto proyecto, EntregableId entregable, boolean aprobado) {
}
