package com.crearcode.leads.infraestructura.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el comportamiento de {@link SembradorDeUsuarioAdmin} contra
 * el arranque real de la aplicacion (valores por defecto de
 * ADMIN_USERNAME/ADMIN_PASSWORD en application.properties).
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class SembradorDeUsuarioAdminIT {

	@Autowired
	private UsuarioRepositorio repositorio;

	@Autowired
	private CifradorDeContrasenas cifrador;

	@Test
	void creaElUsuarioAdminConfiguradoAlArrancarConLaContrasenaCifrada() {
		Usuario usuario = repositorio.buscarPorCorreo(new Correo("admin@crearcode-cesar.local")).orElseThrow();

		assertThat(usuario.rol()).isEqualTo(Rol.ADMIN);
		assertThat(cifrador.verificar("cambiar-en-produccion", usuario.contrasenaHash())).isTrue();
	}

}
