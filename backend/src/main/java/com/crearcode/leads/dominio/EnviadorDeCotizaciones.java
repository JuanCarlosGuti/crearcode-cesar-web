package com.crearcode.leads.dominio;

/**
 * Puerto de salida para hacerle llegar la cotización al cliente. El
 * adaptador arma el correo, adjunta el PDF y construye el enlace a la
 * cuenta — la aplicación solo entrega la cotización.
 */
public interface EnviadorDeCotizaciones {

	void enviar(Cotizacion cotizacion);

}
