package com.crearcode.leads.infraestructura.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.ImagenGenerada;

/**
 * Encadena dos proveedores de imágenes: si el primario falla, responde
 * el respaldo y el visitante ni se entera. Ninguno de los dos tiene SLA
 * en su capa gratis, así que el respaldo es la diferencia entre "el
 * demo no sirve" y "el demo tardó un poco más".
 */
class GeneradorDeImagenesConRespaldo implements GeneradorDeImagenes {

	private static final Logger LOG = LoggerFactory.getLogger(GeneradorDeImagenesConRespaldo.class);

	private final GeneradorDeImagenes primario;
	private final GeneradorDeImagenes respaldo;

	GeneradorDeImagenesConRespaldo(GeneradorDeImagenes primario, GeneradorDeImagenes respaldo) {
		this.primario = primario;
		this.respaldo = respaldo;
	}

	@Override
	public ImagenGenerada generar(String descripcion) {
		try {
			return primario.generar(descripcion);
		} catch (AsistenteNoDisponibleException fallaDelPrimario) {
			// Sin datos del visitante en el log (solo el motivo técnico).
			LOG.warn("El proveedor primario de imágenes falló; se usa el respaldo", fallaDelPrimario);
			return respaldo.generar(descripcion);
		}
	}

}
