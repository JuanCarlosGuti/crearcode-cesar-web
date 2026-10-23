package com.crearcode.leads.infraestructura.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
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
