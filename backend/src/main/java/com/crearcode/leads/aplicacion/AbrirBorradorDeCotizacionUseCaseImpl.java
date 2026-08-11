package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.AbrirBorradorDeCotizacionUseCase;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NuevoBorrador;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudRepositorio;

/**
 * Abre el borrador (HU-44). Cuando nace de un lead, los datos del
 * cliente salen de la solicitud: el fundador no vuelve a escribir lo
 * que el visitante ya escribió.
 */
@Service
class AbrirBorradorDeCotizacionUseCaseImpl implements AbrirBorradorDeCotizacionUseCase {

	private final CotizacionRepositorio cotizaciones;
	private final SolicitudRepositorio solicitudes;
	private final Clock reloj;
	private final int diasDeValidezPorDefecto;

	AbrirBorradorDeCotizacionUseCaseImpl(CotizacionRepositorio cotizaciones, SolicitudRepositorio solicitudes,
			Clock reloj,
			@Value("${app.cotizaciones.dias-validez-por-defecto}") int diasDeValidezPorDefecto) {
		this.cotizaciones = cotizaciones;
		this.solicitudes = solicitudes;
		this.reloj = reloj;
		this.diasDeValidezPorDefecto = diasDeValidezPorDefecto;
	}

	@Override
	@Transactional
	public Cotizacion abrir(NuevoBorrador borrador) {
		Instant ahora = Instant.now(reloj);
		int dias = borrador.diasDeValidez() > 0 ? borrador.diasDeValidez() : diasDeValidezPorDefecto;

		DatosDelCliente cliente = borrador.cliente() != null
				? borrador.cliente()
				: clienteDesdeElLead(borrador);

		Cotizacion cotizacion = Cotizacion.abrirBorrador(cliente, borrador.impuesto(), ahora,
				ahora.plus(Duration.ofDays(dias)), borrador.origen(), borrador.notas());
		for (ItemDeCotizacion item : borrador.items()) {
			cotizacion.agregarItem(item);
		}

		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	private DatosDelCliente clienteDesdeElLead(NuevoBorrador borrador) {
		SolicitudDeContacto solicitud = solicitudes.buscarPorId(borrador.origen())
				.orElseThrow(() -> new SolicitudNoEncontradaException(borrador.origen()));
		DatosDeContacto contacto = solicitud.datosDeContacto();
		// La razón social manda sobre el nombre de la persona: es lo que
		// va en el documento. Si el lead no trajo empresa, queda el nombre.
		String nombre = contacto.empresa() != null && !contacto.empresa().isBlank()
				? contacto.empresa()
				: contacto.nombre();
		return new DatosDelCliente(nombre, contacto.correo(), contacto.telefono().valor(), null);
	}

}
