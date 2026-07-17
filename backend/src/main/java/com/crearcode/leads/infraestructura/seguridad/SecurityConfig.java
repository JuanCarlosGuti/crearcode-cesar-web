package com.crearcode.leads.infraestructura.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * API REST sin sesión: cada petición al panel admin se autentica por sí
 * misma con HTTP Basic contra un único usuario (credenciales por
 * variable de entorno, sin registro ni recuperación de contraseña en
 * v1). Sin CSRF ni sesiones HTTP, que solo tienen sentido con
 * autenticación basada en cookies. El healthcheck y el registro público
 * de solicitudes (POST) no exigen autenticación; todo lo demás sí.
 */
@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, RateLimitingFilter rateLimitingFilter) throws Exception {
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
						.anyRequest().authenticated())
				.httpBasic(basic -> {
				})
				.addFilterBefore(rateLimitingFilter, BasicAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder,
			@Value("${app.admin.username}") String usuario, @Value("${app.admin.password}") String contrasena) {
		UserDetails admin = User.withUsername(usuario)
				.password(passwordEncoder.encode(contrasena))
				.roles("ADMIN")
				.build();
		return new InMemoryUserDetailsManager(admin);
	}

}
