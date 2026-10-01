package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.Proyecto;

/**
 * Un entregable pasó a EN_REVISION: el cliente tiene algo que mirar
 * (HU-52). Lo consume el aviso por correo después de confirmar la
 * transacción, con el mismo patrón que {@code SolicitudRegistrada}.
 */
record EntregableListoParaRevisar(Proyecto proyecto, EntregableId entregable) {
}
