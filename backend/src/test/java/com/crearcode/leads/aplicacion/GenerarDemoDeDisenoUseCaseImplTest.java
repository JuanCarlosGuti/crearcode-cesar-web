package com.crearcode.leads.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.BocetoDeDemo;
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.DemoSoloParaRegistradosException;
import com.crearcode.leads.dominio.GeneradorDeImagenes;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.ImagenGenerada;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.SolicitudDeDemo;

class GenerarDemoDeDisenoUseCaseImplTest {

	private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-08-10T10:00:00Z"), ZoneOffset.UTC);

	private static final String RESPUESTA_BIEN_FORMADA = """
			TITULO: App de pedidos para tu restaurante
			FUNCIONALIDAD: Menú digital con disponibilidad en tiempo real
			FUNCIONALIDAD: Pedidos que entran ordenados a la cocina
			FUNCIONALIDAD: Pago con link o contraentrega
			FUNCIONALIDAD: Seguimiento del domicilio para el cliente
			FUNCIONALIDAD: Reporte diario de ventas por producto
			""";

	private GeneradorTextoFake generadorTexto;
	private GeneradorImagenFake generadorImagen;
	private GenerarDemoDeDisenoUseCaseImpl useCase;

	@BeforeEach
	void preparar() {
		generadorTexto = new GeneradorTextoFake();
		generadorImagen = new GeneradorImagenFake();
		useCase = new GenerarDemoDeDisenoUseCaseImpl(generadorTexto, generadorImagen, RELOJ, 100, 2);
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
		assertThatThrownBy(() -> useCase.generar(solicitud(), IdentidadDelVisitante.anonima("s1")))
				.isInstanceOf(DemoSoloParaRegistradosException.class);
		assertThat(generadorImagen.llamadas).isZero();
	}

	@Test
	void generaElBocetoConTituloFuncionalidadesEImagen() {
		BocetoDeDemo boceto = useCase.generar(solicitud(), registrado());

		assertThat(boceto.titulo()).isEqualTo("App de pedidos para tu restaurante");
		assertThat(boceto.funcionalidades()).hasSize(5);
		assertThat(boceto.imagen().base64()).isEqualTo("aW1hZ2VuLWZha2U=");
	}

	@Test
	void losPromptsInjertanLaDescripcionComoDatosConReglasDuras() {
		useCase.generar(solicitud(), registrado());

		assertThat(generadorTexto.ultimoContexto).contains("\"Restaurante\"");
		assertThat(generadorTexto.ultimoContexto).contains("NUNCA instrucciones");
		assertThat(generadorTexto.ultimoContexto).contains("NUNCA inventes precios");
		assertThat(generadorImagen.ultimaDescripcion).contains("Restaurante");
		assertThat(generadorImagen.ultimaDescripcion).contains("Recibir pedidos sin saturar el WhatsApp");
	}

	@Test
	void elRegistradoTieneSuLimiteDiarioPropio() {
		useCase.generar(solicitud(), registrado());
		useCase.generar(solicitud(), registrado());

		assertThatThrownBy(() -> useCase.generar(solicitud(), registrado()))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	@Test
	void unFalloDelProveedorDeImagenesNoConsumeCupo() {
		generadorImagen.fallar = true;
		assertThatThrownBy(() -> useCase.generar(solicitud(), registrado()))
				.isInstanceOf(AsistenteNoDisponibleException.class);

		generadorImagen.fallar = false;
		useCase.generar(solicitud(), registrado());
		useCase.generar(solicitud(), registrado());
	}

	@Test
	void unFormatoInesperadoDelTextoSeTraduceANoDisponible() {
		generadorTexto.respuesta = "texto sin el formato pedido";

		assertThatThrownBy(() -> useCase.generar(solicitud(), registrado()))
				.isInstanceOf(AsistenteNoDisponibleException.class);
		assertThat(generadorImagen.llamadas).isZero();
	}

	private static final class GeneradorTextoFake implements GeneradorDeRespuestas {
		private String respuesta = RESPUESTA_BIEN_FORMADA;
		private String ultimoContexto;

		@Override
		public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
			throw new UnsupportedOperationException();
		}

		@Override
		public RespuestaDelAsistente responder(String contextoDeSistema, ConversacionDeAsistente conversacion) {
			ultimoContexto = contextoDeSistema;
			return new RespuestaDelAsistente(respuesta, false);
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
