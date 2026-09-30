package com.crearcode.leads.dominio;

/**
 * Cierra los proyectos cuya garantía de 60 días ya venció (decisión 23).
 * Lo dispara un programador diario; devuelve cuántos cerró.
 */
public interface CerrarGarantiasVencidasUseCase {

	int cerrar();

}
