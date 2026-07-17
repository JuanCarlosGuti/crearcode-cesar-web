package com.crearcode.leads.infraestructura.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptCifradorDeContrasenasTest {

	private final BCryptCifradorDeContrasenas cifrador = new BCryptCifradorDeContrasenas(new BCryptPasswordEncoder());

	@Test
	void elHashNuncaContieneLaContrasenaEnClaro() {
		String hash = cifrador.hash("mi-clave-secreta");

		assertThat(hash).doesNotContain("mi-clave-secreta");
	}

	@Test
	void verificarAceptaLaContrasenaCorrecta() {
		String hash = cifrador.hash("mi-clave-secreta");

		assertThat(cifrador.verificar("mi-clave-secreta", hash)).isTrue();
	}

	@Test
	void verificarRechazaUnaContrasenaIncorrecta() {
		String hash = cifrador.hash("mi-clave-secreta");

		assertThat(cifrador.verificar("otra-clave", hash)).isFalse();
	}

	@Test
	void dosHashesDeLaMismaContrasenaSonDistintos() {
		assertThat(cifrador.hash("mi-clave-secreta")).isNotEqualTo(cifrador.hash("mi-clave-secreta"));
	}

}
