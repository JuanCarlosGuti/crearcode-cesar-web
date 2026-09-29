package com.crearcode.leads.aplicacion;

import com.crearcode.leads.dominio.CifradorDeContrasenas;

/** Fake determinista de {@link CifradorDeContrasenas} para tests de casos de uso. */
class FakeCifradorDeContrasenas implements CifradorDeContrasenas {

	/** Cuantas veces se verifico: el senuelo anti-timing se prueba contando. */
	int verificaciones;

	@Override
	public String hash(String contrasenaEnClaro) {
		return "hash:" + contrasenaEnClaro;
	}

	@Override
	public boolean verificar(String contrasenaEnClaro, String hash) {
		verificaciones++;
		return hash(contrasenaEnClaro).equals(hash);
	}

}
