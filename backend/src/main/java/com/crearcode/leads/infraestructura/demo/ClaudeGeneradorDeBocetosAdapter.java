package com.crearcode.leads.infraestructura.demo;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Usage;
import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.GeneradorDeBocetos;
import com.crearcode.leads.dominio.SolicitudDeDemo;

/**
 * Claude arma el boceto del demo de diseño (decisión 33, ADR-16) con el
 * SDK oficial y salida estructurada: la respuesta cumple el esquema de
 * {@code boceto/esquema.json} y el dominio valida lo que el esquema no
 * puede garantizar.
 *
 * <ul>
 * <li><b>La clave es la del proyecto</b> y llega por constructor, nunca
 * del entorno: en la máquina del usuario {@code ANTHROPIC_API_KEY} es la
 * de otro proyecto, y leerla mezclaría el consumo.</li>
 * <li><b>Effort bajo y sin {@code thinking}</b>: el boceto es contenido,
 * no un problema que razonar, y así responde antes y gasta menos. Sirve
 * igual con Sonnet que con Opus, donde el pensamiento no se apaga.</li>
 * <li><b>Las instrucciones van en caché</b>: si dos bocetos se piden con
 * menos de cinco minutos de diferencia, el segundo las lee más barato.</li>
 * <li><b>Un solo reintento</b>: si Claude falla, el respaldo de Groq está
 * esperando y reintentar más solo alarga la espera del visitante.</li>
 * <li><b>Cada llamada deja en el log</b> el modelo, los tokens y el costo
 * estimado, sin nada de lo que escribió el visitante.</li>
 * </ul>
 */
class ClaudeGeneradorDeBocetosAdapter implements GeneradorDeBocetos {

	private static final Logger LOG = LoggerFactory.getLogger(ClaudeGeneradorDeBocetosAdapter.class);

	private final AnthropicClient cliente;
	private final String modelo;
	private final long maximoDeTokens;
	private final OutputConfig salidaEstructurada;
	private final List<TextBlockParam> instrucciones;

	ClaudeGeneradorDeBocetosAdapter(String urlBase, String apiKey, String modelo, long timeoutSegundos,
			long maximoDeTokens) {
		this.cliente = AnthropicOkHttpClient.builder()
				.baseUrl(urlBase)
				.apiKey(apiKey)
				.timeout(Duration.ofSeconds(timeoutSegundos))
				.maxRetries(1)
				.build();
		this.modelo = modelo;
		this.maximoDeTokens = maximoDeTokens;
		JsonOutputFormat.Schema.Builder esquema = JsonOutputFormat.Schema.builder();
		InstruccionesDeBoceto.ESQUEMA.forEach((clave, valor) -> esquema.putAdditionalProperty(clave, JsonValue.from(valor)));
		this.salidaEstructurada = OutputConfig.builder()
				.effort(OutputConfig.Effort.LOW)
				.format(JsonOutputFormat.builder().schema(esquema.build()).build())
				.build();
		this.instrucciones = List.of(TextBlockParam.builder()
				.text(InstruccionesDeBoceto.INSTRUCCIONES)
				.cacheControl(CacheControlEphemeral.builder().build())
				.build());
	}

	@Override
	public BocetoDeSolucion generar(SolicitudDeDemo solicitud) {
		long inicio = System.nanoTime();
		Message respuesta;
		try {
			respuesta = cliente.messages().create(MessageCreateParams.builder()
					.model(modelo)
					.maxTokens(maximoDeTokens)
					.systemOfTextBlockParams(instrucciones)
					.outputConfig(salidaEstructurada)
					.addUserMessage(InstruccionesDeBoceto.mensajeDelVisitante(solicitud))
					.build());
		} catch (AnthropicException excepcion) {
			// El detalle va a la causa (logs internos); jamás al visitante.
			throw new AsistenteNoDisponibleException("Fallo llamando a Claude", excepcion);
		}
		// Antes de mirar si terminó: una negativa también se cobra.
		registrarConsumo(respuesta, inicio);

		StopReason razon = respuesta.stopReason().orElse(null);
		if (StopReason.REFUSAL.equals(razon) || StopReason.MAX_TOKENS.equals(razon)) {
			// En esos dos casos la salida estructurada no garantiza el esquema.
			throw new AsistenteNoDisponibleException("Claude no terminó el boceto (" + razon + ")");
		}
		String texto = respuesta.content().stream()
				.flatMap(bloque -> bloque.text().stream())
				.map(TextBlock::text)
				.collect(Collectors.joining());
		return LectorDeBoceto.leer(texto);
	}

	private void registrarConsumo(Message respuesta, long inicio) {
		Usage uso = respuesta.usage();
		long cacheEscrita = uso.cacheCreationInputTokens().orElse(0L);
		long cacheLeida = uso.cacheReadInputTokens().orElse(0L);
		String costo = PreciosDeClaude
				.costoEstimado(modelo, uso.inputTokens(), uso.outputTokens(), cacheEscrita, cacheLeida)
				.map(valor -> String.format(Locale.ROOT, "%.4f", valor))
				.orElse("desconocido");
		LOG.info("Boceto con Claude: modelo={} entrada={} salida={} cacheEscrita={} cacheLeida={} "
				+ "costoEstimadoUsd={} segundos={}", modelo, uso.inputTokens(), uso.outputTokens(), cacheEscrita,
				cacheLeida, costo, String.format(Locale.ROOT, "%.1f", (System.nanoTime() - inicio) / 1e9));
	}

}
