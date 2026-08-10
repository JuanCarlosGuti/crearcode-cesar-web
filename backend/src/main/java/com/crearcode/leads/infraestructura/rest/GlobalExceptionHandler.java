package com.crearcode.leads.infraestructura.rest;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.crearcode.leads.aplicacion.CredencialesInvalidasException;
import com.crearcode.leads.aplicacion.CuentaNoVerificadaException;
import com.crearcode.leads.aplicacion.LimiteDeUsoAlcanzadoException;
import com.crearcode.leads.aplicacion.LimiteGlobalAlcanzadoException;
import com.crearcode.leads.aplicacion.SolicitudNoEncontradaException;
import com.crearcode.leads.aplicacion.UsuarioYaExisteException;
import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConsentimientoRequeridoException;
import com.crearcode.leads.dominio.ContrasenaInvalidaException;
import com.crearcode.leads.dominio.ConversacionInvalidaException;
import com.crearcode.leads.dominio.DatosDeContactoInvalidosException;
import com.crearcode.leads.dominio.DemoSoloParaRegistradosException;
import com.crearcode.leads.dominio.DiagnosticoInvalidoException;
import com.crearcode.leads.dominio.SolicitudDeDemoInvalidaException;
import com.crearcode.leads.dominio.MensajeDeChatInvalidoException;
import com.crearcode.leads.dominio.NegocioSimuladoInvalidoException;
import com.crearcode.leads.dominio.TokenDeCuentaInvalidoException;
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

	@ExceptionHandler(CuentaNoVerificadaException.class)
	ResponseEntity<ErrorResponse> cuentaNoVerificada(CuentaNoVerificadaException excepcion) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(UsuarioYaExisteException.class)
	ResponseEntity<ErrorResponse> usuarioYaExiste(UsuarioYaExisteException excepcion) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(TokenDeCuentaInvalidoException.class)
	ResponseEntity<ErrorResponse> tokenDeCuentaInvalido(TokenDeCuentaInvalidoException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(ContrasenaInvalidaException.class)
	ResponseEntity<ErrorResponse> contrasenaInvalida(ContrasenaInvalidaException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler({ MensajeDeChatInvalidoException.class, ConversacionInvalidaException.class,
			NegocioSimuladoInvalidoException.class, DiagnosticoInvalidoException.class,
			SolicitudDeDemoInvalidaException.class })
	ResponseEntity<ErrorResponse> conversacionInvalida(RuntimeException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(LimiteDeUsoAlcanzadoException.class)
	ResponseEntity<ErrorAsistenteResponse> limiteDeUsoDelAsistente(LimiteDeUsoAlcanzadoException excepcion) {
		String codigo = excepcion.identidadRegistrada() ? "limite-registrado" : "limite-anonimo";
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(new ErrorAsistenteResponse(excepcion.getMessage(), codigo));
	}

	/**
	 * El límite global agotado y el fallo del proveedor son lo mismo para
	 * el visitante: asistente no disponible, con la alternativa humana
	 * (invariante 3 del contexto asistente).
	 */
	@ExceptionHandler(DemoSoloParaRegistradosException.class)
	ResponseEntity<ErrorAsistenteResponse> demoSoloParaRegistrados(DemoSoloParaRegistradosException excepcion) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ErrorAsistenteResponse(excepcion.getMessage(), "solo-registrados"));
	}

	@ExceptionHandler({ LimiteGlobalAlcanzadoException.class, AsistenteNoDisponibleException.class })
	ResponseEntity<ErrorAsistenteResponse> asistenteNoDisponible(RuntimeException excepcion) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(new ErrorAsistenteResponse("El asistente no está disponible en este momento",
						"no-disponible"));
	}

}
