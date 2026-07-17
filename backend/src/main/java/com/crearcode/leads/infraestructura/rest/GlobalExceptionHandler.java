package com.crearcode.leads.infraestructura.rest;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.crearcode.leads.aplicacion.CredencialesInvalidasException;
import com.crearcode.leads.aplicacion.SolicitudNoEncontradaException;
import com.crearcode.leads.dominio.ConsentimientoRequeridoException;
import com.crearcode.leads.dominio.DatosDeContactoInvalidosException;
import com.crearcode.leads.dominio.TransicionDeEstadoInvalidaException;

/**
 * Traduce las excepciones de dominio y aplicación a respuestas HTTP
 * consistentes, sin stacktraces ni detalles internos. Las excepciones no
 * capturadas aquí siguen el manejo por defecto de Spring (500, sin
 * detalles expuestos).
 */
@RestControllerAdvice
class GlobalExceptionHandler {

	/**
	 * Maneja explícitamente los fallos de Bean Validation en vez de dejar
	 * que el {@code DefaultHandlerExceptionResolver} de Spring los
	 * resuelva: ese resolver registra en WARN el valor rechazado de cada
	 * campo, lo que filtraría datos de contacto reales en los logs si en
	 * el futuro se agrega una validación de formato (@Email, @Pattern)
	 * directamente en el DTO (ver ISS-036).
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> datosDeSolicitudInvalidos(MethodArgumentNotValidException excepcion) {
		String mensaje = excepcion.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(new ErrorResponse(mensaje));
	}

	@ExceptionHandler(DatosDeContactoInvalidosException.class)
	ResponseEntity<ErrorResponse> datosDeContactoInvalidos(DatosDeContactoInvalidosException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(ConsentimientoRequeridoException.class)
	ResponseEntity<ErrorResponse> consentimientoRequerido(ConsentimientoRequeridoException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(TransicionDeEstadoInvalidaException.class)
	ResponseEntity<ErrorResponse> transicionDeEstadoInvalida(TransicionDeEstadoInvalidaException excepcion) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(SolicitudNoEncontradaException.class)
	ResponseEntity<ErrorResponse> solicitudNoEncontrada(SolicitudNoEncontradaException excepcion) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	ResponseEntity<ErrorResponse> credencialesInvalidas(CredencialesInvalidasException excepcion) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(excepcion.getMessage()));
	}

}
