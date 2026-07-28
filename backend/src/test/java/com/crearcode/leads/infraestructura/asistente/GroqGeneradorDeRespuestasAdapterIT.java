package com.crearcode.leads.infraestructura.asistente;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.MensajeDeChat;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RolDeMensaje;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * IT contra un stub HTTP local (HttpServer del JDK): determinista, sin
 * gastar cuota de Groq ni exponer la key — la prueba con el proveedor
 * real es manual, al cierre de la fase (ISS-118).
 */
class GroqGeneradorDeRespuestasAdapterIT {

	private static HttpServer stub;
	private static int puerto;

	private static final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
	private static final AtomicReference<String> autorizacionRecibida = new AtomicReference<>();
	private static final AtomicReference<Integer> statusARetornar = new AtomicReference<>(200);
	private static final AtomicReference<String> respuestaARetornar = new AtomicReference<>("");

	@BeforeAll
	static void levantarStub() throws IOException {
		stub = HttpServer.create(new InetSocketAddress(0), 0);
		stub.createContext("/openai/v1/chat/completions", intercambio -> {
			cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			autorizacionRecibida.set(intercambio.getRequestHeaders().getFirst("Authorization"));
			byte[] cuerpo = respuestaARetornar.get().getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "application/json");
			intercambio.sendResponseHeaders(statusARetornar.get(), cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});
		stub.start();
		puerto = stub.getAddress().getPort();
	}

	@AfterAll
	static void apagarStub() {
		stub.stop(0);
	}

	@BeforeEach
	void reiniciarStub() {
		statusARetornar.set(200);
		respuestaARetornar.set(respuestaGroq("Con gusto te cuento de nuestros servicios."));
		cuerpoRecibido.set(null);
	}

	private static String respuestaGroq(String contenido) {
		return """
				{"choices":[{"message":{"role":"assistant","content":"%s"}}]}
				""".formatted(contenido.replace("\"", "\\\""));
	}

	private GroqGeneradorDeRespuestasAdapter crearAdaptador() {
		return new GroqGeneradorDeRespuestasAdapter(
				"http://localhost:" + puerto + "/openai/v1", "key-de-prueba", "modelo-de-prueba", 5);
	}

	private static ConversacionDeAsistente pregunta(String texto) {
		return new ConversacionDeAsistente(List.of(new MensajeDeChat(RolDeMensaje.USUARIO, texto)));
	}

	@Test
	void enviaElPromptDeSistemaYLaConversacionEnFormatoOpenAi() {
		RespuestaDelAsistente respuesta = crearAdaptador().responder(pregunta("¿Qué servicios ofrecen?"));

		assertThat(respuesta.texto()).isEqualTo("Con gusto te cuento de nuestros servicios.");
		assertThat(respuesta.escalarAHumano()).isFalse();
		assertThat(cuerpoRecibido.get()).contains("\"role\":\"system\"");
		assertThat(cuerpoRecibido.get()).contains("NUNCA inventes precios");
		assertThat(cuerpoRecibido.get()).contains("¿Qué servicios ofrecen?");
		assertThat(cuerpoRecibido.get()).contains("\"model\":\"modelo-de-prueba\"");
		assertThat(autorizacionRecibida.get()).isEqualTo("Bearer key-de-prueba");
	}

	@Test
	void elMarcadorDeEscalamientoActivaLaBanderaYSeRetiraDelTexto() {
		respuestaARetornar.set(respuestaGroq(
				"Cada proyecto se cotiza a la medida. Escríbenos por WhatsApp.\\n[ESCALAR]"));

		RespuestaDelAsistente respuesta = crearAdaptador().responder(pregunta("¿Cuánto cuesta una app?"));

		assertThat(respuesta.escalarAHumano()).isTrue();
		assertThat(respuesta.texto()).doesNotContain("[ESCALAR]");
		assertThat(respuesta.texto()).contains("se cotiza a la medida");
	}

	@Test
	void unErrorDelProveedorSeTraduceAAsistenteNoDisponible() {
		statusARetornar.set(500);
		respuestaARetornar.set("{\"error\":\"interno\"}");

		assertThatThrownBy(() -> crearAdaptador().responder(pregunta("hola")))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

	@Test
	void unaRespuestaSinContenidoSeTraduceAAsistenteNoDisponible() {
		respuestaARetornar.set("{\"choices\":[]}");

		assertThatThrownBy(() -> crearAdaptador().responder(pregunta("hola")))
				.isInstanceOf(AsistenteNoDisponibleException.class);
	}

}
