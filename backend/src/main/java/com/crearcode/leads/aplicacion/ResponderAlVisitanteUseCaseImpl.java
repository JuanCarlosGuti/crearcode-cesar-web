package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.ResponderAlVisitanteUseCase;
import com.crearcode.leads.dominio.RespuestaDelAsistente;

/**
 * Aplica los límites de uso ANTES de llamar al proveedor (invariante 2
 * del contexto asistente, ADR-10): global diario (el techo de la capa
 * gratis), por identidad (registrado > anónimo). Contadores en memoria
 * por día — suficiente para una instancia única en v1, igual que el
 * rate limiting por IP; un fallo del proveedor no consume cupo.
 */
@Service
class ResponderAlVisitanteUseCaseImpl implements ResponderAlVisitanteUseCase {

	private final GeneradorDeRespuestas generador;
	private final Clock reloj;
	private final int limiteGlobalDiario;
	private final int limiteDiarioRegistrado;
	private final int limiteDiarioAnonimo;

	private final ContadorDiario contadorGlobal = new ContadorDiario();
	private final ContadorDiario contadorPorIdentidad = new ContadorDiario();

	ResponderAlVisitanteUseCaseImpl(GeneradorDeRespuestas generador, Clock reloj,
			@Value("${app.asistente.limite-global-diario}") int limiteGlobalDiario,
			@Value("${app.asistente.limite-diario-registrado}") int limiteDiarioRegistrado,
			@Value("${app.asistente.limite-diario-anonimo}") int limiteDiarioAnonimo) {
		this.generador = generador;
		this.reloj = reloj;
		this.limiteGlobalDiario = limiteGlobalDiario;
		this.limiteDiarioRegistrado = limiteDiarioRegistrado;
		this.limiteDiarioAnonimo = limiteDiarioAnonimo;
	}

	@Override
	public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion, IdentidadDelVisitante identidad) {
		LocalDate hoy = LocalDate.now(reloj);

		if (contadorGlobal.valor(hoy, "global") >= limiteGlobalDiario) {
			throw new LimiteGlobalAlcanzadoException();
		}
		int limitePersonal = identidad.registrada() ? limiteDiarioRegistrado : limiteDiarioAnonimo;
		if (contadorPorIdentidad.valor(hoy, identidad.clave()) >= limitePersonal) {
			throw new LimiteDeUsoAlcanzadoException(identidad.registrada());
		}

		RespuestaDelAsistente respuesta = generador.responder(conversacion);

		contadorGlobal.incrementar(hoy, "global");
		contadorPorIdentidad.incrementar(hoy, identidad.clave());
		return respuesta;
	}

}
