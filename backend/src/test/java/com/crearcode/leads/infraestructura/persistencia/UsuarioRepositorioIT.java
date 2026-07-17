package com.crearcode.leads.infraestructura.persistencia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Usa un correo de prueba distinto al del admin sembrado
 * automaticamente al arrancar (ver {@link SembradorDeUsuarioAdmin} en
 * infraestructura/seguridad) para no chocar con el indice unico de
 * correo.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class UsuarioRepositorioIT {

	private static final String CORREO_DE_PRUEBA = "otro-usuario@crearcode-cesar-test.local";

	@Autowired
	private UsuarioRepositorio repositorio;

	@Test
	void guardarYBuscarPorCorreoDevuelveElMismoUsuario() {
		Usuario usuario = Usuario.crear(new Correo(CORREO_DE_PRUEBA), "hash", Rol.ADMIN);

		repositorio.guardar(usuario);

		Usuario encontrado = repositorio.buscarPorCorreo(new Correo(CORREO_DE_PRUEBA)).orElseThrow();
		assertThat(encontrado.id()).isEqualTo(usuario.id());
		assertThat(encontrado.correo()).isEqualTo(usuario.correo());
		assertThat(encontrado.contrasenaHash()).isEqualTo("hash");
		assertThat(encontrado.rol()).isEqualTo(Rol.ADMIN);
	}

	@Test
	void buscarPorCorreoEsInsensibleAMayusculas() {
		repositorio.guardar(Usuario.crear(new Correo(CORREO_DE_PRUEBA), "hash", Rol.ADMIN));

		assertThat(repositorio.buscarPorCorreo(new Correo("Otro-Usuario@Crearcode-Cesar-Test.Local"))).isPresent();
	}

	@Test
	void buscarPorCorreoInexistenteDevuelveVacio() {
		assertThat(repositorio.buscarPorCorreo(new Correo("no-existe@crearcode-cesar-test.local"))).isEmpty();
	}

}
