package com.crearcode.leads.dominio;

/**
 * El demo de diseño es solo para cuentas registradas (HU-42, decisión
 * de docs/10: control de costo del proveedor de imágenes y gancho del
 * registro). Defensa en profundidad: la seguridad ya exige token, pero
 * la regla vive también en el dominio.
 */
public class DemoSoloParaRegistradosException extends RuntimeException {

	public DemoSoloParaRegistradosException() {
		super("El demo de diseño es para cuentas registradas");
	}

}
