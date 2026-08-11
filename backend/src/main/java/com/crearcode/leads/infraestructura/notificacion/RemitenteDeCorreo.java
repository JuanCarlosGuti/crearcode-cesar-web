package com.crearcode.leads.infraestructura.notificacion;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Remitente de TODOS los correos que envía el sitio. Se fija explícito
 * en cada adaptador en vez de dejar que Spring use el usuario SMTP:
 * con un servicio transaccional ese usuario no es una dirección (en
 * Resend es literalmente {@code resend}), y el proveedor rechaza el
 * envío con **422 "from address not allowed"** si el remitente no
 * pertenece a un dominio verificado.
 *
 * <p>{@code responderA} va aparte a propósito: quien reciba el correo
 * debe poder responder a una dirección que alguien lee, aunque el envío
 * salga por la infraestructura del proveedor.
 */
@ConfigurationProperties(prefix = "app.correo")
public record RemitenteDeCorreo(String remitente, String responderA) {
}
