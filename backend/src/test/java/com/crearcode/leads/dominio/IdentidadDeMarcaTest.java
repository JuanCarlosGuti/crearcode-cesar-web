package com.crearcode.leads.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * ISS-237 (decisión 35): la identidad de marca que propone la IA. Todo
 * se elige de listas cerradas, y el único texto libre que no es un
 * texto —el símbolo del logo y el ícono de cada elemento— tiene que ser
 * un emoji de verdad, para que no se cuele nada más.
 */
class IdentidadDeMarcaTest {

	private static LogoDeMarca logo(String simbolo) {
		return new LogoDeMarca(FormaDeLogo.ESCUDO, simbolo, EstiloDeLetra.CLASICA);
	}

	@Test
	void unaIdentidadValidaConservaSusPartesYNormalizaLosTextos() {
		IdentidadDeMarca identidad = new IdentidadDeMarca("  Barbería El Patrón ", " Estilo que se nota ",
				PaletaDeBoceto.NOCTURNA, logo("💈"));

		assertThat(identidad.nombre()).isEqualTo("Barbería El Patrón");
		assertThat(identidad.eslogan()).isEqualTo("Estilo que se nota");
		assertThat(identidad.paleta()).isEqualTo(PaletaDeBoceto.NOCTURNA);
		assertThat(identidad.logo().simbolo()).isEqualTo("💈");
	}

	@Test
	void elNombreEsObligatorioYCorto() {
		assertThatThrownBy(() -> new IdentidadDeMarca(" ", "", PaletaDeBoceto.FRESCA, logo("")))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new IdentidadDeMarca("x".repeat(IdentidadDeMarca.MAXIMO_NOMBRE + 1), "",
				PaletaDeBoceto.FRESCA, logo(""))).isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new IdentidadDeMarca("Tienda", "x".repeat(IdentidadDeMarca.MAXIMO_ESLOGAN + 1),
				PaletaDeBoceto.FRESCA, logo(""))).isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void elEsloganTampocoMuestraDinero() {
		assertThatThrownBy(() -> new IdentidadDeMarca("Tienda", "Todo a $ 10.000", PaletaDeBoceto.FRESCA, logo("")))
				.isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void elSimboloAceptaEmojisCompuestosYVacio() {
		// Con selector de variación, con unión invisible y una bandera.
		for (String emoji : new String[] { "💈", "✂️", "👩‍🍳", "🇨🇴", "" }) {
			assertThat(logo(emoji).simbolo()).isEqualTo(emoji);
		}
	}

	@Test
	void elSimboloRechazaCualquierCosaQueNoSeaUnEmoji() {
		for (String noEmoji : new String[] { "A", "1", "<b>", "💈 Barbería", "#", "💈💈💈💈💈💈💈" }) {
			assertThatThrownBy(() -> logo(noEmoji)).as(noEmoji).isInstanceOf(BocetoInvalidoException.class);
		}
	}

	@Test
	void cadaElementoPuedeLlevarSuIcono() {
		assertThat(new ElementoDePieza("Corte clásico", "30 minutos", "✂️").icono()).isEqualTo("✂️");
		assertThat(new ElementoDePieza("Corte clásico", "30 minutos").icono()).isEmpty();
		assertThatThrownBy(() -> new ElementoDePieza("Corte", "", "tijeras"))
				.isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void lasListasCerradasSeReconocenSinImportarMayusculas() {
		assertThat(PaletaDeBoceto.desde("calida")).contains(PaletaDeBoceto.CALIDA);
		assertThat(FormaDeLogo.desde("Hexagono")).contains(FormaDeLogo.HEXAGONO);
		assertThat(EstiloDeLetra.desde("SENCILLA")).contains(EstiloDeLetra.SENCILLA);
		assertThat(PaletaDeBoceto.desde("arcoiris")).isEmpty();
	}

}
