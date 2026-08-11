package com.crearcode.leads.infraestructura.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.EnviadorDeCotizaciones;
import com.crearcode.leads.dominio.GeneradorDeDocumento;

/**
 * Le hace llegar la cotización al cliente con el PDF adjunto (HU-45):
 * es el único correo del sitio que lleva adjunto.
 *
 * <p>Como el resto de correos, el adaptador es quien construye el
 * enlace a la cuenta — la aplicación no conoce rutas del frontend — y
 * delega el envío en {@link TransporteDeCorreo}, que es quien sabe si
 * hoy se sale por SMTP o por la API de Resend.
 */
@Component
class EnviadorDeCotizacionesAdapter implements EnviadorDeCotizaciones {

	private final TransporteDeCorreo transporte;
	private final GeneradorDeDocumento generadorDeDocumento;
	private final String frontendUrl;

	EnviadorDeCotizacionesAdapter(TransporteDeCorreo transporte,
			GeneradorDeDocumento generadorDeDocumento,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.transporte = transporte;
		this.generadorDeDocumento = generadorDeDocumento;
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void enviar(Cotizacion cotizacion) {
		String numero = cotizacion.numero().valor();
		byte[] pdf = generadorDeDocumento.generar(cotizacion);

		String cuerpo = """
				Hola,

				Adjuntamos la cotización %s para %s.

				También puedes verla y responderla (aceptar o rechazar) desde tu
				cuenta:

				%s/mi-cuenta/cotizaciones

				La cotización es válida hasta la fecha indicada en el documento.
				Si tienes dudas, respóndenos este correo y lo hablamos.

				— Crear Code Cesar S.A.S. · Valledupar, Colombia
				""".formatted(numero, cotizacion.cliente().nombre(), frontendUrl);

		// El caso de uso trata el fallo como best-effort: la cotización
		// queda enviada igual y el PDF se puede compartir a mano.
		transporte.enviar(new CorreoSaliente(cotizacion.cliente().correo().valor(),
				"Tu cotización " + numero + " · Crear Code Cesar", cuerpo,
				new CorreoSaliente.Adjunto(numero + ".pdf", pdf)));
	}

}
