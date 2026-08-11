package com.crearcode.leads.infraestructura.notificacion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EnviadorDeCorreosDeCuenta;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EnviadorDeCorreosDeCuentaAdapterIT {

	// Mismo patrón de NotificadorEmailAdapterIT: bloque estático para que
	// el puerto exista cuando @DynamicPropertySource lo lee.
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
	private EnviadorDeCorreosDeCuenta enviador;

	@Test
	void elCorreoDeVerificacionLlevaElEnlaceConElToken() throws Exception {
		enviador.enviarVerificacion(new Correo("cliente@correo-de-prueba.com"), "token-verificacion-123");

		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[GREEN_MAIL.getReceivedMessages().length - 1];
		assertThat(recibido.getAllRecipients()[0].toString()).isEqualTo("cliente@correo-de-prueba.com");
		assertThat(recibido.getSubject()).isEqualTo("Verifica tu cuenta en Crear Code Cesar");
		assertThat((String) recibido.getContent())
				.contains("https://sitio-de-prueba.example/verificar-correo?token=token-verificacion-123")
				.contains("24 horas");
	}

	// El From explicito es lo que permite enviar por un servicio
	// transaccional: su usuario SMTP no es una direccion del dominio.
	@Test
	void saleDelRemitenteDelDominioPropioYSeRespondeAlBuzonDeContacto() throws Exception {
		enviador.enviarVerificacion(new Correo("cliente@correo-de-prueba.com"), "token-remitente");

		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[GREEN_MAIL.getReceivedMessages().length - 1];
		assertThat(recibido.getFrom()[0].toString())
				.isEqualTo("Crear Code Cesar <contacto@crearcodecesar.com>");
		assertThat(recibido.getReplyTo()[0].toString()).isEqualTo("contacto@crearcodecesar.com");
	}

	@Test
	void elCorreoDeRecuperacionLlevaElEnlaceConElToken() throws Exception {
		enviador.enviarRecuperacion(new Correo("cliente@correo-de-prueba.com"), "token-recuperacion-456");

		MimeMessage recibido = GREEN_MAIL.getReceivedMessages()[GREEN_MAIL.getReceivedMessages().length - 1];
		assertThat(recibido.getSubject()).isEqualTo("Restablece tu contraseña en Crear Code Cesar");
		assertThat((String) recibido.getContent())
				.contains("https://sitio-de-prueba.example/restablecer-contrasena?token=token-recuperacion-456")
				.contains("1 hora");
	}

}
