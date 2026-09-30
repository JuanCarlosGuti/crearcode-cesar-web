package com.crearcode.leads.aplicacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.crearcode.leads.dominio.NotificadorDeProyectos;

/**
 * Manda los avisos del proyecto después de que la transacción confirme y
 * fuera del hilo de la petición, con el mismo patrón que
 * {@code NotificarSolicitudRegistrada}: sin AFTER_COMMIT se avisaría de
 * un cambio revertido, y sin @Async el cliente que aprueba esperaría al
 * SMTP. Todo es best-effort: el cambio ya está guardado y se ve en el
 * portal, así que un correo perdido no pierde nada.
 */
@Component
class NotificarCambiosDeProyecto {

	/** Ejecutor de hilos virtuales de {@code ConfiguracionDeTareasAsincronas}. */
	private static final String EJECUTOR_DE_NOTIFICACIONES = "ejecutorDeNotificaciones";

	private static final Logger LOG = LoggerFactory.getLogger(NotificarCambiosDeProyecto.class);

	private final NotificadorDeProyectos notificador;

	NotificarCambiosDeProyecto(NotificadorDeProyectos notificador) {
		this.notificador = notificador;
	}

	@Async(EJECUTOR_DE_NOTIFICACIONES)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void alQuedarListoParaRevisar(EntregableListoParaRevisar evento) {
		intentar(() -> notificador.entregableListoParaRevisar(evento.proyecto(), evento.entregable()),
				"entregable en revisión", evento.proyecto().id().valor());
	}

	@Async(EJECUTOR_DE_NOTIFICACIONES)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void alRegistrarsePago(PagoRegistrado evento) {
		intentar(() -> notificador.pagoRegistrado(evento.proyecto(), evento.pago()), "pago registrado",
				evento.proyecto().id().valor());
	}

	@Async(EJECUTOR_DE_NOTIFICACIONES)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void alResponderElCliente(EntregableRespondidoPorElCliente evento) {
		intentar(() -> notificador.clienteRespondio(evento.proyecto(), evento.entregable(), evento.aprobado()),
				"respuesta del cliente", evento.proyecto().id().valor());
	}

	/** El id del proyecto y no el correo: el log no es sitio para datos personales. */
	private static void intentar(Runnable aviso, String cual, Object proyecto) {
		try {
			aviso.run();
		} catch (RuntimeException excepcion) {
			LOG.warn("No se pudo enviar el aviso de {} del proyecto {}", cual, proyecto, excepcion);
		}
	}

}
