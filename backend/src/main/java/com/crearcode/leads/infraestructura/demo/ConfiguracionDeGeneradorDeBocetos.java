package com.crearcode.leads.infraestructura.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.crearcode.leads.dominio.GeneradorDeBocetos;

/**
 * Único punto donde se decide quién arma los bocetos del demo de diseño
 * (decisión 33), con la variable {@code DEMO_PROVEEDOR_BOCETOS}:
 *
 * <ul>
 * <li>{@code claude} — Claude como primario y Groq como respaldo
 * automático (producción). Sin {@code CLAUDE_API_KEY} no se intenta
 * Claude: sería una llamada fallida y una espera de más en cada
 * boceto.</li>
 * <li>cualquier otro valor o ausencia — solo Groq (el default, para que
 * en local y en CI nada gaste créditos).</li>
 * </ul>
 *
 * Las credenciales llegan por entorno y jamás se loguean.
 */
@Configuration
class ConfiguracionDeGeneradorDeBocetos {

	private static final Logger LOG = LoggerFactory.getLogger(ConfiguracionDeGeneradorDeBocetos.class);

	@Bean
	GeneradorDeBocetos generadorDeBocetos(
			@Value("${app.demo.bocetos.proveedor}") String proveedor,
			@Value("${app.demo.claude.url}") String claudeUrl,
			@Value("${app.demo.claude.key}") String claudeKey,
			@Value("${app.demo.claude.modelo}") String claudeModelo,
			@Value("${app.demo.claude.timeout-segundos}") long claudeTimeout,
			@Value("${app.demo.claude.max-tokens}") long claudeMaximoDeTokens,
			@Value("${app.asistente.groq.url}") String groqUrl,
			@Value("${app.asistente.groq.key}") String groqKey,
			@Value("${app.asistente.groq.modelo}") String groqModelo,
			@Value("${app.demo.groq.timeout-segundos}") long groqTimeout) {

		GeneradorDeBocetos groq = new GroqGeneradorDeBocetosAdapter(groqUrl, groqKey, groqModelo, groqTimeout);
		if (!"claude".equals(proveedor)) {
			return groq;
		}
		if (claudeKey == null || claudeKey.isBlank()) {
			LOG.error("DEMO_PROVEEDOR_BOCETOS=claude pero falta CLAUDE_API_KEY: los bocetos los arma Groq.");
			return groq;
		}
		return new GeneradorDeBocetosConRespaldo(
				new ClaudeGeneradorDeBocetosAdapter(claudeUrl, claudeKey, claudeModelo, claudeTimeout,
						claudeMaximoDeTokens),
				groq);
	}

}
