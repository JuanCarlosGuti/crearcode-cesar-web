package com.crearcode.leads.dominio;

public interface ResponderCotizacionUseCase {

	Cotizacion aceptar(CotizacionId id, Correo correoDelCliente);

	Cotizacion rechazar(CotizacionId id, Correo correoDelCliente);

}
