package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.SolicitudDeContacto;

/**
 * Se registró una solicitud de contacto. Lo consume la notificación
 * por correo DESPUÉS de que la transacción confirme.
 *
 * <p>
 * Antes el correo se enviaba dentro del {@code @Transactional}, en
 * línea: un SMTP lento mantenía abiertas a la vez la conexión de base
 * de datos y la petición HTTP del visitante hasta que venciera el
 * timeout —10 s el de SMTP, 15 s el de Resend— y el visitante miraba
 * un botón «Enviando…» todo ese rato por un correo que no es suyo
 * (auditoría del 28 sep 2026, P2-2e). Peor: si la transacción se
 * revertía después, el correo ya había salido avisando de una
 * solicitud que no existe.
 */
record SolicitudRegistrada(SolicitudDeContacto solicitud) {
}
