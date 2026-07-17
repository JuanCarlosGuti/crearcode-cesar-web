package com.crearcode.leads.infraestructura.rest;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.ServicioDeInteres;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Por defecto, cuando Spring resuelve una {@code MethodArgumentNotValidException}
 * con su {@code DefaultHandlerExceptionResolver}, registra en WARN el
 * valor rechazado de cada campo. Con las validaciones actuales
 * (@NotBlank/@NotNull) ese valor siempre es vacío, pero el patrón es
 * frágil: si en el futuro se agrega una validación de formato
 * (@Email, @Pattern) directamente en el DTO, un correo o teléfono real
 * quedaría en el log. Por eso {@link GlobalExceptionHandler} maneja esa
 * excepción explícitamente, evitando que el resolver por defecto
 * (y su log) llegue a intervenir.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class LoggingSinDatosPersonalesIT {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void unaSolicitudInvalidaNoDisparaElLogPorDefectoConValoresRechazados() {
		Logger logRaiz = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logRaiz.addAppender(appender);

		try {
			SolicitudRequest invalida = new SolicitudRequest("", null, "nombre@empresa.com", "3001234567",
					ServicioDeInteres.OTRO, "mensaje", true, null);

			restTemplate.postForEntity("/api/solicitudes", invalida, String.class);

			boolean intervinoElResolverPorDefecto = appender.list.stream()
					.anyMatch(evento -> evento.getLoggerName().contains("DefaultHandlerExceptionResolver"));

			assertThat(intervinoElResolverPorDefecto).isFalse();
		} finally {
			logRaiz.detachAppender(appender);
		}
	}

}
