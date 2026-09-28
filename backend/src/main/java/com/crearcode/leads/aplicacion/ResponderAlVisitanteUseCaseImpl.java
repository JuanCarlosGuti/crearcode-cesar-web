package com.crearcode.leads.aplicacion;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.ResponderAlVisitanteUseCase;
import com.crearcode.leads.dominio.RespuestaDelAsistente;

/**
 * Chat del asistente (F9). Los límites de uso (invariante 2 del
 * contexto asistente, ADR-10) los aplica {@link CupoDeIa}, que es la
 * misma regla para las cuatro herramientas: reserva antes de llamar al
 * proveedor y devuelve el cupo si el trabajo no llegó a hacerse.
 */
@Service
class ResponderAlVisitanteUseCaseImpl implements ResponderAlVisitanteUseCase {

	private final GeneradorDeRespuestas generador;
	private final CupoDeIa cupo;

	ResponderAlVisitanteUseCaseImpl(GeneradorDeRespuestas generador, Clock reloj,
			@Value("${app.asistente.limite-global-diario}") int limiteGlobalDiario,
			@Value("${app.asistente.limite-diario-registrado}") int limiteDiarioRegistrado,
			@Value("${app.asistente.limite-diario-anonimo}") int limiteDiarioAnonimo,
			@Value("${app.asistente.limite-diario-por-red}") int limiteDiarioPorRed) {
		this.generador = generador;
		this.cupo = new CupoDeIa(reloj, limiteGlobalDiario, limiteDiarioRegistrado, limiteDiarioAnonimo,
				limiteDiarioPorRed);
	}

	@Override
	public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion, IdentidadDelVisitante identidad) {
		return cupo.ejecutar(identidad, () -> generador.responder(conversacion));
	}

}
