package com.crearcode.leads.infraestructura.asistente;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.RespuestaDelAsistente;

/**
 * Encadena dos generadores de texto: si el primario falla, responde el
 * respaldo y el visitante ni se entera. Es el mismo patrón que ya
 * protegía al demo de diseño ({@code GeneradorDeImagenesConRespaldo}),
 * traído aquí por lo que pasó el 28 sep 2026: Groq retiró el modelo
 * configurado, el puerto tenía un único montaje y las tres herramientas
 * de texto respondieron 503 durante días — mientras el demo, que sí
 * tenía respaldo, siguió funcionando.
 *
 * <p>
 * Solo atrapa {@link AsistenteNoDisponibleException}: un límite de cupo
 * o unos datos inválidos no son cosa del proveedor y reintentarlos en
 * otro sería gastar dos llamadas para el mismo error.
 */
class GeneradorDeRespuestasConRespaldo implements GeneradorDeRespuestas {

	private static final Logger LOG = LoggerFactory.getLogger(GeneradorDeRespuestasConRespaldo.class);

	private final GeneradorDeRespuestas primario;
	private final GeneradorDeRespuestas respaldo;

	GeneradorDeRespuestasConRespaldo(GeneradorDeRespuestas primario, GeneradorDeRespuestas respaldo) {
		this.primario = primario;
		this.respaldo = respaldo;
	}

	@Override
	public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
		try {
			return primario.responder(conversacion);
		} catch (AsistenteNoDisponibleException fallaDelPrimario) {
			return conRespaldo(fallaDelPrimario, () -> respaldo.responder(conversacion));
		}
	}

	@Override
	public RespuestaDelAsistente responder(String contextoDeSistema, ConversacionDeAsistente conversacion) {
		try {
			return primario.responder(contextoDeSistema, conversacion);
		} catch (AsistenteNoDisponibleException fallaDelPrimario) {
			return conRespaldo(fallaDelPrimario, () -> respaldo.responder(contextoDeSistema, conversacion));
		}
	}

	/**
	 * El WARN es el único aviso de que el primario está roto: sin él, el
	 * respaldo taparía la avería hasta que también se cayera. Solo el
	 * motivo técnico, nunca la conversación del visitante.
	 */
	private RespuestaDelAsistente conRespaldo(AsistenteNoDisponibleException falla,
			java.util.function.Supplier<RespuestaDelAsistente> conElRespaldo) {
		LOG.warn("El proveedor primario de texto falló; responde el respaldo", falla);
		return conElRespaldo.get();
	}

}
