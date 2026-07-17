package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

	private static final Correo CORREO = new Correo("admin@crearcode-cesar.local");

	@Test
	void crearAsignaUnIdYLosDatosDados() {
		Usuario usuario = Usuario.crear(CORREO, "hash-de-prueba", Rol.ADMIN);

		assertThat(usuario.id()).isNotNull();
		assertThat(usuario.correo()).isEqualTo(CORREO);
		assertThat(usuario.contrasenaHash()).isEqualTo("hash-de-prueba");
		assertThat(usuario.rol()).isEqualTo(Rol.ADMIN);
	}

	@Test
	void cadaUsuarioCreadoTieneUnIdDistinto() {
		Usuario primero = Usuario.crear(CORREO, "hash", Rol.ADMIN);
		Usuario segundo = Usuario.crear(CORREO, "hash", Rol.ADMIN);

		assertThat(primero.id()).isNotEqualTo(segundo.id());
	}

	@Test
	void rechazaHashDeContrasenaVacio() {
		assertThatThrownBy(() -> Usuario.crear(CORREO, "   ", Rol.ADMIN))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rechazaHashDeContrasenaNulo() {
		assertThatThrownBy(() -> Usuario.crear(CORREO, null, Rol.ADMIN))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void reconstruyeUnUsuarioExistenteConSuIdOriginal() {
		UsuarioId id = UsuarioId.nuevo();

		Usuario usuario = new Usuario(id, CORREO, "hash", Rol.ADMIN);

		assertThat(usuario.id()).isEqualTo(id);
	}

}
