package com.crearcode.leads.infraestructura.notificacion;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Transporte HTTP de Resend contra un stub del JDK — sin red externa ni
 * API key real. Existe porque Render bloquea los puertos SMTP salientes
 * en el plan gratuito y este camino (443) nunca se bloquea.
 */
class TransporteResendIT {

	private static HttpServer stub;
	private static final AtomicReference<Integer> status = new AtomicReference<>(200);
	private static final AtomicReference<String> ultimaRuta = new AtomicReference<>("");
	private static final AtomicReference<String> ultimaAutorizacion = new AtomicReference<>("");
	private static final AtomicReference<String> ultimoCuerpo = new AtomicReference<>("");

	private TransporteResend transporte;

	@BeforeAll
	static void levantarStub() throws IOException {
		stub = HttpServer.create(new InetSocketAddress(0), 0);
		stub.createContext("/", intercambio -> {
			ultimaRuta.set(intercambio.getRequestURI().getPath());
			ultimaAutorizacion.set(String.valueOf(intercambio.getRequestHeaders().getFirst("Authorization")));
			ultimoCuerpo.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

			byte[] cuerpo = "{\"id\":\"correo-de-prueba\"}".getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(status.get(), cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stub.start();
	}

	@AfterAll
	static void apagarStub() {
		stub.stop(0);
	}

	@BeforeEach
	void preparar() {
		status.set(200);
		transporte = new TransporteResend("http://localhost:" + stub.getAddress().getPort(),
				"re_clave-de-prueba",
				new RemitenteDeCorreo("Crear Code Cesar <contacto@crearcodecesar.com>",
						"contacto@crearcodecesar.com"),
				10);
	}

	@Test
	void envioUnCorreoSimpleConElRemitenteYLaRespuestaDelDominioPropio() {
		transporte.enviar(CorreoSaliente.simple("cliente@empresa.com", "Asunto de prueba", "Cuerpo"));

		assertThat(ultimaRuta.get()).isEqualTo("/emails");
		assertThat(ultimaAutorizacion.get()).isEqualTo("Bearer re_clave-de-prueba");
		assertThat(ultimoCuerpo.get()).contains("\"from\":\"Crear Code Cesar <contacto@crearcodecesar.com>\"");
		assertThat(ultimoCuerpo.get()).contains("\"reply_to\":[\"contacto@crearcodecesar.com\"]");
		assertThat(ultimoCuerpo.get()).contains("\"to\":[\"cliente@empresa.com\"]");
		assertThat(ultimoCuerpo.get()).contains("Cuerpo");
	}

	@Test
	void elPdfDeLaCotizacionViajaComoAdjuntoEnBase64() {
		byte[] pdf = "%PDF-falso".getBytes(StandardCharsets.UTF_8);

		transporte.enviar(new CorreoSaliente("cliente@empresa.com", "Tu cotización", "Adjunta va",
				new CorreoSaliente.Adjunto("COT-2026-0001.pdf", pdf)));

		assertThat(ultimoCuerpo.get()).contains("\"filename\":\"COT-2026-0001.pdf\"");
		assertThat(ultimoCuerpo.get()).contains(Base64.getEncoder().encodeToString(pdf));
	}

	// 422 es lo que responde Resend cuando el remitente no pertenece a un
	// dominio verificado: debe traducirse a un fallo claro, no propagarse.
	@Test
	void unRechazoDelProveedorSeTraduceAFalloDeEnvio() {
		status.set(422);

		assertThatThrownBy(() -> transporte.enviar(
				CorreoSaliente.simple("cliente@empresa.com", "Asunto", "Cuerpo")))
				.isInstanceOf(EnvioDeCorreoFallidoException.class);
	}

}
