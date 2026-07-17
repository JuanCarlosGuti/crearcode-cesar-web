package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;

import com.crearcode.leads.dominio.NotificadorPort;
import com.crearcode.leads.dominio.SolicitudDeContacto;

/** Fake de {@link NotificadorPort} para tests de casos de uso. */
class FakeNotificadorPort implements NotificadorPort {

	final List<SolicitudDeContacto> notificadas = new ArrayList<>();
	boolean fallarAlNotificar = false;

	@Override
	public void notificarNuevaSolicitud(SolicitudDeContacto solicitud) {
		if (fallarAlNotificar) {
			throw new RuntimeException("Fallo simulado de notificación");
		}
		notificadas.add(solicitud);
	}

}
