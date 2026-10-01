package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.CrearProyectoUseCase;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;

/**
 * Crea el proyecto (HU-53). Desde una cotización aceptada, el equipo no
 * vuelve a escribir lo que ya se acordó: los ítems se proponen como
 * entregables y el dominio comprueba que el plan confirmado sume lo que
 * el cliente aceptó.
 */
@Service
class CrearProyectoUseCaseImpl implements CrearProyectoUseCase {

	private static final int LONGITUD_MAXIMA_DEL_NOMBRE = 120;
	private static final String NOMBRE_DE_LA_FASE_PROPUESTA = "Fase 1";

	private final ProyectoRepositorio proyectos;
	private final CotizacionRepositorio cotizaciones;
	private final Clock reloj;

	CrearProyectoUseCaseImpl(ProyectoRepositorio proyectos, CotizacionRepositorio cotizaciones, Clock reloj) {
		this.proyectos = proyectos;
		this.cotizaciones = cotizaciones;
		this.reloj = reloj;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NuevaFase> proponerPlan(CotizacionId id) {
		Cotizacion cotizacion = cotizacion(id);
		List<DatosDeEntregable> entregables = new ArrayList<>();
		for (ItemDeCotizacion item : cotizacion.items()) {
			// Decisión 21 como punto de partida: el primero es el anticipo.
			MomentoDeCobro cobro = entregables.isEmpty() ? MomentoDeCobro.AL_INICIAR : MomentoDeCobro.AL_APROBAR;
			entregables.add(new DatosDeEntregable(recortado(item.descripcion()), null, item.subtotal(), cobro));
		}
		return List.of(new NuevaFase(new DatosDeFase(NOMBRE_DE_LA_FASE_PROPUESTA, null, null, null), entregables));
	}

	@Override
	@Transactional
	public Proyecto desdeCotizacion(CotizacionId id, DescripcionDelProyecto descripcion, List<NuevaFase> plan) {
		Cotizacion cotizacion = cotizacion(id);
		if (proyectos.buscarPorCotizacion(id).isPresent()) {
			throw new ProyectoYaExisteParaLaCotizacionException(id);
		}
		Proyecto proyecto = Proyecto.desdeCotizacion(cotizacion, descripcion, plan, Instant.now(reloj));
		proyectos.guardar(proyecto);
		return proyecto;
	}

	@Override
	@Transactional
	public Proyecto enBlanco(Correo correoDelCliente, String nombreDelCliente, DescripcionDelProyecto descripcion,
			Porcentaje impuesto, List<NuevaFase> plan) {
		Proyecto proyecto = Proyecto.enBlanco(correoDelCliente, nombreDelCliente, descripcion, impuesto, plan,
				Instant.now(reloj));
		proyectos.guardar(proyecto);
		return proyecto;
	}

	private Cotizacion cotizacion(CotizacionId id) {
		return cotizaciones.buscarPorId(id).orElseThrow(() -> new CotizacionNoEncontradaException(id));
	}

	/** Un ítem admite 200 caracteres y un entregable 120. */
	private static String recortado(String descripcion) {
		return descripcion.length() <= LONGITUD_MAXIMA_DEL_NOMBRE
				? descripcion
				: descripcion.substring(0, LONGITUD_MAXIMA_DEL_NOMBRE);
	}

}
