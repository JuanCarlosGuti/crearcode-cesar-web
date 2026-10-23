package com.crearcode.leads.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeDemo;
import com.crearcode.leads.dominio.BocetoDeSolucion;
import com.crearcode.leads.dominio.DemoSoloParaRegistradosException;
import com.crearcode.leads.dominio.ElementoDePieza;
import com.crearcode.leads.dominio.GeneradorDeBocetos;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.ImagenGenerada;
import com.crearcode.leads.dominio.PantallaDeBoceto;
import com.crearcode.leads.dominio.PiezaDeBoceto;
import com.crearcode.leads.dominio.SolicitudDeDemo;
import com.crearcode.leads.dominio.TipoDePieza;

/**
 * El demo de diseño desde ISS-233: el boceto lo arma el puerto
 * {@link GeneradorDeBocetos} (Claude con Groq de respaldo) y la imagen
 * de Cloudflare/Pollinations es opcional y complementaria mientras se
 * decide si se conserva (decisión 33).
 */
class GenerarDemoDeDisenoUseCaseImplTest {

	private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-23T10:00:00Z"), ZoneOffset.UTC);

	private static final BocetoDeSolucion BOCETO = new BocetoDeSolucion("App de pedidos para tu restaurante",
			List.of("Menú del día", "Pedidos por WhatsApp", "Estado del domicilio", "Historial de clientes",
					"Aviso de pedido listo"),
			List.of(pantalla("Inicio"), pantalla("Pedidos")));

	private static PantallaDeBoceto pantalla(String nombre) {
		return new PantallaDeBoceto(nombre, List.of(
				new PiezaDeBoceto(TipoDePieza.ENCABEZADO, nombre, List.of()),
				new PiezaDeBoceto(TipoDePieza.LISTA, "", List.of(new ElementoDePieza("Ana · almuerzo", "En camino")))));
	}

	private GeneradorBocetosFake generadorBocetos;
	private GeneradorImagenFake generadorImagen;

	@BeforeEach
	void preparar() {
		generadorBocetos = new GeneradorBocetosFake();
		generadorImagen = new GeneradorImagenFake();
	}

	private GenerarDemoDeDisenoUseCaseImpl useCase(boolean incluirImagen) {
		return new GenerarDemoDeDisenoUseCaseImpl(generadorBocetos, generadorImagen, RELOJ, 100, 2, incluirImagen);
	}

	private static SolicitudDeDemo solicitud() {
		return new SolicitudDeDemo("Restaurante", "Vendemos almuerzos y domicilios",
				"Recibir pedidos sin saturar el WhatsApp");
	}

	private static IdentidadDelVisitante registrado() {
		return IdentidadDelVisitante.registrada("cliente@correo.com");
	}

	@Test
	void soloLasIdentidadesRegistradasPuedenGenerar() {
		assertThatThrownBy(() -> useCase(true).generar(solicitud(), IdentidadDelVisitante.anonima("s1")))
				.isInstanceOf(DemoSoloParaRegistradosException.class);
		assertThat(generadorBocetos.llamadas).isZero();
		assertThat(generadorImagen.llamadas).isZero();
	}

	@Test
	void devuelveElBocetoConSusPantallasYLaImagen() {
		BocetoDeDemo demo = useCase(true).generar(solicitud(), registrado());

		assertThat(demo.solucion().pantallas()).extracting(PantallaDeBoceto::nombre).containsExactly("Inicio", "Pedidos");
		assertThat(demo.imagen()).hasValueSatisfying(imagen -> assertThat(imagen.base64()).isEqualTo("aW1hZ2VuLWZha2U="));
		assertThat(generadorBocetos.ultimaSolicitud).isEqualTo(solicitud());
	}

	@Test
	void conLaImagenApagadaNiSiquieraLaPide() {
		BocetoDeDemo demo = useCase(false).generar(solicitud(), registrado());

		assertThat(demo.imagen()).isEmpty();
		assertThat(generadorImagen.llamadas).isZero();
	}

	/**
	 * El proveedor de imágenes recibe la descripción DENTRO DE LA URL, y
	 * su respaldo es Pollinations, un servicio gratuito sin contrato ni
	 * acuerdo de tratamiento de datos (auditoría del 28 sep 2026,
	 * P1-8c): ahí solo puede ir texto que generó el modelo.
	 */
	@Test
	void alProveedorDeImagenesSoloLeLlegaElTituloDelBoceto() {
		useCase(true).generar(solicitud(), registrado());

		assertThat(generadorImagen.ultimaDescripcion)
				.doesNotContain("Recibir pedidos sin saturar el WhatsApp")
				.doesNotContain("Vendemos almuerzos y domicilios")
				.contains("App de pedidos para tu restaurante");
	}

	@Test
	void siLaImagenFallaElBocetoSaleIgualYSinElla() {
		generadorImagen.fallar = true;

		BocetoDeDemo demo = useCase(true).generar(solicitud(), registrado());

		assertThat(demo.solucion().titulo()).isEqualTo("App de pedidos para tu restaurante");
		assertThat(demo.imagen()).isEmpty();
	}

	@Test
	void unFalloDelBocetoNoConsumeCupoNiPideLaImagen() {
		GenerarDemoDeDisenoUseCaseImpl useCase = useCase(true);
		generadorBocetos.fallar = true;
		assertThatThrownBy(() -> useCase.generar(solicitud(), registrado()))
				.isInstanceOf(AsistenteNoDisponibleException.class);
		assertThat(generadorImagen.llamadas).isZero();

		generadorBocetos.fallar = false;
		useCase.generar(solicitud(), registrado());
		useCase.generar(solicitud(), registrado());
	}

	@Test
	void elRegistradoTieneSuLimiteDiarioPropio() {
		GenerarDemoDeDisenoUseCaseImpl useCase = useCase(false);
		useCase.generar(solicitud(), registrado());
		useCase.generar(solicitud(), registrado());

		assertThatThrownBy(() -> useCase.generar(solicitud(), registrado()))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	private static final class GeneradorBocetosFake implements GeneradorDeBocetos {
		private boolean fallar;
		private int llamadas;
		private SolicitudDeDemo ultimaSolicitud;

		@Override
		public BocetoDeSolucion generar(SolicitudDeDemo solicitud) {
			llamadas++;
			if (fallar) {
				throw new AsistenteNoDisponibleException("proveedor de bocetos caído");
			}
			ultimaSolicitud = solicitud;
			return BOCETO;
		}
	}

	private static final class GeneradorImagenFake implements GeneradorDeImagenes {
		private String ultimaDescripcion;
		private boolean fallar;
		private int llamadas;

		@Override
		public ImagenGenerada generar(String descripcion) {
			llamadas++;
			if (fallar) {
				throw new AsistenteNoDisponibleException("proveedor de imágenes caído");
			}
			ultimaDescripcion = descripcion;
			return new ImagenGenerada("aW1hZ2VuLWZha2U=", "image/png");
		}
	}

}
