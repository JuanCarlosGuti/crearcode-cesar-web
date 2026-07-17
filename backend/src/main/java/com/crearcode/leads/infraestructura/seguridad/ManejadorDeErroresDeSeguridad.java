package com.crearcode.leads.infraestructura.seguridad;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.crearcode.leads.infraestructura.rest.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Sin esto, un token invalido/expirado o un intento de acceso sin
 * autorizacion suficiente devuelven el 401/403 por defecto de Spring
 * Security (con un cuerpo inconsistente con el resto de la API):
 * {@code @RestControllerAdvice} no ve las excepciones que se lanzan
 * dentro de la cadena de filtros de seguridad, antes del
 * {@code DispatcherServlet}.
 */
@Component
class ManejadorDeErroresDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	ManejadorDeErroresDeSeguridad(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		escribirError(response, HttpStatus.UNAUTHORIZED, "No autenticado");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escribirError(response, HttpStatus.FORBIDDEN, "No autorizado");
	}

	private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), new ErrorResponse(mensaje));
	}

}
