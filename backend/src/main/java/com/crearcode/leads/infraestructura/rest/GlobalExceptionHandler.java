package com.crearcode.leads.infraestructura.rest;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.crearcode.leads.aplicacion.CotizacionNoEncontradaException;
import com.crearcode.leads.aplicacion.CredencialesInvalidasException;
import com.crearcode.leads.aplicacion.CuentaNoVerificadaException;
import com.crearcode.leads.aplicacion.EliminacionNoPermitidaException;
import com.crearcode.leads.aplicacion.LimiteDeUsoAlcanzadoException;
import com.crearcode.leads.aplicacion.LimiteGlobalAlcanzadoException;
import com.crearcode.leads.aplicacion.SolicitudNoEncontradaException;
import com.crearcode.leads.aplicacion.UsuarioNoEncontradoException;
import com.crearcode.leads.aplicacion.UsuarioYaExisteException;
import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConsentimientoRequeridoException;
import com.crearcode.leads.dominio.ContrasenaInvalidaException;
import com.crearcode.leads.dominio.ConversacionInvalidaException;
import com.crearcode.leads.dominio.CotizacionInvalidaException;
import com.crearcode.leads.dominio.MontoInvalidoException;
import com.crearcode.leads.dominio.PorcentajeInvalidoException;
import com.crearcode.leads.dominio.CotizacionVencidaException;
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

	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

	/**
	 * También cubre el caso de una cotización ajena: el caso de uso la
	 * trata como inexistente para no revelar que existe (invariante 6 del
	 * contexto de cotizaciones).
	 */
	@ExceptionHandler(CotizacionNoEncontradaException.class)
	ResponseEntity<ErrorResponse> cotizacionNoEncontrada(CotizacionNoEncontradaException excepcion) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(CotizacionInvalidaException.class)
	ResponseEntity<ErrorResponse> cotizacionInvalida(CotizacionInvalidaException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	/**
	 * Monto o porcentaje imposibles, vengan de una cotización o de un
	 * proyecto: el mismo 400 que respondía cuando eran "cotización
	 * inválida", para que ningún cliente de la API note el cambio.
	 */
	@ExceptionHandler({ MontoInvalidoException.class, PorcentajeInvalidoException.class })
	ResponseEntity<ErrorResponse> valorInvalido(RuntimeException excepcion) {
		return ResponseEntity.badRequest().body(new ErrorResponse(excepcion.getMessage()));
	}

	/** Vencida: el estado era correcto, lo que falló fue el plazo. */
	@ExceptionHandler(CotizacionVencidaException.class)
	ResponseEntity<ErrorResponse> cotizacionVencida(CotizacionVencidaException excepcion) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(excepcion.getMessage()));
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

	@ExceptionHandler(DemoSoloParaRegistradosException.class)
	ResponseEntity<ErrorAsistenteResponse> demoSoloParaRegistrados(DemoSoloParaRegistradosException excepcion) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(new ErrorAsistenteResponse(excepcion.getMessage(), "solo-registrados"));
	}

	/**
	 * Cupo diario de la casa agotado: no es una avería, es el techo de la
	 * capa gratis del proveedor. Vuelve solo al día siguiente, así que va
	 * a INFO y con su propio código — un monitor que alerte sobre
	 * {@code proveedor-caido} no debe despertar a nadie por esto.
	 */
	@ExceptionHandler(LimiteGlobalAlcanzadoException.class)
	ResponseEntity<ErrorAsistenteResponse> limiteGlobalAlcanzado(LimiteGlobalAlcanzadoException excepcion) {
		LOG.info("Cupo global diario de IA agotado: {}", excepcion.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(new ErrorAsistenteResponse("El cupo de hoy para esta herramienta ya se agotó",
						"limite-global"));
	}

	/**
	 * El proveedor de IA falló: esto SÍ es una avería. El 28 sep 2026
	 * Groq retiró el modelo configurado, las tres herramientas
	 * respondieron 503 durante días, y como el cupo agotado y el
	 * proveedor caído compartían código y no había ni una línea de log,
	 * desde fuera era indistinguible de un día de mucho tráfico. Se
	 * registra la excepción y su causa (estado HTTP y cuerpo del
	 * proveedor, que es lo que trae RestClientException), nunca la
	 * conversación del visitante.
	 */
	@ExceptionHandler(AsistenteNoDisponibleException.class)
	ResponseEntity<ErrorAsistenteResponse> proveedorDeIaCaido(AsistenteNoDisponibleException excepcion) {
		Throwable causa = excepcion.getCause();
		LOG.warn("Proveedor de IA caído — {}: {}{}", excepcion.getClass().getSimpleName(),
				excepcion.getMessage(), causa == null ? "" : " | causa: " + causa.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(new ErrorAsistenteResponse("El asistente no está disponible en este momento",
						"proveedor-caido"));
	}

	@ExceptionHandler(UsuarioNoEncontradoException.class)
	ResponseEntity<ErrorResponse> usuarioNoEncontrado(UsuarioNoEncontradoException excepcion) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(excepcion.getMessage()));
	}

	@ExceptionHandler(EliminacionNoPermitidaException.class)
	ResponseEntity<ErrorResponse> eliminacionNoPermitida(EliminacionNoPermitidaException excepcion) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(excepcion.getMessage()));
	}

}
