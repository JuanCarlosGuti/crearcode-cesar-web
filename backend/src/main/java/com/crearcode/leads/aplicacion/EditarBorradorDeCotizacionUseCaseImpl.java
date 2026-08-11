package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.EditarBorradorDeCotizacionUseCase;
import com.crearcode.leads.dominio.ItemDeCotizacion;

@Service
class EditarBorradorDeCotizacionUseCaseImpl implements EditarBorradorDeCotizacionUseCase {

	private final CotizacionRepositorio cotizaciones;
	private final Clock reloj;

	EditarBorradorDeCotizacionUseCaseImpl(CotizacionRepositorio cotizaciones, Clock reloj) {
		this.cotizaciones = cotizaciones;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public Cotizacion editar(CotizacionId id, List<ItemDeCotizacion> items, String notas) {
		Cotizacion cotizacion = buscar(id);

		cotizacion.reemplazarItems(items);
		cotizacion.cambiarNotas(notas);

		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	@Override
	@Transactional
	public void cancelar(CotizacionId id) {
		Cotizacion cotizacion = buscar(id);

		cotizacion.cancelar(Instant.now(reloj));

		cotizaciones.guardar(cotizacion);
	}

	private Cotizacion buscar(CotizacionId id) {
		return cotizaciones.buscarPorId(id).orElseThrow(() -> new CotizacionNoEncontradaException(id));
	}

}
