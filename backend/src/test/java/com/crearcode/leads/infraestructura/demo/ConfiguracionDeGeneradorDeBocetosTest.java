package com.crearcode.leads.infraestructura.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.GeneradorDeBocetos;

/**
 * ISS-232: un solo lugar decide quién arma los bocetos, como con las
 * imágenes. Sin clave de Claude no se intenta Claude: sería una llamada
 * fallida y una espera de más en cada boceto.
 */
class ConfiguracionDeGeneradorDeBocetosTest {

	private GeneradorDeBocetos crear(String proveedor, String claveDeClaude) {
		return new ConfiguracionDeGeneradorDeBocetos().generadorDeBocetos(proveedor,
				"https://api.anthropic.com", claveDeClaude, "claude-sonnet-5-5", 60, 6000,
				"https://api.groq.com/openai/v1", "key-de-groq", "openai/gpt-oss-120b", 45);
	}

	@Test
	void conClaudeYSuClaveUsaClaudeConGroqDeRespaldo() {
		assertThat(crear("claude", "clave-del-proyecto")).isInstanceOf(GeneradorDeBocetosConRespaldo.class);
	}

	@Test
	void conClaudePeroSinClaveUsaSoloGroq() {
		assertThat(crear("claude", " ")).isInstanceOf(GroqGeneradorDeBocetosAdapter.class);
	}

	@Test
	void porDefectoUsaSoloGroq() {
		assertThat(crear("groq", "clave-del-proyecto")).isInstanceOf(GroqGeneradorDeBocetosAdapter.class);
		assertThat(crear("", "clave-del-proyecto")).isInstanceOf(GroqGeneradorDeBocetosAdapter.class);
	}

}
