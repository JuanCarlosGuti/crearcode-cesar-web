package com.crearcode.leads.infraestructura.asistente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.crearcode.leads.dominio.GeneradorDeRespuestas;

/**
 * Único punto donde se decide qué modelo de texto usa la IA del sitio,
 * igual que {@code ConfiguracionDeGeneradorDeImagenes} hace con las
 * imágenes.
 *
 * <p>
 * Con {@code GROQ_MODELO_RESPALDO} definido y distinto del primario, el
 * puerto se monta con {@link GeneradorDeRespuestasConRespaldo}: si el
 * primario falla, contesta el segundo modelo. Vacío o igual al
 * primario, se monta el adaptador solo.
 *
 * <p>
 * Esto es exactamente lo que faltó el 28 sep 2026: Groq retiró
 * {@code llama-3.3-70b-versatile}, la API devolvió
 * {@code 404 model_not_found} y las tres herramientas de texto
 * estuvieron caídas días, cuando otro modelo del mismo proveedor
 * respondía perfectamente.
 *
 * <p>
 * Aviso honesto sobre su alcance: los dos modelos viven en Groq, así
 * que esto cubre el modelo retirado, el saturado y el que falla — no
 * cubre que Groq entero se caiga. Un respaldo de otro proveedor exige
 * una segunda credencial; hasta que exista, esa caída sigue terminando
 * en el 503 con la salida a WhatsApp.
 */
@Configuration
class ConfiguracionDeGeneradorDeRespuestas {

	@Bean
	GeneradorDeRespuestas generadorDeRespuestas(
			@Value("${app.asistente.groq.url}") String url,
			@Value("${app.asistente.groq.key}") String key,
			@Value("${app.asistente.groq.modelo}") String modelo,
			@Value("${app.asistente.groq.modelo-respaldo}") String modeloRespaldo,
			@Value("${app.asistente.groq.timeout-segundos}") long timeoutSegundos) {

		GeneradorDeRespuestas primario = new GroqGeneradorDeRespuestasAdapter(url, key, modelo, timeoutSegundos);
		if (modeloRespaldo.isBlank() || modeloRespaldo.equals(modelo)) {
			return primario;
		}
		return new GeneradorDeRespuestasConRespaldo(primario,
				new GroqGeneradorDeRespuestasAdapter(url, key, modeloRespaldo, timeoutSegundos));
	}

}
