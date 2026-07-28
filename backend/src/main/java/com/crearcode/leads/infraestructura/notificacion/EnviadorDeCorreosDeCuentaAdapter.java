package com.crearcode.leads.infraestructura.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EnviadorDeCorreosDeCuenta;

/**
 * Correos transaccionales de cuentas (fase F8). Este adaptador es quien
 * arma el enlace completo — la aplicación solo entrega el token en
 * claro y no conoce rutas del frontend. Copys en docs/08-contenido.md
 * §Cuentas de cliente. `app.frontend-url` sale de FRONTEND_URL (ADR-06:
 * nada hardcodeado al dominio web).
 */
@Component
class EnviadorDeCorreosDeCuentaAdapter implements EnviadorDeCorreosDeCuenta {

	private final JavaMailSender mailSender;
	private final String frontendUrl;

	EnviadorDeCorreosDeCuentaAdapter(JavaMailSender mailSender,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.mailSender = mailSender;
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void enviarVerificacion(Correo destino, String tokenEnClaro) {
		String enlace = frontendUrl + "/verificar-correo?token=" + tokenEnClaro;
		enviar(destino, "Verifica tu cuenta en Crear Code Cesar", """
				Hola,

				Gracias por registrarte en Crear Code Cesar. Para activar tu
				cuenta, abre este enlace (vence en 24 horas):

				%s

				Si no creaste esta cuenta, ignora este correo.

				— Crear Code Cesar S.A.S. · Valledupar, Colombia
				""".formatted(enlace));
	}

	@Override
	public void enviarRecuperacion(Correo destino, String tokenEnClaro) {
		String enlace = frontendUrl + "/restablecer-contrasena?token=" + tokenEnClaro;
		enviar(destino, "Restablece tu contraseña en Crear Code Cesar", """
				Hola,

				Recibimos una solicitud para restablecer tu contraseña. Abre
				este enlace para crear una nueva (vence en 1 hora):

				%s

				Si no fuiste tú, ignora este correo: tu contraseña actual sigue
				siendo válida.

				— Crear Code Cesar S.A.S. · Valledupar, Colombia
				""".formatted(enlace));
	}

	private void enviar(Correo destino, String asunto, String cuerpo) {
		SimpleMailMessage mensaje = new SimpleMailMessage();
		mensaje.setTo(destino.valor());
		mensaje.setSubject(asunto);
		mensaje.setText(cuerpo);
		mailSender.send(mensaje);
	}

}
