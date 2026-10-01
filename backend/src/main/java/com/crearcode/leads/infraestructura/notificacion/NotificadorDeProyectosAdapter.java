package com.crearcode.leads.infraestructura.notificacion;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.Entregable;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.NotificadorDeProyectos;
import com.crearcode.leads.dominio.Pago;
import com.crearcode.leads.dominio.PagoId;
import com.crearcode.leads.dominio.Proyecto;

/**
 * Los tres correos del proyecto (F12). Como los de cuenta, arma aquí los
 * enlaces con {@code app.frontend-url} (ADR-06): la aplicación no conoce
 * rutas del frontend. Texto plano y sobrio, igual que los demás correos
 * del sitio.
 */
@Component
class NotificadorDeProyectosAdapter implements NotificadorDeProyectos {

	private static final Logger LOG = LoggerFactory.getLogger(NotificadorDeProyectosAdapter.class);
	private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

	/**
	 * Dominios reservados por el RFC 6761/2606: no existen en internet.
	 * Un correo hacia ellos es un rebote seguro, y los rebotes dañan la
	 * reputación del dominio para los correos de verdad. El cliente de
	 * demostración usa uno ({@code .local}).
	 */
	private static final List<String> DOMINIOS_RESERVADOS = List.of(".local", ".test", ".example", ".invalid");
	private static final String FIRMA = "— Crear Code Cesar S.A.S. · Valledupar, Colombia";

	private final TransporteDeCorreo transporte;
	private final String frontendUrl;
	private final String correoDelEquipo;

	NotificadorDeProyectosAdapter(TransporteDeCorreo transporte, @Value("${app.frontend-url}") String frontendUrl,
			@Value("${app.notificaciones.correo-destino}") String correoDelEquipo) {
		this.transporte = transporte;
		this.frontendUrl = frontendUrl;
		this.correoDelEquipo = correoDelEquipo;
	}

	@Override
	public void entregableListoParaRevisar(Proyecto proyecto, EntregableId entregableId) {
		if (esDeDominioReservado(proyecto)) {
			return;
		}
		Entregable entregable = proyecto.entregable(entregableId);
		transporte.enviar(CorreoSaliente.simple(proyecto.correoDelCliente().valor(),
				"Tienes algo para revisar: " + entregable.nombre(), """
						Hola, %s:

						«%s», de tu proyecto «%s», está listo para que lo revises.
						Desde tu cuenta puedes aprobarlo o decirnos qué ajustar:

						%s

						%s
						""".formatted(proyecto.nombreDelCliente(), entregable.nombre(),
						proyecto.descripcion().nombre(), enlaceDelCliente(proyecto), FIRMA)));
	}

	@Override
	public void pagoRegistrado(Proyecto proyecto, PagoId pagoId) {
		if (esDeDominioReservado(proyecto)) {
			return;
		}
		Pago pago = proyecto.pagos().stream()
				.filter(registrado -> registrado.id().equals(pagoId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("El pago no es de este proyecto"));
		Entregable entregable = proyecto.entregable(pago.entregable());
		transporte.enviar(CorreoSaliente.simple(proyecto.correoDelCliente().valor(),
				"Recibimos tu pago de " + pesos(pago.monto()), """
						Hola, %s:

						Registramos tu pago de %s por «%s», de tu proyecto «%s».

						Total pagado del proyecto: %s
						Saldo del proyecto: %s

						Puedes ver el detalle en tu cuenta:

						%s

						Este correo confirma el pago recibido; no es una factura.

						%s
						""".formatted(proyecto.nombreDelCliente(), pesos(pago.monto()), entregable.nombre(),
						proyecto.descripcion().nombre(), pesos(proyecto.totalPagado()), pesos(proyecto.saldo()),
						enlaceDelCliente(proyecto), FIRMA)));
	}

	@Override
	public void clienteRespondio(Proyecto proyecto, EntregableId entregableId, boolean aprobado) {
		Entregable entregable = proyecto.entregable(entregableId);
		String asunto = aprobado
				? "El cliente aprobó «" + entregable.nombre() + "»"
				: "El cliente pidió ajustes en «" + entregable.nombre() + "»";
		String detalle = aprobado
				? "Quedó aprobado y, si se cobra al aprobarse, ya aparece como pago pendiente."
				: "Lo que pidió ajustar:\n\n" + entregable.notaDeAjustes();
		transporte.enviar(CorreoSaliente.simple(correoDelEquipo, asunto, """
				%s, del proyecto «%s»:

				%s

				%s
				""".formatted(proyecto.nombreDelCliente(), proyecto.descripcion().nombre(), detalle,
				frontendUrl + "/admin/proyectos/" + proyecto.id().valor())));
	}

	private static boolean esDeDominioReservado(Proyecto proyecto) {
		String correo = proyecto.correoDelCliente().valor().toLowerCase(Locale.ROOT);
		boolean reservado = DOMINIOS_RESERVADOS.stream().anyMatch(correo::endsWith);
		if (reservado) {
			LOG.info("No se envía el aviso del proyecto {}: el correo del cliente es de un dominio reservado",
					proyecto.id().valor());
		}
		return reservado;
	}

	private String enlaceDelCliente(Proyecto proyecto) {
		return frontendUrl + "/mi-cuenta/proyectos/" + proyecto.id().valor();
	}

	/** "$ 590.000": pesos colombianos sin decimales, como se escriben aquí. */
	private static String pesos(Dinero dinero) {
		NumberFormat formato = NumberFormat.getCurrencyInstance(COLOMBIA);
		formato.setMaximumFractionDigits(0);
		return formato.format(dinero.monto());
	}

}
