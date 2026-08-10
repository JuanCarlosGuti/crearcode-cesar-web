package com.crearcode.leads.dominio;

/**
 * Imagen generada por IA para el demo de diseño (F10d, HU-42). Viaja
 * como base64 y se muestra SOLO como imagen — nunca HTML ejecutable
 * (regla de la HU).
 */
public record ImagenGenerada(String base64, String tipoMime) {

	public ImagenGenerada {
		base64 = base64 == null ? "" : base64.trim();
		tipoMime = tipoMime == null || tipoMime.isBlank() ? "image/png" : tipoMime.trim();
		if (base64.isEmpty()) {
			throw new AsistenteNoDisponibleException("El proveedor devolvió una imagen vacía");
		}
	}

}
