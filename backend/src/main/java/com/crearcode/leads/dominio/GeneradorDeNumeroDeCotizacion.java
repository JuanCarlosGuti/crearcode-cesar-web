package com.crearcode.leads.dominio;

/**
 * Entrega el siguiente consecutivo del año. La implementación debe ser
 * atómica: dos envíos simultáneos no pueden recibir el mismo número ni
 * saltarse uno (invariante 5 del contexto).
 */
public interface GeneradorDeNumeroDeCotizacion {

	NumeroDeCotizacion siguiente(int anio);

}
