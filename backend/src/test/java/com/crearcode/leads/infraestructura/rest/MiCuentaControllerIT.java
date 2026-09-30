package com.crearcode.leads.infraestructura.rest;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.GeneradorDeToken;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Borrar la propia cuenta es el derecho de supresión de la Ley 1581
 * que la política v2 promete. Lo que esta prueba cuida no es que
 * funcione, sino que no se convierta en una forma de borrar la cuenta
 * de otro.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class MiCuentaControllerIT {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepositorio usuarios;

	@Autowired
	private GeneradorDeToken generadorDeToken;

	private String tokenDe(String correo) {
		Usuario cliente = Usuario.registrarCliente(new Correo(correo), "hash-cualquiera").verificar();
		usuarios.guardar(cliente);
		return generadorDeToken.generar(cliente, Instant.now()).token();
	}

	private ResponseEntity<String> borrarCuentaCon(String token) {
		HttpHeaders headers = new HttpHeaders();
		if (token != null) {
			headers.setBearerAuth(token);
		}
		return restTemplate.exchange("/api/mi-cuenta", HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
	}

	@Test
	void unClienteBorraSuPropiaCuentaYDejaDeExistir() {
		String correo = "borrame@correo-de-prueba.com";
		String token = tokenDe(correo);

		ResponseEntity<String> respuesta = borrarCuentaCon(token);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(usuarios.buscarPorCorreo(new Correo(correo))).isEmpty();
	}

	/**
	 * El correo sale del token y no del cuerpo, así que no hay forma de
	 * pedir que se borre una cuenta ajena: sin token no hay a quién
	 * borrar.
	 */
	@Test
	void sinTokenNoSeBorraNada() {
		String correo = "intacta@correo-de-prueba.com";
		tokenDe(correo);

		ResponseEntity<String> respuesta = borrarCuentaCon(null);

		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(usuarios.buscarPorCorreo(new Correo(correo))).isPresent();
	}

	@Test
	void borrarDosVecesLaMismaCuentaResponde404YNoRevienta() {
		String token = tokenDe("dos-veces@correo-de-prueba.com");
		borrarCuentaCon(token);

		assertThat(borrarCuentaCon(token).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

}
