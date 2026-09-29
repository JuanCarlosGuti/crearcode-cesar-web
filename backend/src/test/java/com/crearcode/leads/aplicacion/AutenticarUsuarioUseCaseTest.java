package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.SesionAutenticada;
import com.crearcode.leads.dominio.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutenticarUsuarioUseCaseTest {

	private FakeUsuarioRepositorio repositorio;
	private FakeCifradorDeContrasenas cifrador;
	private AutenticarUsuarioUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		repositorio = new FakeUsuarioRepositorio();
		cifrador = new FakeCifradorDeContrasenas();
		Clock reloj = Clock.fixed(Instant.parse("2026-07-16T10:00:00Z"), ZoneOffset.UTC);
		useCase = new AutenticarUsuarioUseCaseImpl(repositorio, cifrador, new FakeGeneradorDeToken(), reloj);
	}

	private void registrarUsuario(String correo, String contrasenaEnClaro) {
		repositorio.guardar(Usuario.crear(new Correo(correo), cifrador.hash(contrasenaEnClaro), Rol.ADMIN));
	}

	/**
	 * Con un correo que no existe se salia sin verificar nada, y
	 * verificar un hash de BCrypt cuesta ~100 ms: la diferencia de
	 * tiempo entre "este correo no existe" y "existe pero la clave esta
	 * mal" se mide desde fuera con un cronometro, asi que la respuesta
	 * generica no ocultaba nada (auditoria del 28 sep 2026, P3-a).
	 */
	@Test
	void unCorreoQueNoExisteGastaElMismoTrabajoQueUnoQueSi() {
		registrarUsuario("existe@correo.com", "clave-correcta");
		cifrador.verificaciones = 0;

		assertThatThrownBy(() -> useCase.autenticar("noexiste@correo.com", "lo-que-sea"))
				.isInstanceOf(CredencialesInvalidasException.class);

		assertThat(cifrador.verificaciones).isEqualTo(1);
	}

	@Test
	void autenticarConCredencialesCorrectasDevuelveUnaSesion() {
		registrarUsuario("admin@crearcode-cesar.local", "clave-correcta");

		SesionAutenticada sesion = useCase.autenticar("admin@crearcode-cesar.local", "clave-correcta");

		assertThat(sesion.token()).isNotBlank();
		assertThat(sesion.expiraEn()).isAfter(Instant.parse("2026-07-16T10:00:00Z"));
	}

	@Test
	void autenticarEsInsensibleAMayusculasEnElCorreo() {
		registrarUsuario("admin@crearcode-cesar.local", "clave-correcta");

		SesionAutenticada sesion = useCase.autenticar("Admin@Crearcode-Cesar.Local", "clave-correcta");

		assertThat(sesion.token()).isNotBlank();
	}

	@Test
	void autenticarConCorreoInexistenteLanzaCredencialesInvalidas() {
		assertThatThrownBy(() -> useCase.autenticar("no-existe@crearcode-cesar.local", "cualquiera"))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void autenticarConContrasenaIncorrectaLanzaCredencialesInvalidas() {
		registrarUsuario("admin@crearcode-cesar.local", "clave-correcta");

		assertThatThrownBy(() -> useCase.autenticar("admin@crearcode-cesar.local", "clave-incorrecta"))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void autenticarConCorreoDeFormatoInvalidoLanzaCredencialesInvalidasNoUnErrorDeFormato() {
		assertThatThrownBy(() -> useCase.autenticar("esto-no-es-un-correo", "cualquiera"))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void unClienteSinVerificarConContrasenaCorrectaLanzaCuentaNoVerificada() {
		repositorio.guardar(
				Usuario.registrarCliente(new Correo("cliente@correo-de-prueba.com"), cifrador.hash("clave-correcta")));

		assertThatThrownBy(() -> useCase.autenticar("cliente@correo-de-prueba.com", "clave-correcta"))
				.isInstanceOf(CuentaNoVerificadaException.class);
	}

	@Test
	void unClienteSinVerificarConContrasenaIncorrectaLanzaCredencialesInvalidasNoCuentaNoVerificada() {
		repositorio.guardar(
				Usuario.registrarCliente(new Correo("cliente@correo-de-prueba.com"), cifrador.hash("clave-correcta")));

		// El gate va DESPUÉS de verificar la contraseña: sin ella, el estado
		// de la cuenta no se revela (invariante 6 del contexto).
		assertThatThrownBy(() -> useCase.autenticar("cliente@correo-de-prueba.com", "clave-incorrecta"))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void unClienteVerificadoConCredencialesCorrectasObtieneSesion() {
		repositorio.guardar(Usuario
				.registrarCliente(new Correo("cliente@correo-de-prueba.com"), cifrador.hash("clave-correcta"))
				.verificar());

		SesionAutenticada sesion = useCase.autenticar("cliente@correo-de-prueba.com", "clave-correcta");

		assertThat(sesion.token()).isNotBlank();
		assertThat(sesion.rol()).isEqualTo(Rol.CLIENTE);
	}

}
