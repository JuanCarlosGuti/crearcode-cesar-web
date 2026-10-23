package com.crearcode.leads.infraestructura.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.ElementoDePieza;
import com.crearcode.leads.dominio.EstiloDeLetra;
import com.crearcode.leads.dominio.FormaDeLogo;
import com.crearcode.leads.dominio.PaletaDeBoceto;
import com.crearcode.leads.dominio.PiezaDeBoceto;
import com.crearcode.leads.dominio.TipoDePieza;

/**
 * ISS-231: lo que devuelve el proveedor pasa por aquí antes de ser un
 * boceto. Cualquier cosa que no se pueda leer o que no cumpla las
 * reglas es una falla del proveedor —pasa al respaldo—, nunca un error
 * del visitante.
 */
class LectorDeBocetoTest {

	@Test
	void conviertElJsonDelEsquemaEnElBocetoDelDominio() {
		BocetoDeSolucion boceto = LectorDeBoceto.leer(BocetoDePrueba.JSON);

		assertThat(boceto.titulo()).isEqualTo("App de pedidos para tu restaurante");
		assertThat(boceto.funcionalidades()).hasSize(5);
		assertThat(boceto.pantallas()).hasSize(2);
		assertThat(boceto.pantallas().getFirst().piezas()).extracting(pieza -> pieza.tipo())
				.containsExactly(TipoDePieza.ENCABEZADO, TipoDePieza.CATEGORIAS, TipoDePieza.BOTON);
		assertThat(boceto.pantallas().get(1).piezas().getFirst().elementos().getFirst().detalle())
				.isEqualTo("En camino");
	}

	@Test
	void leeLaIdentidadDeMarcaYLosDibujos() {
		BocetoDeSolucion boceto = LectorDeBoceto.leer(BocetoDePrueba.JSON);

		assertThat(boceto.identidad().nombre()).isEqualTo("Sabor Valduparense");
		assertThat(boceto.identidad().eslogan()).isEqualTo("Comida casera hasta tu puerta");
		assertThat(boceto.identidad().paleta()).isEqualTo(PaletaDeBoceto.CALIDA);
		assertThat(boceto.identidad().logo().forma()).isEqualTo(FormaDeLogo.CIRCULO);
		assertThat(boceto.identidad().logo().simbolo()).isEqualTo("🍲");
		assertThat(boceto.identidad().logo().letra()).isEqualTo(EstiloDeLetra.SENCILLA);
		assertThat(boceto.pantallas().getFirst().piezas().get(1).elementos()).extracting(ElementoDePieza::icono)
				.containsExactly("🍛", "🍳");
	}

	/**
	 * Un dibujo que no es un emoji se descarta en vez de perder el boceto:
	 * el elemento queda sin dibujo y el logo usa la inicial.
	 */
	@Test
	void unIconoOUnSimboloQueNoEsEmojiSeDescartaSinPerderElBoceto() {
		String conTexto = BocetoDePrueba.JSON.replace("\"🍛\"", "\"almuerzo\"").replace("\"🍲\"", "\"S\"");

		BocetoDeSolucion boceto = LectorDeBoceto.leer(conTexto);

		assertThat(boceto.pantallas().getFirst().piezas().get(1).elementos().getFirst().icono()).isEmpty();
		assertThat(boceto.identidad().logo().simbolo()).isEmpty();
	}

	@Test
	void unValorFueraDeLasListasCerradasTomaElDePorDefecto() {
		String fueraDeLista = BocetoDePrueba.JSON.replace("\"CALIDA\"", "\"ARCOIRIS\"")
				.replace("\"CIRCULO\"", "\"TRIANGULO\"").replace("\"SENCILLA\"", "\"GOTICA\"");

		BocetoDeSolucion boceto = LectorDeBoceto.leer(fueraDeLista);

		assertThat(boceto.identidad().paleta()).isEqualTo(PaletaDeBoceto.FRESCA);
		assertThat(boceto.identidad().logo().forma()).isEqualTo(FormaDeLogo.CIRCULO);
		assertThat(boceto.identidad().logo().letra()).isEqualTo(EstiloDeLetra.MODERNA);
	}

	@Test
	void sinIdentidadEsFallaDelProveedor() {
		String sinIdentidad = BocetoDePrueba.JSON.substring(0, BocetoDePrueba.JSON.indexOf("\"identidad\""))
				+ BocetoDePrueba.JSON.substring(BocetoDePrueba.JSON.indexOf("\"titulo\": \"App"));

		assertThatThrownBy(() -> LectorDeBoceto.leer(sinIdentidad)).isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unTipoQueNoEstaEnElCatalogoEsFallaDelProveedor() {
		String conTipoInventado = BocetoDePrueba.JSON.replace("\"BOTON\"", "\"CARRUSEL\"");

		assertThatThrownBy(() -> LectorDeBoceto.leer(conTipoInventado))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unJsonRotoEsFallaDelProveedor() {
		assertThatThrownBy(() -> LectorDeBoceto.leer("{\"titulo\": \"App\""))
				.isInstanceOf(AsistenteNoDisponibleException.class);
		assertThatThrownBy(() -> LectorDeBoceto.leer("No puedo ayudarte con eso."))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	/**
	 * En la comparación del 23 oct 2026, Opus escribió un mensaje de chat
	 * de más de 60 caracteres y se perdió el boceto entero. Lo que se pasa
	 * de largo se ajusta aquí, antes del dominio, cortando en una palabra
	 * completa.
	 */
	@Test
	void unTextoQueSePasaDeLargoSeCortaEnUnaPalabraCompletaYElBocetoSeSalva() {
		String mensajeLargo = "Hola, ¿me confirmas si el pedido de cemento llega antes del mediodía a la obra?";
		String conMensajeLargo = BocetoDePrueba.JSON.replace("\"Ana · almuerzo ejecutivo\"", "\"" + mensajeLargo + "\"");

		BocetoDeSolucion boceto = LectorDeBoceto.leer(conMensajeLargo);

		String ajustado = boceto.pantallas().get(1).piezas().getFirst().elementos().getFirst().texto();
		assertThat(ajustado).hasSizeLessThanOrEqualTo(ElementoDePieza.MAXIMO_TEXTO).endsWith("…");
		assertThat(mensajeLargo).startsWith(ajustado.substring(0, ajustado.length() - 1));
		assertThat(ajustado.substring(0, ajustado.length() - 1)).doesNotEndWith(" ");
		assertThat(mensajeLargo.charAt(ajustado.length() - 1)).isEqualTo(' ');
	}

	@Test
	void loQueSobraDeUnaListaSeDescartaSinPerderElBoceto() {
		String categoria = "{\"texto\": \"%s\", \"detalle\": \"\", \"icono\": \"\"}";
		String siete = String.join(", ", java.util.stream.Stream.of("A", "B", "C", "D", "E", "F", "G")
				.map(categoria::formatted).toList());
		String sieteCategorias = BocetoDePrueba.JSON.replace(
				"{\"texto\": \"Almuerzos\", \"detalle\": \"\", \"icono\": \"🍛\"}, "
						+ "{\"texto\": \"Desayunos\", \"detalle\": \"\", \"icono\": \"🍳\"}",
				siete);
		assertThat(sieteCategorias).contains("\"G\"");

		BocetoDeSolucion boceto = LectorDeBoceto.leer(sieteCategorias);

		assertThat(boceto.pantallas().getFirst().piezas().get(1).elementos()).hasSize(PiezaDeBoceto.MAXIMO_ELEMENTOS);
	}

	@Test
	void unBocetoQueRompeLasReglasDelDominioEsFallaDelProveedor() {
		String conPrecio = BocetoDePrueba.JSON.replace("\"En camino\"", "\"$ 18.000\"");

		assertThatThrownBy(() -> LectorDeBoceto.leer(conPrecio)).isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unCampoQueFaltaEsFallaDelProveedor() {
		String sinPantallas = """
				{"titulo": "App", "funcionalidades": ["a", "b", "c"]}
				""";

		assertThatThrownBy(() -> LectorDeBoceto.leer(sinPantallas)).isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
