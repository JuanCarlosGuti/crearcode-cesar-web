package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.PagoId;
import com.crearcode.leads.dominio.Proyecto;

/** Se registró un pago del cliente (HU-52): le llega la confirmación con su saldo. */
record PagoRegistrado(Proyecto proyecto, PagoId pago) {
}
