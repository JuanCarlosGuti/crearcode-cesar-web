package com.crearcode.leads.dominio;

/**
 * Puerto de salida del demo de diseño (decisión 33): arma el boceto de
 * la solución a partir de lo que el visitante contó de su negocio. Lo
 * implementan Claude (primario) y Groq (respaldo), con el mismo
 * esquema.
 *
 * <p>
 * Si el proveedor falla, se niega o devuelve un boceto que no cumple
 * las reglas, lanza {@link AsistenteNoDisponibleException}.
 */
public interface GeneradorDeBocetos {

	BocetoDeSolucion generar(SolicitudDeDemo solicitud);

}
