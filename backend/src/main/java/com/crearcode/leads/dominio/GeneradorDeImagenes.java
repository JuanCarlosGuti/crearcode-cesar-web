package com.crearcode.leads.dominio;

/**
 * Puerto de salida hacia el proveedor de imágenes del demo de diseño
 * (F10d, decisión 8 de docs/10: Gemini Flash Image en capa gratis).
 */
public interface GeneradorDeImagenes {

	/**
	 * @throws AsistenteNoDisponibleException si el proveedor falla — la
	 *                                        aplicación la traduce al
	 *                                        estado amable, nunca llega
	 *                                        como error técnico.
	 */
	ImagenGenerada generar(String descripcion);

}
