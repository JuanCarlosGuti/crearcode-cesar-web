package com.crearcode.leads.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.UsuarioId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrearUsuarioUseCaseTest {

	private FakeUsuarioRepositorio repositorio;
	private FakeCifradorDeContrasenas cifrador;
	private CrearUsuarioUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		repositorio = new FakeUsuarioRepositorio();
		cifrador = new FakeCifradorDeContrasenas();
		useCase = new CrearUsuarioUseCaseImpl(repositorio, cifrador);
	}

	@Test
	void crearPersisteElUsuarioConLaContrasenaCifrada() {
		Correo correo = new Correo("admin@crearcode-cesar.local");

		UsuarioId id = useCase.crear(correo, "clave-en-claro", Rol.ADMIN);

		var usuario = repositorio.buscarPorCorreo(correo).orElseThrow();
		assertThat(usuario.id()).isEqualTo(id);
		assertThat(usuario.contrasenaHash()).isEqualTo(cifrador.hash("clave-en-claro"));
		assertThat(usuario.rol()).isEqualTo(Rol.ADMIN);
	}

	@Test
	void rechazaCrearUnSegundoUsuarioConElMismoCorreo() {
		Correo correo = new Correo("admin@crearcode-cesar.local");
		useCase.crear(correo, "clave-uno", Rol.ADMIN);

		assertThatThrownBy(() -> useCase.crear(correo, "clave-dos", Rol.ADMIN))
				.isInstanceOf(UsuarioYaExisteException.class);
	}

}
