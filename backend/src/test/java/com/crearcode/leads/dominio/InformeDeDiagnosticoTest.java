package com.crearcode.leads.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class InformeDeDiagnosticoTest {

	private static OportunidadDeAutomatizacion oportunidad() {
		return new OportunidadDeAutomatizacion("Respuestas automáticas",
				"Las preguntas repetidas se contestan solas.", "Dejas de contestar lo mismo todo el día.");
	}

	@Test
	void unInformeValidoTieneVeredictoYTresOportunidades() {
		InformeDeDiagnostico informe = new InformeDeDiagnostico("  Tu negocio tiene un problema de tiempo.  ",
				List.of(oportunidad(), oportunidad(), oportunidad()));

		assertThat(informe.veredicto()).isEqualTo("Tu negocio tiene un problema de tiempo.");
		assertThat(informe.oportunidades()).hasSize(3);
	}

	@Test
	void rechazaVeredictoVacio() {
		assertThatThrownBy(() -> new InformeDeDiagnostico("  ",
				List.of(oportunidad(), oportunidad(), oportunidad())))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

	@Test
	void rechazaUnNumeroDeOportunidadesDistintoDeTres() {
		assertThatThrownBy(() -> new InformeDeDiagnostico("Veredicto", List.of(oportunidad())))
				.isInstanceOf(DiagnosticoInvalidoException.class);
		assertThatThrownBy(() -> new InformeDeDiagnostico("Veredicto", Collections.emptyList()))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

	@Test
	void unaOportunidadRechazaCamposVacios() {
		assertThatThrownBy(() -> new OportunidadDeAutomatizacion(" ", "detalle", "beneficio"))
				.isInstanceOf(DiagnosticoInvalidoException.class);
	}

}
