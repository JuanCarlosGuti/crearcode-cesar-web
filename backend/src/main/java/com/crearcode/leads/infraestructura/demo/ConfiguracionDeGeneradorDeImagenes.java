package com.crearcode.leads.infraestructura.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.crearcode.leads.dominio.GeneradorDeImagenes;

/**
 * Único punto donde se decide qué proveedor de imágenes usa el demo de
 * diseño, con la variable {@code DEMO_PROVEEDOR_IMAGENES}:
 *
 * <ul>
 * <li>{@code cloudflare} — Workers AI como primario y Pollinations como
 * respaldo automático (configuración de producción desde ISS-137).</li>
 * <li>{@code gemini} — Gemini Flash Image; su capa gratis no incluía
 * imágenes en ago 2026, queda listo para el día que haya billing.</li>
 * <li>cualquier otro valor o ausencia — Pollinations solo (default).</li>
 * </ul>
 *
 * Las credenciales llegan por entorno y jamás se loguean.
 */
@Configuration
class ConfiguracionDeGeneradorDeImagenes {

	@Bean
	GeneradorDeImagenes generadorDeImagenes(
			@Value("${app.demo.proveedor-imagenes}") String proveedor,
			@Value("${app.demo.pollinations.url}") String pollinationsUrl,
			@Value("${app.demo.pollinations.timeout-segundos}") long pollinationsTimeout,
			@Value("${app.demo.gemini.url}") String geminiUrl,
			@Value("${app.demo.gemini.key}") String geminiKey,
			@Value("${app.demo.gemini.modelo}") String geminiModelo,
			@Value("${app.demo.gemini.timeout-segundos}") long geminiTimeout,
			@Value("${app.demo.cloudflare.url}") String cloudflareUrl,
			@Value("${app.demo.cloudflare.cuenta}") String cloudflareCuenta,
			@Value("${app.demo.cloudflare.token}") String cloudflareToken,
			@Value("${app.demo.cloudflare.modelo}") String cloudflareModelo,
			@Value("${app.demo.cloudflare.timeout-segundos}") long cloudflareTimeout) {

		GeneradorDeImagenes pollinations = new PollinationsGeneradorDeImagenesAdapter(pollinationsUrl,
				pollinationsTimeout);

		return switch (proveedor) {
			case "gemini" -> new GeminiGeneradorDeImagenesAdapter(geminiUrl, geminiKey, geminiModelo, geminiTimeout);
			case "cloudflare" -> new GeneradorDeImagenesConRespaldo(
					new CloudflareGeneradorDeImagenesAdapter(cloudflareUrl, cloudflareCuenta, cloudflareToken,
							cloudflareModelo, cloudflareTimeout),
					pollinations);
			default -> pollinations;
		};
	}

}
