package com.crearcode.leads.aplicacion;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Único punto donde la aplicación decide qué reloj usar. El dominio nunca
 * llama a {@code Instant.now()} directamente (ver invariante 7 en
 * docs/03-modelo-de-dominio.md); los casos de uso reciben este
 * {@link Clock} inyectado.
 */
@Configuration
class RelojConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
