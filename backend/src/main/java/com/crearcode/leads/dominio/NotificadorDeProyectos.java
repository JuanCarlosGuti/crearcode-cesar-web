package com.crearcode.leads.dominio;

/**
 * Puerto de salida de los avisos por correo del proyecto (F12, HU-52 y
 * HU-57). Solo estos tres: el cliente no recibe un correo por cada cambio
 * menor.
 */
public interface NotificadorDeProyectos {

	/** Al cliente: un entregable está listo para que lo revise. */
	void entregableListoParaRevisar(Proyecto proyecto, EntregableId entregable);

	/** Al cliente: se registró su pago, con el monto y cómo queda el saldo. */
	void pagoRegistrado(Proyecto proyecto, PagoId pago);

	/** Al equipo: el cliente aprobó o pidió ajustes desde su cuenta. */
	void clienteRespondio(Proyecto proyecto, EntregableId entregable, boolean aprobado);

}
