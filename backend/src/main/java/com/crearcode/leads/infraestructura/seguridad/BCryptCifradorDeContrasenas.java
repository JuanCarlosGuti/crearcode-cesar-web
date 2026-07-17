package com.crearcode.leads.infraestructura.seguridad;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.CifradorDeContrasenas;

@Component
class BCryptCifradorDeContrasenas implements CifradorDeContrasenas {

	private final PasswordEncoder encoder;

	BCryptCifradorDeContrasenas(PasswordEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	public String hash(String contrasenaEnClaro) {
		return encoder.encode(contrasenaEnClaro);
	}

	@Override
	public boolean verificar(String contrasenaEnClaro, String hash) {
		return encoder.matches(contrasenaEnClaro, hash);
	}

}
