package com.crearcode.leads.aplicacion;

import java.time.Duration;
import java.time.Instant;

import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.SesionAutenticada;
import com.crearcode.leads.dominio.Usuario;

/** Fake determinista de {@link GeneradorDeToken} para tests de casos de uso. */
class FakeGeneradorDeToken implements GeneradorDeToken {

	@Override
	public SesionAutenticada generar(Usuario usuario, Instant ahora) {
		return new SesionAutenticada("token-de-prueba:" + usuario.id().valor(), ahora.plus(Duration.ofHours(8)));
	}

}
