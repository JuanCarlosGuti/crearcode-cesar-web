package com.crearcode.leads.aplicacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.crearcode.leads.dominio.NotificadorPort;

/**
 * Manda el aviso interno de un lead nuevo, después de que la
 * transacción confirme y fuera del hilo de la petición.
 *
 * <p>
 * Antes esto ocurría en línea dentro del {@code @Transactional} del
 * caso de uso, y eso tenía dos problemas distintos. El de cara al
 * visitante: un SMTP lento mantenía abiertas a la vez la conexión de
 * base de datos y su petición HTTP hasta que venciera el timeout —10 s
 * el de SMTP, 15 s el de Resend—, y él miraba «Enviando…» todo ese rato
 * por un correo que ni siquiera es suyo. El de cara a los datos: si la
 * transacción se revertía después de enviar, el correo ya había salido
 * avisando de una solicitud que no existe.
 *
 * <p>
 * {@code AFTER_COMMIT} resuelve el segundo y {@code @Async} el primero.
 * Sigue siendo best-effort, como antes (HU-18, caso triste): un fallo
 * del correo no puede tumbar nada, porque la solicitud ya está
 * guardada y visible en el panel.
 */
@Component
class NotificarSolicitudRegistrada {

	/**
	 * Ejecutor de hilos virtuales declarado en
	 * {@code ConfiguracionDeTareasAsincronas}. Va como literal porque la
	 * aplicación no puede importar infraestructura (ArchUnit).
	 */
	private static final String EJECUTOR_DE_NOTIFICACIONES = "ejecutorDeNotificaciones";

	private static final Logger LOG = LoggerFactory.getLogger(NotificarSolicitudRegistrada.class);

	private final NotificadorPort notificador;

	NotificarSolicitudRegistrada(NotificadorPort notificador) {
		this.notificador = notificador;
	}

	@Async(EJECUTOR_DE_NOTIFICACIONES)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void alRegistrarse(SolicitudRegistrada evento) {
		try {
			notificador.notificarNuevaSolicitud(evento.solicitud());
		} catch (RuntimeException excepcion) {
			// Best-effort: la solicitud ya está guardada y visible en el
			// panel, así que un correo perdido no pierde el lead. Sin id
			// de solicitud en el mensaje no habría forma de rastrearlo.
			LOG.warn("No se pudo notificar la solicitud {}", evento.solicitud().id(), excepcion);
		}
	}

}
