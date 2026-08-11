package com.crearcode.leads.dominio;

/**
 * Puerto de salida para convertir una cotización en el documento que
 * recibe el cliente. El dominio no sabe que hoy es un PDF hecho con
 * OpenPDF: cambiar de librería —o pasar a un servicio externo— es
 * cambiar el adaptador.
 */
public interface GeneradorDeDocumento {

	/** El PDF ya renderizado, listo para descargar o adjuntar. */
	byte[] generar(Cotizacion cotizacion);

}
