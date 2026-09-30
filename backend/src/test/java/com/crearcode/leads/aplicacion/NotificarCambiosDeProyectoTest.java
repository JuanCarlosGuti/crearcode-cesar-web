package com.crearcode.leads.aplicacion;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.NotificadorDeProyectos;
import com.crearcode.leads.dominio.PagoId;
import com.crearcode.leads.dominio.Proyecto;

import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.idDe;
import static com.crearcode.leads.aplicacion.CasosDeProyectoDePrueba.proyectoConDosEntregables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/** HU-52 y HU-57: los tres avisos del proyecto, best-effort y después del commit. */
class NotificarCambiosDeProyectoTest {

	/** Registra lo que se le pide y, si se le indica, falla como un SMTP caído. */
	private static final class NotificadorFalso implements NotificadorDeProyectos {

		private final List<String> avisos = new ArrayList<>();
		private boolean fallar;

		@Override
		public void entregableListoParaRevisar(Proyecto proyecto, EntregableId entregable) {
			registrar("revision " + proyecto.entregable(entregable).nombre());
		}

		@Override
		public void pagoRegistrado(Proyecto proyecto, PagoId pago) {
			registrar("pago " + pago.valor());
		}

		@Override
		public void clienteRespondio(Proyecto proyecto, EntregableId entregable, boolean aprobado) {
			registrar((aprobado ? "aprobo " : "ajustes ") + proyecto.entregable(entregable).nombre());
		}

		private void registrar(String aviso) {
			if (fallar) {
				throw new IllegalStateException("SMTP caído");
			}
			avisos.add(aviso);
		}

	}

	private final NotificadorFalso notificador = new NotificadorFalso();
	private final NotificarCambiosDeProyecto listener = new NotificarCambiosDeProyecto(notificador);
	private final Proyecto proyecto = proyectoConDosEntregables();
	private final EntregableId diseno = idDe(proyecto, "Diseño");

	@Test
	void cadaEventoLlegaASuAviso() {
		PagoId pago = PagoId.nuevo();

		listener.alQuedarListoParaRevisar(new EntregableListoParaRevisar(proyecto, diseno));
		listener.alRegistrarsePago(new PagoRegistrado(proyecto, pago));
		listener.alResponderElCliente(new EntregableRespondidoPorElCliente(proyecto, diseno, true));
		listener.alResponderElCliente(new EntregableRespondidoPorElCliente(proyecto, diseno, false));

		assertThat(notificador.avisos).containsExactly("revision Diseño", "pago " + pago.valor(), "aprobo Diseño",
				"ajustes Diseño");
	}

	/** El cambio ya quedó guardado: un correo perdido no puede deshacerlo ni romper nada. */
	@Test
	void unFalloDelCorreoNoSePropaga() {
		notificador.fallar = true;

		assertThatCode(() -> {
			listener.alQuedarListoParaRevisar(new EntregableListoParaRevisar(proyecto, diseno));
			listener.alRegistrarsePago(new PagoRegistrado(proyecto, PagoId.nuevo()));
			listener.alResponderElCliente(new EntregableRespondidoPorElCliente(proyecto, diseno, true));
		}).doesNotThrowAnyException();
	}

	/**
	 * Mismo arreglo que las solicitudes (auditoría P2-2e): sin
	 * AFTER_COMMIT se avisaría de un cambio que la transacción revirtió,
	 * y sin @Async la petición esperaría al SMTP.
	 */
	@Test
	void losTresEscuchanDespuesDelCommitYFueraDelHiloDeLaPeticion() throws NoSuchMethodException {
		for (Method metodo : new Method[] {
				NotificarCambiosDeProyecto.class.getDeclaredMethod("alQuedarListoParaRevisar",
						EntregableListoParaRevisar.class),
				NotificarCambiosDeProyecto.class.getDeclaredMethod("alRegistrarsePago", PagoRegistrado.class),
				NotificarCambiosDeProyecto.class.getDeclaredMethod("alResponderElCliente",
						EntregableRespondidoPorElCliente.class) }) {
			assertThat(metodo.getAnnotation(TransactionalEventListener.class).phase())
					.as(metodo.getName()).isEqualTo(TransactionPhase.AFTER_COMMIT);
			assertThat(metodo.getAnnotation(Async.class)).as(metodo.getName()).isNotNull();
		}
	}

}
