package com.crearcode.leads.infraestructura.notificacion;

/**
 * Cómo sale físicamente un correo. Existe porque **Render bloquea el
 * tráfico saliente a los puertos SMTP (25, 465, 587) en los servicios
 * del plan gratuito**: las conexiones no se rechazan, se descartan, así
 * que el envío se queda colgado hasta agotar el timeout. En producción
 * se usa entonces la API HTTP de Resend; en local sigue siendo SMTP
 * contra Mailpit, que es lo que hace verificable el flujo sin red.
 */
interface TransporteDeCorreo {

	void enviar(CorreoSaliente correo);

}
