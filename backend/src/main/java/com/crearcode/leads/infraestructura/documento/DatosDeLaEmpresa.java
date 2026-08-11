package com.crearcode.leads.infraestructura.documento;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Encabezado del documento. El NIT y la dirección fiscal siguen
 * PENDIENTES de que el usuario los confirme (ver
 * docs/05-backlog-issues.md §F11): si llegan vacíos, el PDF
 * simplemente no imprime esa línea, en vez de mostrar un dato inventado.
 */
@ConfigurationProperties(prefix = "app.empresa")
public record DatosDeLaEmpresa(String razonSocial, String nit, String direccion, String ciudad,
		String telefono, String correo, String sitioWeb) {

	public boolean tieneNit() {
		return nit != null && !nit.isBlank();
	}

	public boolean tieneDireccion() {
		return direccion != null && !direccion.isBlank();
	}

}
