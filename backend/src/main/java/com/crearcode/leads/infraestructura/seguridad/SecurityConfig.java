package com.crearcode.leads.infraestructura.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API REST sin sesión (cada endpoint protegido se autentica por
 * petición, ver el login de administrador en ISS-035): sin CSRF ni
 * sesiones HTTP, que solo tienen sentido con autenticación basada en
 * cookies. El healthcheck y el registro público de solicitudes (POST)
 * no exigen autenticación; todo lo demás sí.
 */
@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(autorizacion -> autorizacion
						.requestMatchers("/actuator/health").permitAll()
						// Spring MVC reenvia aqui internamente al resolver
						// errores (ej. de Bean Validation); si no fuera
						// publica, todo error terminaria devolviendo 403
						// en vez del status real.
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/solicitudes").permitAll()
						.anyRequest().authenticated());
		return http.build();
	}

}
