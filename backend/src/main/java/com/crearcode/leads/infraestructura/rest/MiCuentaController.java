package com.crearcode.leads.infraestructura.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.dominio.EliminarMiCuentaUseCase;

/**
 * La cuenta del cliente sobre sí misma. Hoy solo eliminarla: derecho de
 * supresión de la Ley 1581 que la política de datos v2 promete y que
 * hasta ahora solo se podía ejercer escribiendo un correo y esperando
 * a que alguien lo atendiera.
 */
@RestController
@RequestMapping("/api/mi-cuenta")
class MiCuentaController {

	private final EliminarMiCuentaUseCase eliminarMiCuenta;

	MiCuentaController(EliminarMiCuentaUseCase eliminarMiCuenta) {
		this.eliminarMiCuenta = eliminarMiCuenta;
	}

	/**
	 * El correo sale del token, nunca del cuerpo: así nadie puede borrar
	 * la cuenta de otro. 204 y no 200 — no queda nada que devolver.
	 */
	@DeleteMapping
	ResponseEntity<Void> eliminar(Authentication autenticacion) {
		eliminarMiCuenta.eliminar(autenticacion.getName());
		return ResponseEntity.noContent().build();
	}

}
