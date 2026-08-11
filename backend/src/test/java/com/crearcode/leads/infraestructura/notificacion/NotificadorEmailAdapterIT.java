package com.crearcode.leads.infraestructura.notificacion;

import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.NotificadorPort;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.Telefono;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificadorEmailAdapterIT {

	// Arranca en un bloque estático (no via @RegisterExtension) para
	// garantizar que el puerto ya existe cuando @DynamicPropertySource lo
	// lee: ese método corre antes que los callbacks de ciclo de vida de
	// extensiones de JUnit.
	private static final GreenMail GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP);

	static {
		GREEN_MAIL.start();
	}

	@DynamicPropertySource
	static void propiedadesDeCorreo(DynamicPropertyRegistry registry) {
		registry.add("spring.mail.host", () -> "localhost");
		registry.add("spring.mail.port", () -> GREEN_MAIL.getSmtp().getPort());
		registry.add("app.notificaciones.correo-destino", () -> "fundador@crearcodecesar-test.com");
	}

	@AfterAll
	static void detenerServidorDeCorreo() {
		GREEN_MAIL.stop();
	}

	// GreenMail acumula los mensajes de toda la clase: sin vaciar la
	// bandeja, un test que cuenta correos depende de cuántos corrieron
	// antes (y del orden, que JUnit no garantiza).
	@BeforeEach
	void vaciarBandeja() throws Exception {
		GREEN_MAIL.purgeEmailFromAllMailboxes();
	}

	@Autowired
	private NotificadorPort notificador;

	@Test
	void envioUnCorreoConLosDatosClaveDeLaSolicitud() throws Exception {
		DatosDeContacto datos = new DatosDeContacto(
				"Juan Pérez", "Empresa S.A.S.", new Correo("nombre@empresa.com"), new Telefono("3001234567"));
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(datos, ServicioDeInteres.IA_Y_AUTOMATIZACION,
				"Quiero automatizar mi negocio", new ConsentimientoDatos(true, Instant.now(), "v1"), Instant.now());

		notificador.notificarNuevaSolicitud(solicitud);

		MimeMessage[] recibidos = GREEN_MAIL.getReceivedMessages();
		assertThat(recibidos).hasSize(1);
		assertThat(recibidos[0].getAllRecipients()[0].toString()).isEqualTo("fundador@crearcodecesar-test.com");

		String cuerpo = (String) recibidos[0].getContent();
		assertThat(cuerpo).contains("Juan Pérez", "nombre@empresa.com", "3001234567",
				"Quiero automatizar mi negocio");
	}

	/**
	 * El remitente se fija explícito: si se dejara al usuario SMTP, con un
	 * servicio transaccional saldría una dirección que no pertenece al
	 * dominio verificado y el envío se rechazaría con 422.
	 */
	@Test
	void saleDelRemitenteDelDominioPropioYSeRespondeAlBuzonDeContacto() throws Exception {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				new DatosDeContacto("Ana", null, new Correo("ana@empresa.com"), new Telefono("3001234567")),
				ServicioDeInteres.OTRO, "Hola",
				new ConsentimientoDatos(true, Instant.now(), "v1"), Instant.now());

		notificador.notificarNuevaSolicitud(solicitud);

		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[GREEN_MAIL.getReceivedMessages().length - 1];
		assertThat(recibido.getFrom()[0].toString())
				.isEqualTo("Crear Code Cesar <contacto@crearcodecesar.com>");
		assertThat(recibido.getReplyTo()[0].toString()).isEqualTo("contacto@crearcodecesar.com");
	}

}
