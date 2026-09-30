package com.crearcode.leads.infraestructura.notificacion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NotificadorDeProyectos;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.NuevoPago;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.PagoId;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import static org.assertj.core.api.Assertions.assertThat;

/** HU-52 y HU-57: los tres correos del proyecto, contra un SMTP de prueba. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificadorDeProyectosAdapterIT {

	private static final GreenMail GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP);

	static {
		GREEN_MAIL.start();
	}

	@DynamicPropertySource
	static void propiedadesDeCorreo(DynamicPropertyRegistry registry) {
		registry.add("spring.mail.host", () -> "localhost");
		registry.add("spring.mail.port", () -> GREEN_MAIL.getSmtp().getPort());
		registry.add("app.frontend-url", () -> "https://sitio-de-prueba.example");
		registry.add("app.notificaciones.correo-destino", () -> "equipo@correo-de-prueba.com");
	}

	@AfterAll
	static void detenerServidorDeCorreo() {
		GREEN_MAIL.stop();
	}

	@Autowired
	private NotificadorDeProyectos notificador;

	private final Proyecto proyecto = Proyecto.enBlanco(new Correo("cliente@correo-de-prueba.com"), "Café Valle",
			new DescripcionDelProyecto("Tienda de Café Valle", null, LocalDate.of(2026, 9, 1), null),
			new Porcentaje(19),
			List.of(new NuevaFase(new DatosDeFase("Fase 1", null, null, null), List.of(
					new DatosDeEntregable("Diseño de la tienda", null, Dinero.de(1_000_000),
							MomentoDeCobro.AL_INICIAR)))),
			Instant.parse("2026-09-01T15:00:00Z"));

	private final EntregableId diseno = proyecto.entregables().getFirst().id();

	private MimeMessage ultimo() {
		MimeMessage[] recibidos = GREEN_MAIL.getReceivedMessages();
		return recibidos[recibidos.length - 1];
	}

	@Test
	void alClienteLeLlegaQueTieneAlgoParaRevisarConElEnlaceDirecto() throws Exception {
		notificador.entregableListoParaRevisar(proyecto, diseno);

		MimeMessage recibido = ultimo();
		assertThat(recibido.getAllRecipients()[0].toString()).isEqualTo("cliente@correo-de-prueba.com");
		assertThat(recibido.getSubject()).contains("Diseño de la tienda");
		assertThat((String) recibido.getContent())
				.contains("Tienda de Café Valle")
				.contains("https://sitio-de-prueba.example/mi-cuenta/proyectos/" + proyecto.id().valor());
	}

	@Test
	void alClienteLeLlegaLaConfirmacionDelPagoConSuSaldoYQueNoEsFactura() throws Exception {
		PagoId pago = proyecto.registrarPago(new NuevoPago(diseno, Dinero.de(590_000), LocalDate.of(2026, 9, 2),
				MedioDePago.NEQUI_DAVIPLATA, OrigenDePago.MANUAL, null, new Correo("admin@crearcodecesar.com")),
				Instant.parse("2026-09-02T15:00:00Z"));

		notificador.pagoRegistrado(proyecto, pago);

		MimeMessage recibido = ultimo();
		assertThat(recibido.getAllRecipients()[0].toString()).isEqualTo("cliente@correo-de-prueba.com");
		assertThat((String) recibido.getContent())
				.contains("590.000")
				.contains("600.000")
				.contains("no es una factura");
	}

	@Test
	void alEquipoLeLlegaLaRespuestaDelClienteConLaNotaDeAjustes() throws Exception {
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_CURSO, null, Instant.now());
		proyecto.cambiarEstadoDeEntregable(diseno, EstadoEntregable.EN_REVISION, null, Instant.now());
		proyecto.pedirAjustesComoCliente(diseno, new Correo("cliente@correo-de-prueba.com"), "El logo más grande",
				Instant.now());

		notificador.clienteRespondio(proyecto, diseno, false);

		MimeMessage recibido = ultimo();
		assertThat(recibido.getAllRecipients()[0].toString()).isEqualTo("equipo@correo-de-prueba.com");
		assertThat(recibido.getSubject()).contains("ajustes").contains("Diseño de la tienda");
		assertThat((String) recibido.getContent())
				.contains("El logo más grande")
				.contains("https://sitio-de-prueba.example/admin/proyectos/" + proyecto.id().valor());
	}

}
