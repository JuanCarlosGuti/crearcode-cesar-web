package com.crearcode.leads.aplicacion;

import java.util.ArrayList;
import java.util.List;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.EnviadorDeCorreosDeCuenta;

/**
 * Fake de {@link EnviadorDeCorreosDeCuenta}: registra los envíos y
 * puede simular un fallo de SMTP (para probar el best-effort).
 */
class FakeEnviadorDeCorreosDeCuenta implements EnviadorDeCorreosDeCuenta {

	record Envio(Correo destino, String tokenEnClaro) {
	}

	private final List<Envio> verificaciones = new ArrayList<>();
	private final List<Envio> recuperaciones = new ArrayList<>();
	private boolean fallar = false;

	@Override
	public void enviarVerificacion(Correo destino, String tokenEnClaro) {
		if (fallar) {
			throw new IllegalStateException("SMTP caído (simulado)");
		}
		verificaciones.add(new Envio(destino, tokenEnClaro));
	}

	@Override
	public void enviarRecuperacion(Correo destino, String tokenEnClaro) {
		if (fallar) {
			throw new IllegalStateException("SMTP caído (simulado)");
		}
		recuperaciones.add(new Envio(destino, tokenEnClaro));
	}

	void simularFallo() {
		this.fallar = true;
	}

	List<Envio> verificacionesEnviadas() {
		return List.copyOf(verificaciones);
	}

	List<Envio> recuperacionesEnviadas() {
		return List.copyOf(recuperaciones);
	}

}
