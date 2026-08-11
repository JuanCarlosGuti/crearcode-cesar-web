package com.crearcode.leads.infraestructura.notificacion;

import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EnviadorDeCotizaciones;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EnviadorDeCotizacionesAdapterIT {

	private static final GreenMail GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP);

	static {
		GREEN_MAIL.start();
	}

	@DynamicPropertySource
	static void propiedadesDeCorreo(DynamicPropertyRegistry registry) {
		registry.add("spring.mail.host", () -> "localhost");
		registry.add("spring.mail.port", () -> GREEN_MAIL.getSmtp().getPort());
		registry.add("app.frontend-url", () -> "https://sitio-de-prueba.example");
	}

	@AfterAll
	static void detenerServidorDeCorreo() {
		GREEN_MAIL.stop();
	}

	@Autowired
	private EnviadorDeCotizaciones enviador;

	private static Cotizacion cotizacionEnviada() {
		Instant ahora = Instant.parse("2026-08-11T15:00:00Z");
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@correo-de-prueba.com"), null, null),
				new Porcentaje(19), ahora, ahora.plusSeconds(15 * 24 * 3600L), null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Desarrollo", 1, Dinero.de(5_000_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 3), ahora);
		return cotizacion;
	}

	@Test
	void elCorreoVaAlClienteConElNumeroYElEnlaceASuCuenta() throws Exception {
		enviador.enviar(cotizacionEnviada());

		assertThat(GREEN_MAIL.waitForIncomingEmail(5000, 1)).isTrue();
		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[0];

		assertThat(recibido.getAllRecipients()[0].toString()).isEqualTo("cliente@correo-de-prueba.com");
		assertThat(recibido.getSubject()).contains("COT-2026-0003");

		String cuerpo = textoDe(recibido);
		assertThat(cuerpo).contains("https://sitio-de-prueba.example/mi-cuenta/cotizaciones");
		assertThat(cuerpo).contains("Panaderia El Trigal");
	}

	// Resend y compañía rechazan con 422 cualquier remitente fuera del
	// dominio verificado, y el usuario SMTP de esos servicios ni siquiera
	// es una direccion: el From va explicito.
	@Test
	void saleDelRemitenteDelDominioPropioYSeRespondeAlBuzonDeContacto() throws Exception {
		enviador.enviar(cotizacionEnviada());

		assertThat(GREEN_MAIL.waitForIncomingEmail(5000, 1)).isTrue();
		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[GREEN_MAIL.getReceivedMessages().length - 1];

		assertThat(recibido.getFrom()[0].toString())
				.isEqualTo("Crear Code Cesar <contacto@crearcodecesar.com>");
		assertThat(recibido.getReplyTo()[0].toString()).isEqualTo("contacto@crearcodecesar.com");
	}

	// Lo que distingue a este correo del resto del sitio: lleva adjunto.
	@Test
	void elCorreoLlevaElPdfAdjuntoConElNumeroComoNombre() throws Exception {
		enviador.enviar(cotizacionEnviada());

		assertThat(GREEN_MAIL.waitForIncomingEmail(5000, 1)).isTrue();
		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[GREEN_MAIL.getReceivedMessages().length - 1];

		Multipart partes = (Multipart) recibido.getContent();
		Part adjunto = null;
		for (int i = 0; i < partes.getCount(); i++) {
			Part parte = partes.getBodyPart(i);
			if (Part.ATTACHMENT.equalsIgnoreCase(parte.getDisposition())) {
				adjunto = parte;
			}
		}

		assertThat(adjunto).isNotNull();
		assertThat(adjunto.getFileName()).isEqualTo("COT-2026-0003.pdf");
		assertThat(adjunto.getContentType()).contains("application/pdf");
		assertThat(adjunto.getInputStream().readAllBytes()).isNotEmpty();
	}

	/**
	 * MimeMessageHelper anida multiparts (mixed → alternative), así que
	 * el texto hay que buscarlo en profundidad, no solo en el primer
	 * nivel.
	 */
	private static String textoDe(Object contenido) throws Exception {
		if (contenido instanceof String texto) {
			return texto;
		}
		if (contenido instanceof MimeMessage mensaje) {
			return textoDe(mensaje.getContent());
		}
		if (contenido instanceof Multipart partes) {
			StringBuilder texto = new StringBuilder();
			for (int i = 0; i < partes.getCount(); i++) {
				Part parte = partes.getBodyPart(i);
				if (!Part.ATTACHMENT.equalsIgnoreCase(parte.getDisposition())) {
					texto.append(textoDe(parte.getContent()));
				}
			}
			return texto.toString();
		}
		return "";
	}

}
