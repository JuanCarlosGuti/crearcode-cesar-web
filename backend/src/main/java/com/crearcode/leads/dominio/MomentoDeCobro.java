package com.crearcode.leads.dominio;

/**
 * Cuándo se puede cobrar un entregable (decisión 21 de docs/10): el
 * primero al iniciar el proyecto —es el anticipo— y los demás al
 * aprobarse. Va en cada entregable y no fijo en el código porque la
 * misma decisión admite "salvo acuerdo distinto en la cotización".
 */
public enum MomentoDeCobro {
	AL_INICIAR,
	AL_APROBAR
}
