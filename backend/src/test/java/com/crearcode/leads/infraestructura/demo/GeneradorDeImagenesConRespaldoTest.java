package com.crearcode.leads.infraestructura.demo;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.ImagenGenerada;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El demo de diseño no puede depender de un solo proveedor sin SLA: si
 * el primario falla, el respaldo responde y el visitante ni se entera.
 */
class GeneradorDeImagenesConRespaldoTest {

	private final List<String> llamados = new ArrayList<>();

	private GeneradorDeImagenes queResponde(String nombre) {
		return descripcion -> {
			llamados.add(nombre);
			return new ImagenGenerada("base64-de-" + nombre, "image/jpeg");
		};
	}

	private GeneradorDeImagenes queFalla(String nombre) {
		return descripcion -> {
			llamados.add(nombre);
			throw new AsistenteNoDisponibleException("falló " + nombre);
		};
	}

	@Test
	void usaElPrimarioYNiSiquieraLlamaAlRespaldoCuandoTodoVaBien() {
		GeneradorDeImagenes generador = new GeneradorDeImagenesConRespaldo(
				queResponde("primario"), queResponde("respaldo"));

		ImagenGenerada imagen = generador.generar("una app de pedidos");

		assertThat(imagen.base64()).isEqualTo("base64-de-primario");
		assertThat(llamados).containsExactly("primario");
	}

	@Test
	void cuandoElPrimarioFallaRespondeElRespaldo() {
		GeneradorDeImagenes generador = new GeneradorDeImagenesConRespaldo(
				queFalla("primario"), queResponde("respaldo"));

		ImagenGenerada imagen = generador.generar("una app de pedidos");

		assertThat(imagen.base64()).isEqualTo("base64-de-respaldo");
		assertThat(llamados).containsExactly("primario", "respaldo");
	}

	@Test
	void siAmbosFallanElVisitanteRecibeElErrorEstandarDeIndisponibilidad() {
		GeneradorDeImagenes generador = new GeneradorDeImagenesConRespaldo(
				queFalla("primario"), queFalla("respaldo"));

		assertThatThrownBy(() -> generador.generar("una app de pedidos"))
				.isInstanceOf(AsistenteNoDisponibleException.class);
		assertThat(llamados).containsExactly("primario", "respaldo");
	}

}
