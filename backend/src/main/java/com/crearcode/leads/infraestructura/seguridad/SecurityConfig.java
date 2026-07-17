package com.crearcode.leads.infraestructura.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API REST sin sesión: cada petición al panel admin se autentica por sí
 * misma con un JWT Bearer autoemitido (ver ADR-08 en
 * docs/02-arquitectura.md — reemplaza el HTTP Basic de la fase F2). Sin
 * CSRF ni sesiones HTTP, que solo tienen sentido con autenticación
 * basada en cookies. El healthcheck, el registro público de solicitudes
 * (POST) y el login (POST) no exigen autenticación; todo lo demás sí.
 */
@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, RateLimitingFilter rateLimitingFilter,
			JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter,
			ManejadorDeErroresDeSeguridad manejadorDeErrores) throws Exception {
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
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter)))
				.exceptionHandling(manejo -> manejo
						.authenticationEntryPoint(manejadorDeErrores)
						.accessDeniedHandler(manejadorDeErrores))
				.addFilterBefore(rateLimitingFilter, BearerTokenAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthoritiesClaimName("rol");
		authoritiesConverter.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
		return converter;
	}

}
