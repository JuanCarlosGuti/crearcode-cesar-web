package com.crearcode.leads.infraestructura.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.GeneradorDeBocetos;
import com.crearcode.leads.dominio.SolicitudDeDemo;

/**
 * Claude primero y Groq si Claude falla (decisión 33). La cuenta de
 * Anthropic no tiene tarjeta: cuando se acaban los créditos las
 * llamadas fallan en vez de cobrar, y el respaldo es la diferencia
 * entre "el demo no sirve" y "el boceto salió algo más sencillo".
 */
class GeneradorDeBocetosConRespaldo implements GeneradorDeBocetos {

	private static final Logger LOG = LoggerFactory.getLogger(GeneradorDeBocetosConRespaldo.class);

	private final GeneradorDeBocetos primario;
	private final GeneradorDeBocetos respaldo;

	GeneradorDeBocetosConRespaldo(GeneradorDeBocetos primario, GeneradorDeBocetos respaldo) {
		this.primario = primario;
		this.respaldo = respaldo;
	}

	@Override
	public BocetoDeSolucion generar(SolicitudDeDemo solicitud) {
		try {
			return primario.generar(solicitud);
		} catch (AsistenteNoDisponibleException fallaDelPrimario) {
			// Sin datos del visitante en el log (solo el motivo técnico).
			LOG.warn("El proveedor primario de bocetos falló; se usa el respaldo", fallaDelPrimario);
			return respaldo.generar(solicitud);
		}
	}

}
