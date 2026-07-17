package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.CifradorDeContrasenas;

/** Fake determinista de {@link CifradorDeContrasenas} para tests de casos de uso. */
class FakeCifradorDeContrasenas implements CifradorDeContrasenas {

	@Override
	public String hash(String contrasenaEnClaro) {
		return "hash:" + contrasenaEnClaro;
	}

	@Override
	public boolean verificar(String contrasenaEnClaro, String hash) {
		return hash(contrasenaEnClaro).equals(hash);
	}

}
