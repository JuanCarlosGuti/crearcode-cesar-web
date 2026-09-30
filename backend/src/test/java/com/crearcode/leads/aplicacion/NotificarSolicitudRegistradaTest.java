package com.crearcode.leads.aplicacion;

import java.lang.reflect.Method;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class NotificarSolicitudRegistradaTest {

	private final FakeNotificadorPort notificador = new FakeNotificadorPort();
	private final NotificarSolicitudRegistrada listener = new NotificarSolicitudRegistrada(notificador);

	private static SolicitudRegistrada evento() {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				new DatosDeContacto("Juan Pérez", "Empresa S.A.S.", new Correo("nombre@empresa.com"),
						new Telefono("3001234567")),
				ServicioDeInteres.OTRO, "mensaje",
				new ConsentimientoDatos(true, Instant.now(), "v1"), Instant.now());
		return new SolicitudRegistrada(solicitud);
	}

	@Test
	void mandaElAvisoDeLaSolicitudRecienRegistrada() {
		listener.alRegistrarse(evento());

		assertThat(notificador.notificadas).hasSize(1);
	}

	/**
	 * Best-effort, igual que cuando el envío estaba en línea (HU-18,
	 * caso triste): la solicitud ya está guardada y visible en el panel,
	 * así que un correo perdido no pierde el lead. Y ahora corre en otro
	 * hilo: una excepción que escapara no la vería nadie.
	 */
	@Test
	void unFalloDelCorreoNoSePropaga() {
		notificador.fallarAlNotificar = true;

		assertThatCode(() -> listener.alRegistrarse(evento())).doesNotThrowAnyException();
	}

	/**
	 * Las dos anotaciones son el arreglo entero: sin AFTER_COMMIT se
	 * podría avisar de una solicitud que la transacción revirtió
	 * después, y sin @Async el visitante seguiría esperando al SMTP
	 * (auditoría del 28 sep 2026, P2-2e). Como nada más las verifica en
	 * las pruebas unitarias, se comprueban aquí.
	 */
	@Test
	void escuchaDespuesDelCommitYFueraDelHiloDeLaPeticion() throws NoSuchMethodException {
		Method metodo = NotificarSolicitudRegistrada.class
				.getDeclaredMethod("alRegistrarse", SolicitudRegistrada.class);

		assertThat(metodo.getAnnotation(TransactionalEventListener.class).phase())
				.isEqualTo(TransactionPhase.AFTER_COMMIT);
		assertThat(metodo.getAnnotation(Async.class)).isNotNull();
	}

}
