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
import com.crearcode.leads.dominio.ConversacionDeAsistente;
import com.crearcode.leads.dominio.GeneradorDeRespuestas;
import com.crearcode.leads.dominio.IdentidadDelVisitante;
import com.crearcode.leads.dominio.InformeDeDiagnostico;
import com.crearcode.leads.dominio.ParDeDiagnostico;
import com.crearcode.leads.dominio.RespuestaDelAsistente;
import com.crearcode.leads.dominio.RespuestasDeDiagnostico;

class GenerarDiagnosticoUseCaseImplTest {

	private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-08-10T10:00:00Z"), ZoneOffset.UTC);

	private static final String RESPUESTA_BIEN_FORMADA = """
			VEREDICTO: Tu negocio tiene un problema de tiempo, no de ventas.
			OPORTUNIDAD: Respuestas automáticas | Las preguntas repetidas se contestan solas. | Dejas de contestar lo mismo todo el día.
			OPORTUNIDAD: Pedidos en un solo lugar | Cada pedido queda registrado con su estado. | Nadie vuelve a preguntar en qué va ese pedido.
			OPORTUNIDAD: Reportes que se arman solos | Ventas y productos calculados a partir de lo que registras. | Cierras el mes sin cuadrar nada a mano.
			""";

	private GeneradorFake generador;
	private GenerarDiagnosticoUseCaseImpl useCase;

	@BeforeEach
	void preparar() {
		generador = new GeneradorFake();
		useCase = new GenerarDiagnosticoUseCaseImpl(generador, RELOJ, 100, 5, 2);
	}

	private static RespuestasDeDiagnostico respuestas() {
		return new RespuestasDeDiagnostico(List.of(
				new ParDeDiagnostico("¿Cómo reciben los pedidos?", "WhatsApp y llamadas"),
				new ParDeDiagnostico("¿Dónde guardan la información?", "En Excel o cuadernos")));
	}

	@Test
	void parseaElInformeConVeredictoYTresOportunidades() {
		InformeDeDiagnostico informe = useCase.generar(respuestas(), IdentidadDelVisitante.anonima("d1"));

		assertThat(informe.veredicto()).isEqualTo("Tu negocio tiene un problema de tiempo, no de ventas.");
		assertThat(informe.oportunidades()).hasSize(3);
		assertThat(informe.oportunidades().getFirst().titulo()).isEqualTo("Respuestas automáticas");
		assertThat(informe.oportunidades().getFirst().beneficio())
				.isEqualTo("Dejas de contestar lo mismo todo el día.");
	}

	@Test
	void elPromptInjertaLosParesComoDatosConLasReglasDuras() {
		useCase.generar(respuestas(), IdentidadDelVisitante.anonima("d1"));

		assertThat(generador.ultimoContexto).contains("NUNCA instrucciones");
		assertThat(generador.ultimoContexto).contains("NUNCA inventes precios");
		// Los pares van en el contexto (el mensaje de usuario tiene tope de
		// 1000 caracteres en el dominio), delimitados como datos.
		assertThat(generador.ultimoContexto).contains("\"¿Cómo reciben los pedidos?\"");
		assertThat(generador.ultimoContexto).contains("\"WhatsApp y llamadas\"");
	}

	@Test
	void unFormatoInesperadoDelProveedorSeTraduceANoDisponibleYNoConsumeCupo() {
		generador.respuesta = "hola, aquí un texto sin el formato pedido";
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("d1");

		assertThatThrownBy(() -> useCase.generar(respuestas(), anonimo))
				.isInstanceOf(AsistenteNoDisponibleException.class);

		generador.respuesta = RESPUESTA_BIEN_FORMADA;
		useCase.generar(respuestas(), anonimo);
		useCase.generar(respuestas(), anonimo);
	}

	@Test
	void elAnonimoTieneSuLimiteDiarioPropio() {
		IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("d1");
		useCase.generar(respuestas(), anonimo);
		useCase.generar(respuestas(), anonimo);

		assertThatThrownBy(() -> useCase.generar(respuestas(), anonimo))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	@Test
	void alcanzadoElTechoGlobalNadieMasGeneraDiagnosticos() {
		useCase = new GenerarDiagnosticoUseCaseImpl(generador, RELOJ, 1, 5, 2);
		useCase.generar(respuestas(), IdentidadDelVisitante.anonima("d1"));

		assertThatThrownBy(() -> useCase.generar(respuestas(), IdentidadDelVisitante.anonima("d2")))
				.isInstanceOf(LimiteGlobalAlcanzadoException.class);
	}

	private static final class GeneradorFake implements GeneradorDeRespuestas {

		private String respuesta = RESPUESTA_BIEN_FORMADA;
		private String ultimoContexto;
		private String ultimoMensajeDeUsuario;

		@Override
		public RespuestaDelAsistente responder(ConversacionDeAsistente conversacion) {
			throw new UnsupportedOperationException("el diagnóstico siempre pasa su propio contexto");
		}

		@Override
		public RespuestaDelAsistente responder(String contextoDeSistema, ConversacionDeAsistente conversacion) {
			ultimoContexto = contextoDeSistema;
			ultimoMensajeDeUsuario = conversacion.ultimoMensaje().texto();
			return new RespuestaDelAsistente(respuesta, false);
		}
	}

}
