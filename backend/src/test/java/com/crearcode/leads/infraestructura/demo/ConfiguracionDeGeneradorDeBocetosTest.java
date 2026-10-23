package com.crearcode.leads.infraestructura.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

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

	/**
	 * Si el despliegue enciende Claude, su clave llega como secreto por las
	 * tres puertas —el YAML, .kamal/secrets y el workflow—, nunca como valor
	 * en claro. Y fuera del despliegue el proveedor por defecto sigue siendo
	 * Groq, para que ni en local ni en CI nada gaste créditos.
	 */
	@Test
	void siElDespliegueEnciendeClaudeSuClaveLlegaComoSecreto() throws Exception {
		String despliegue = Files.readString(Path.of("..", "config", "deploy.api.yml"));
		if (despliegue.contains("DEMO_PROVEEDOR_BOCETOS: claude")) {
			String secretos = despliegue.substring(despliegue.indexOf("secret:"));
			assertThat(secretos).contains("- CLAUDE_API_KEY");
			assertThat(despliegue.substring(0, despliegue.indexOf("secret:"))).doesNotContain("CLAUDE_API_KEY:");
			assertThat(Files.readString(Path.of("..", ".kamal", "secrets"))).contains("CLAUDE_API_KEY=$CLAUDE_API_KEY");
			assertThat(Files.readString(Path.of("..", ".github", "workflows", "ci.yml")))
					.contains("CLAUDE_API_KEY: ${{ secrets.CLAUDE_API_KEY }}");
		}
		assertThat(Files.readString(Path.of("src", "main", "resources", "application.properties")))
				.contains("app.demo.bocetos.proveedor=${DEMO_PROVEEDOR_BOCETOS:groq}");
	}

}
