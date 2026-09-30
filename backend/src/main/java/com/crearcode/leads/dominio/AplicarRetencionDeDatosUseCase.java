package com.crearcode.leads.dominio;

/**
 * Borra lo que ya cumplió su plazo de conservación (política de datos
 * v2, §9). Prometer un plazo y no aplicarlo es peor que no prometerlo:
 * el dato sigue ahí y además hay un documento público diciendo que no.
 *
 * @return cuántos registros se borraron
 */
public interface AplicarRetencionDeDatosUseCase {

	int aplicar();

}
