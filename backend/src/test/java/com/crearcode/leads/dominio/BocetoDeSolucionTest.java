package com.crearcode.leads.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * ISS-230: el boceto que arma Claude (ADR-16). La salida estructurada
 * de la API garantiza la forma del JSON, pero no los largos ni las
 * cantidades: esas reglas viven aquí.
 */
class BocetoDeSolucionTest {

	private static ElementoDePieza elemento(String texto) {
		return new ElementoDePieza(texto, "");
	}

	private static PiezaDeBoceto encabezado(String titulo) {
		return new PiezaDeBoceto(TipoDePieza.ENCABEZADO, titulo, List.of());
	}

	private static PiezaDeBoceto lista(String... textos) {
		return new PiezaDeBoceto(TipoDePieza.LISTA, "", List.of(textos).stream().map(BocetoDeSolucionTest::elemento).toList());
	}

	private static PantallaDeBoceto pantalla(String nombre) {
		return new PantallaDeBoceto(nombre, List.of(encabezado("Pedidos de hoy"), lista("Ana · almuerzo", "Luis · desayuno")));
	}

	private static final List<String> CINCO = List.of("Menú del día", "Pedidos por WhatsApp", "Estado del domicilio",
			"Historial de clientes", "Avisos de pedido listo");

	@Test
	void unBocetoValidoConservaSusPantallasYNormalizaLosTextos() {
		BocetoDeSolucion boceto = new BocetoDeSolucion("  App de pedidos para tu restaurante  ", CINCO,
				List.of(pantalla(" Inicio "), pantalla("Pedidos")));

		assertThat(boceto.titulo()).isEqualTo("App de pedidos para tu restaurante");
		assertThat(boceto.pantallas()).extracting(PantallaDeBoceto::nombre).containsExactly("Inicio", "Pedidos");
		assertThat(boceto.funcionalidades()).hasSize(5);
	}

	@Test
	void exigeDeDosATresPantallas() {
		assertThatThrownBy(() -> new BocetoDeSolucion("App", CINCO, List.of(pantalla("Inicio"))))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new BocetoDeSolucion("App", CINCO,
				List.of(pantalla("A"), pantalla("B"), pantalla("C"), pantalla("D"))))
				.isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void exigeDeTresASeisFuncionalidadesNoVacias() {
		List<PantallaDeBoceto> dos = List.of(pantalla("A"), pantalla("B"));
		assertThatThrownBy(() -> new BocetoDeSolucion("App", List.of("a", "b"), dos))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new BocetoDeSolucion("App", List.of("a", "b", "c", "d", "e", "f", "g"), dos))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new BocetoDeSolucion("App", List.of("a", " ", "c"), dos))
				.isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void unaPantallaLlevaDeDosAOchoPiezas() {
		assertThatThrownBy(() -> new PantallaDeBoceto("Inicio", List.of(encabezado("Hola"))))
				.isInstanceOf(BocetoInvalidoException.class);
		List<PiezaDeBoceto> nueve = java.util.Collections.nCopies(9, encabezado("Hola"));
		assertThatThrownBy(() -> new PantallaDeBoceto("Inicio", nueve)).isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void losTextosLargosSeRechazanEnVezDeCortarse() {
		assertThatThrownBy(() -> encabezado("x".repeat(PiezaDeBoceto.MAXIMO_TITULO + 1)))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> elemento("x".repeat(ElementoDePieza.MAXIMO_TEXTO + 1)))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new ElementoDePieza("Ana", "x".repeat(ElementoDePieza.MAXIMO_DETALLE + 1)))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new PantallaDeBoceto("x".repeat(PantallaDeBoceto.MAXIMO_NOMBRE + 1),
				List.of(encabezado("Hola"), lista("a")))).isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void cadaTipoExigeLoQueNecesitaParaDibujarse() {
		// Un botón sin texto o una lista sin filas no se pueden dibujar.
		assertThatThrownBy(() -> new PiezaDeBoceto(TipoDePieza.BOTON, " ", List.of()))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new PiezaDeBoceto(TipoDePieza.LISTA, "Pedidos", List.of()))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThat(new PiezaDeBoceto(TipoDePieza.BOTON, "Pedir ahora", List.of()).titulo()).isEqualTo("Pedir ahora");
	}

	@Test
	void unaPiezaLlevaComoMucho6Elementos() {
		List<ElementoDePieza> siete = java.util.Collections.nCopies(7, elemento("Ana"));
		assertThatThrownBy(() -> new PiezaDeBoceto(TipoDePieza.LISTA, "", siete))
				.isInstanceOf(BocetoInvalidoException.class);
	}

	@Test
	void ningunTextoMuestraDinero() {
		// Regla del sitio: ninguna cifra que no podamos sustentar. El
		// cotizador es el único lugar con valores, y son rangos.
		assertThatThrownBy(() -> new ElementoDePieza("Almuerzo", "$ 15.000"))
				.isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> encabezado("Total: 20.000 COP")).isInstanceOf(BocetoInvalidoException.class);
		assertThatThrownBy(() -> new BocetoDeSolucion("App", List.of("Cobros en USD", "b", "c"),
				List.of(pantalla("A"), pantalla("B")))).isInstanceOf(BocetoInvalidoException.class);
		// "Peso" de una carga no es dinero.
		assertThat(new ElementoDePieza("Paquete", "Peso: 5 kg").detalle()).isEqualTo("Peso: 5 kg");
	}

	@Test
	void elTipoSeReconoceSinImportarMayusculas() {
		// La API no garantiza las mayúsculas de un enum.
		assertThat(TipoDePieza.desde("lista")).contains(TipoDePieza.LISTA);
		assertThat(TipoDePieza.desde("Navegacion")).contains(TipoDePieza.NAVEGACION);
		assertThat(TipoDePieza.desde("carrusel")).isEmpty();
	}

}
