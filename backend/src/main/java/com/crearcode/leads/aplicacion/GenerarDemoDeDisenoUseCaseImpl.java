package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeDemo;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.DemoSoloParaRegistradosException;
import com.crearcode.leads.dominio.GenerarDemoDeDisenoUseCase;
import com.crearcode.leads.dominio.GeneradorDeBocetos;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.ImagenGenerada;
import com.crearcode.leads.dominio.SolicitudDeDemo;

/**
 * Demo de diseño con IA (F10d, HU-42): SOLO registrados. Desde ISS-233
 * el boceto —título, funcionalidades y pantallas— lo arma
 * {@link GeneradorDeBocetos} (Claude con Groq de respaldo, decisión 33)
 * y, si {@code DEMO_INCLUIR_IMAGEN} está encendida, se le suma una
 * imagen de referencia mientras se decide si se conserva.
 *
 * <p>
 * Un fallo del boceto no consume cupo. Un fallo de la imagen no tumba
 * el demo: es complementaria, así que el boceto sale igual y sin ella.
 */
@Service
class GenerarDemoDeDisenoUseCaseImpl implements GenerarDemoDeDisenoUseCase {

	private static final Logger LOG = LoggerFactory.getLogger(GenerarDemoDeDisenoUseCaseImpl.class);

	private final GeneradorDeBocetos generadorBocetos;
	private final GeneradorDeImagenes generadorImagenes;
	private final CupoDeIa cupo;
	private final boolean incluirImagen;

	GenerarDemoDeDisenoUseCaseImpl(GeneradorDeBocetos generadorBocetos, GeneradorDeImagenes generadorImagenes,
			Clock reloj,
			@Value("${app.demo.limite-global-diario}") int limiteGlobalDiario,
			@Value("${app.demo.limite-diario-registrado}") int limiteDiarioRegistrado,
			@Value("${app.demo.incluir-imagen}") boolean incluirImagen) {
		this.generadorBocetos = generadorBocetos;
		this.generadorImagenes = generadorImagenes;
		// Sin cupo anonimo: el demo corta antes con
		// DemoSoloParaRegistradosException, asi que ese limite no se
		// alcanza nunca.
		this.cupo = new CupoDeIa(reloj, limiteGlobalDiario, limiteDiarioRegistrado, 0, 0);
		this.incluirImagen = incluirImagen;
	}

	@Override
	public BocetoDeDemo generar(SolicitudDeDemo solicitud, IdentidadDelVisitante identidad) {
		if (!identidad.registrada()) {
			throw new DemoSoloParaRegistradosException();
		}
		return cupo.ejecutar(identidad, () -> {
			BocetoDeSolucion solucion = generadorBocetos.generar(solicitud);
			return new BocetoDeDemo(solucion, incluirImagen ? imagenDeReferencia(solucion) : Optional.empty());
		});
	}

	private Optional<ImagenGenerada> imagenDeReferencia(BocetoDeSolucion solucion) {
		try {
			return Optional.of(generadorImagenes.generar(descripcionDeImagen(solucion.titulo())));
		} catch (AsistenteNoDisponibleException fallaDeLaImagen) {
			// El boceto ya está: sin imagen es mejor que sin nada.
			LOG.warn("La imagen de referencia del demo falló; el boceto sale sin ella", fallaDeLaImagen);
			return Optional.empty();
		}
	}

	/**
	 * Las instrucciones van en inglés (los modelos de imagen rinden
	 * bastante mejor). Lo único que se manda del negocio es el título
	 * que generó el modelo, nunca lo que escribió el cliente: el
	 * proveedor de imágenes recibe la descripción DENTRO DE LA URL —el
	 * path es justo lo que termina en los logs de acceso de medio
	 * internet— y en producción el respaldo es Pollinations, un servicio
	 * gratuito sin contrato ni acuerdo de tratamiento de datos (auditoría
	 * del 28 sep 2026, P1-8c). Se pide una pantalla LLENA de elementos:
	 * la versión anterior pedía "una sola pantalla" y "sin texto largo", y
	 * eso producía mockups casi vacíos (verificado contra Cloudflare
	 * Workers AI el 10 ago 2026). Para que no dibuje cifras de dinero se
	 * le dice QUÉ poner en cada fila —nombre, estado y hora— en vez de
	 * prohibirle precios: los modelos de difusión ignoran las negaciones.
	 */
	private static String descripcionDeImagen(String titulo) {
		return ("UI design mockup of a web app screen: %s. "
				+ "Modern clean interface showing a top navigation bar, a sidebar with menu items, "
				+ "content cards with short placeholder text, and a list where each row shows a person "
				+ "name, a colored status badge and a time. A primary action button. Professional SaaS "
				+ "dashboard style, sober colors, soft shadows, flat design, straight front view, "
				+ "full screen filled with interface elements.")
				.formatted(titulo);
	}

}
