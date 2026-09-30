package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.CerrarGarantiasVencidasUseCase;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;

/**
 * Cierra las garantías vencidas. Cada proyecto se vuelve a leer con
 * bloqueo antes de decidir: si justo a esa hora el equipo le agregó un
 * cambio de alcance —que lo devuelve a ACTIVO—, este proceso lo ve y lo
 * deja en paz en vez de pisarlo.
 */
@Service
class CerrarGarantiasVencidasUseCaseImpl implements CerrarGarantiasVencidasUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(CerrarGarantiasVencidasUseCaseImpl.class);

	private final ProyectoRepositorio proyectos;
	private final Clock reloj;

	CerrarGarantiasVencidasUseCaseImpl(ProyectoRepositorio proyectos, Clock reloj) {
		this.proyectos = proyectos;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public int cerrar() {
		Instant ahora = Instant.now(reloj);
		int cerradas = 0;
		for (Proyecto enGarantia : proyectos.listarPorEstado(EstadoProyecto.EN_GARANTIA)) {
			Proyecto bloqueado = proyectos.buscarPorIdParaModificar(enGarantia.id()).orElse(null);
			if (bloqueado != null && bloqueado.cerrarSiVencioLaGarantia(ahora)) {
				proyectos.guardar(bloqueado);
				cerradas++;
			}
		}
		if (cerradas > 0) {
			LOG.info("Garantías vencidas: {} proyectos cerrados", cerradas);
		}
		return cerradas;
	}

}
