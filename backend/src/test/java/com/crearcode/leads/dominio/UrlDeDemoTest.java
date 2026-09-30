package com.crearcode.leads.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El panel pega un enlace y el portal lo pinta como botón "Ver demo" que
 * abre el cliente. Es la puerta clásica a un {@code javascript:} o a un
 * enlace disfrazado, así que se cierra en el dominio y no en la interfaz
 * (docs/03, Parte 5 §3).
 */
class UrlDeDemoTest {

	@Test
	void aceptaUnEnlaceHttps() {
		assertThat(new UrlDeDemo("https://demo.crearcodecesar.com/pedidos").valor())
				.isEqualTo("https://demo.crearcodecesar.com/pedidos");
	}

	@Test
	void quitaLosEspaciosDeLosBordes() {
		assertThat(new UrlDeDemo("  https://figma.com/proto/abc  ").valor()).isEqualTo("https://figma.com/proto/abc");
	}

	@Test
	void aceptaElEsquemaEnMayusculas() {
		assertThat(new UrlDeDemo("HTTPS://demo.ejemplo.co").valor()).isEqualTo("HTTPS://demo.ejemplo.co");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	void rechazaUnEnlaceVacio(String valor) {
		assertThatThrownBy(() -> new UrlDeDemo(valor)).isInstanceOf(ProyectoInvalidoException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"javascript:alert(1)",
			"JavaScript:alert(document.cookie)",
			"data:text/html,<script>alert(1)</script>",
			"http://demo.ejemplo.co",
			"ftp://demo.ejemplo.co",
			"//demo.ejemplo.co",
			"demo.ejemplo.co" })
	void rechazaTodoLoQueNoSeaHttps(String valor) {
		assertThatThrownBy(() -> new UrlDeDemo(valor)).isInstanceOf(ProyectoInvalidoException.class);
	}

	/**
	 * {@code https://crearcodecesar.com@otro-sitio.co} abre otro-sitio.co,
	 * pero a primera vista parece nuestro.
	 */
	@Test
	void rechazaUnEnlaceConUsuarioAntesDelDominio() {
		assertThatThrownBy(() -> new UrlDeDemo("https://crearcodecesar.com@otro-sitio.co/demo"))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void rechazaUnEnlaceSinDominio() {
		assertThatThrownBy(() -> new UrlDeDemo("https:///ruta")).isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void rechazaUnEnlaceConEspaciosAdentro() {
		assertThatThrownBy(() -> new UrlDeDemo("https://demo.ejemplo.co/mi demo"))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

	@Test
	void rechazaUnEnlaceDemasiadoLargo() {
		assertThatThrownBy(() -> new UrlDeDemo("https://demo.ejemplo.co/" + "a".repeat(500)))
				.isInstanceOf(ProyectoInvalidoException.class);
	}

}
