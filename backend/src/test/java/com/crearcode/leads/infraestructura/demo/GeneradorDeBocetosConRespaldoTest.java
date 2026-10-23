package com.crearcode.leads.infraestructura.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.GeneradorDeBocetos;
import com.crearcode.leads.dominio.SolicitudDeDemo;

/**
 * ISS-232: sin créditos o con Claude caído, el visitante igual recibe
 * su boceto. No hay tarjeta en la cuenta de Anthropic, así que al
 * acabarse el saldo las llamadas fallan en vez de cobrar.
 */
class GeneradorDeBocetosConRespaldoTest {

	private static final SolicitudDeDemo SOLICITUD = new SolicitudDeDemo("Restaurante", "Domicilios",
			"Pedidos ordenados");

	private final List<String> llamados = new ArrayList<>();

	private GeneradorDeBocetos queResponde(String nombre) {
		return solicitud -> {
			llamados.add(nombre);
			return LectorDeBoceto.leer(BocetoDePrueba.JSON.replace("App de pedidos para tu restaurante", "Boceto de " + nombre));
		};
	}

	private GeneradorDeBocetos queFalla(String nombre) {
		return solicitud -> {
			llamados.add(nombre);
			throw new AsistenteNoDisponibleException("falló " + nombre);
		};
	}

	@Test
	void usaElPrimarioYNiSiquieraLlamaAlRespaldoCuandoTodoVaBien() {
		BocetoDeSolucion boceto = new GeneradorDeBocetosConRespaldo(queResponde("claude"), queResponde("groq"))
				.generar(SOLICITUD);

		assertThat(boceto.titulo()).isEqualTo("Boceto de claude");
		assertThat(llamados).containsExactly("claude");
	}

	@Test
	void cuandoElPrimarioFallaRespondeElRespaldo() {
		BocetoDeSolucion boceto = new GeneradorDeBocetosConRespaldo(queFalla("claude"), queResponde("groq"))
				.generar(SOLICITUD);

		assertThat(boceto.titulo()).isEqualTo("Boceto de groq");
		assertThat(llamados).containsExactly("claude", "groq");
	}

	@Test
	void siFallanLosDosElVisitanteVeQueNoEstaDisponible() {
		GeneradorDeBocetos generador = new GeneradorDeBocetosConRespaldo(queFalla("claude"), queFalla("groq"));

		assertThatThrownBy(() -> generador.generar(SOLICITUD)).isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
