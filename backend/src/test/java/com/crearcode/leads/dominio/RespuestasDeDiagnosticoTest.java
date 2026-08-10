package com.crearcode.leads.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class RespuestasDeDiagnosticoTest {

	@Test
	void normalizaYConservaLosPares() {
		RespuestasDeDiagnostico respuestas = new RespuestasDeDiagnostico(List.of(
				new ParDeDiagnostico("  ¿Cómo reciben pedidos?  ", "  WhatsApp y llamadas  ")));

		assertThat(respuestas.pares()).hasSize(1);
		assertThat(respuestas.pares().getFirst().pregunta()).isEqualTo("¿Cómo reciben pedidos?");
		assertThat(respuestas.pares().getFirst().respuesta()).isEqualTo("WhatsApp y llamadas");
	}

	@Test
	void rechazaUnCuestionarioVacio() {
		assertThatThrownBy(() -> new RespuestasDeDiagnostico(Collections.emptyList()))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

	@Test
	void rechazaMasDe10Pares() {
		List<ParDeDiagnostico> pares = Collections.nCopies(11,
				new ParDeDiagnostico("¿Pregunta?", "Respuesta"));

		assertThatThrownBy(() -> new RespuestasDeDiagnostico(pares))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

	@Test
	void rechazaPreguntaORespuestaVacias() {
		assertThatThrownBy(() -> new ParDeDiagnostico("  ", "Respuesta"))
				.isInstanceOf(DiagnosticoInvalidoException.class);
		assertThatThrownBy(() -> new ParDeDiagnostico("¿Pregunta?", " "))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

	@Test
	void rechazaTextosDemasiadoLargos() {
		assertThatThrownBy(() -> new ParDeDiagnostico("x".repeat(201), "Respuesta"))
				.isInstanceOf(DiagnosticoInvalidoException.class);
		assertThatThrownBy(() -> new ParDeDiagnostico("¿Pregunta?", "x".repeat(121)))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

}
