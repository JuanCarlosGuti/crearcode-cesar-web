package com.crearcode.leads.infraestructura.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración mínima de seguridad para la fase F0: solo abre el
 * healthcheck sin autenticación (lo necesitan orquestadores/monitoreo) y
 * exige autenticación para todo lo demás por defecto. El login de
 * administrador (usuario único) se define en la fase F2.
 */
@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(autorizacion -> autorizacion
				.requestMatchers("/actuator/health").permitAll()
				.anyRequest().authenticated());
		return http.build();
	}

}
