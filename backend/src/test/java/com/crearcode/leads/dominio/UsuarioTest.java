package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

	private static final Correo CORREO = new Correo("admin@crearcode-cesar.local");

	@Test
	void crearAsignaUnIdYLosDatosDadosYNaceVerificado() {
		Usuario usuario = Usuario.crear(CORREO, "hash-de-prueba", Rol.ADMIN);

		assertThat(usuario.id()).isNotNull();
		assertThat(usuario.correo()).isEqualTo(CORREO);
		assertThat(usuario.contrasenaHash()).isEqualTo("hash-de-prueba");
		assertThat(usuario.rol()).isEqualTo(Rol.ADMIN);
		assertThat(usuario.verificado()).isTrue();
	}

	@Test
	void registrarClienteNaceConRolClienteYSinVerificar() {
		Usuario cliente = Usuario.registrarCliente(CORREO, "hash-de-prueba");

		assertThat(cliente.id()).isNotNull();
		assertThat(cliente.rol()).isEqualTo(Rol.CLIENTE);
		assertThat(cliente.verificado()).isFalse();
	}

	@Test
	void verificarDevuelveUnaCopiaVerificadaSinTocarLoDemas() {
		Usuario cliente = Usuario.registrarCliente(CORREO, "hash");

		Usuario verificado = cliente.verificar();

		assertThat(verificado.verificado()).isTrue();
		assertThat(verificado.id()).isEqualTo(cliente.id());
		assertThat(verificado.correo()).isEqualTo(cliente.correo());
		assertThat(verificado.contrasenaHash()).isEqualTo(cliente.contrasenaHash());
		assertThat(verificado.rol()).isEqualTo(cliente.rol());
		assertThat(cliente.verificado()).isFalse();
	}

	@Test
	void conContrasenaCambiaSoloElHash() {
		Usuario cliente = Usuario.registrarCliente(CORREO, "hash-viejo");

		Usuario actualizado = cliente.conContrasena("hash-nuevo");

		assertThat(actualizado.contrasenaHash()).isEqualTo("hash-nuevo");
		assertThat(actualizado.id()).isEqualTo(cliente.id());
		assertThat(actualizado.correo()).isEqualTo(cliente.correo());
		assertThat(actualizado.rol()).isEqualTo(cliente.rol());
		assertThat(actualizado.verificado()).isEqualTo(cliente.verificado());
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

		Usuario usuario = new Usuario(id, CORREO, "hash", Rol.ADMIN, true);

		assertThat(usuario.id()).isEqualTo(id);
	}

}
