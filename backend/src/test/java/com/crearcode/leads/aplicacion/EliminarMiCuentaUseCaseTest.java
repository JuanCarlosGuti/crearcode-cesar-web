package com.crearcode.leads.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * "Revocar la autorización o pedir que eliminemos tus datos" es un
 * derecho de la Ley 1581, y la política v2 lo promete. Hasta ahora solo
 * se podía pidiéndolo por correo: existe el derecho, pero el titular
 * depende de que alguien lo atienda a mano.
 */
class EliminarMiCuentaUseCaseTest {

	private FakeUsuarioRepositorio usuarios;
	private FakeTokenDeUsuarioRepositorio tokens;
	private EliminarMiCuentaUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		usuarios = new FakeUsuarioRepositorio();
		tokens = new FakeTokenDeUsuarioRepositorio();
		useCase = new EliminarMiCuentaUseCaseImpl(usuarios, tokens);
	}

	private Usuario clienteRegistrado(String correo) {
		Usuario cliente = Usuario.registrarCliente(new Correo(correo), "hash-cualquiera").verificar();
		usuarios.guardar(cliente);
		return cliente;
	}

	@Test
	void eliminaLaCuentaDelClienteQueLoPide() {
		clienteRegistrado("cliente@correo.com");

		useCase.eliminar("cliente@correo.com");

		assertThat(usuarios.buscarPorCorreo(new Correo("cliente@correo.com"))).isEmpty();
	}

	/**
	 * Un token de recuperación vivo tras borrar la cuenta sería un enlace
	 * que apunta a un usuario que ya no existe: basura en la tabla y una
	 * sorpresa esperando a que alguien la encuentre.
	 */
	@Test
	void arrastraLosTokensDeCorreoDeEsaCuenta() {
		UsuarioId id = clienteRegistrado("cliente@correo.com").id();

		useCase.eliminar("cliente@correo.com");

		assertThat(tokens.eliminadosDe).containsExactly(id);
	}

	@Test
	void unCorreoQueNoExisteNoRevientaNiRevelaNada() {
		assertThatThrownBy(() -> useCase.eliminar("noexiste@correo.com"))
				.isInstanceOf(UsuarioNoEncontradoException.class);
	}

	/**
	 * El panel es de la empresa, no del cliente: una cuenta de admin no
	 * se borra sola desde una pantalla publica, porque dejaria el sitio
	 * sin quien lo administre.
	 */
	@Test
	void unaCuentaDeAdministradorNoSeElimina() {
		usuarios.guardar(Usuario.crear(new Correo("admin@crearcode.com"), "hash", Rol.ADMIN));

		assertThatThrownBy(() -> useCase.eliminar("admin@crearcode.com"))
				.isInstanceOf(EliminacionNoPermitidaException.class);
		assertThat(usuarios.buscarPorCorreo(new Correo("admin@crearcode.com"))).isPresent();
	}

}
