package com.crearcode.leads.dominio;

import java.util.List;

public interface EditarBorradorDeCotizacionUseCase {

	/**
	 * Reemplaza los ítems y las notas del borrador — el formulario del
	 * panel edita la cotización completa y guarda, no ítem por ítem.
	 */
	Cotizacion editar(CotizacionId id, List<ItemDeCotizacion> items, String notas);

	void cancelar(CotizacionId id);

}
