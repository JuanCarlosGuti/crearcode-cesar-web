package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionInvalidaException;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EnviadorDeCotizaciones;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.GeneradorDeNumeroDeCotizacion;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.TransicionDeEstadoInvalidaException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnviarCotizacionUseCaseImplTest {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T10:00:00Z");

	private FakeCotizacionRepositorio cotizaciones;
	private FakeGeneradorDeNumero numeros;
	private FakeEnviadorDeCotizaciones enviador;
	private EnviarCotizacionUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		cotizaciones = new FakeCotizacionRepositorio();
		numeros = new FakeGeneradorDeNumero();
		enviador = new FakeEnviadorDeCotizaciones();
		useCase = new EnviarCotizacionUseCaseImpl(cotizaciones, numeros, enviador,
				Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	private Cotizacion borradorGuardado() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), AHORA, EN_QUINCE_DIAS, null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Desarrollo", 1, Dinero.de(5_000_000)));
		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	@Test
	void asignaElConsecutivoDelAnioEnCursoYDejaLaCotizacionEnviada() {
		Cotizacion enviada = useCase.enviar(borradorGuardado().id());

		assertThat(enviada.estado()).isEqualTo(EstadoCotizacion.ENVIADA);
		assertThat(enviada.numero()).isEqualTo(NumeroDeCotizacion.de(2026, 1));
		assertThat(numeros.aniosPedidos).containsExactly(2026);
	}

	@Test
	void avisaAlClienteConLaCotizacionYaNumerada() {
		useCase.enviar(borradorGuardado().id());

		assertThat(enviador.enviadas).hasSize(1);
		assertThat(enviador.enviadas.get(0).numero()).isEqualTo(NumeroDeCotizacion.de(2026, 1));
	}

	// El correo es best-effort: su fallo no puede dejar a medias el envio.
	@Test
	void siElCorreoFallaLaCotizacionQuedaEnviadaIgual() {
		enviador.fallar = true;

		Cotizacion enviada = useCase.enviar(borradorGuardado().id());

		assertThat(enviada.estado()).isEqualTo(EstadoCotizacion.ENVIADA);
		assertThat(cotizaciones.buscarPorId(enviada.id()).orElseThrow().estado())
				.isEqualTo(EstadoCotizacion.ENVIADA);
	}

	@Test
	void noSePuedeEnviarDosVeces() {
		CotizacionId id = borradorGuardado().id();
		useCase.enviar(id);

		assertThatThrownBy(() -> useCase.enviar(id))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	// Invariante 5: un borrador que no sale no consume consecutivo. Aqui
	// el envio falla por falta de items, y el numero pedido no se usa.
	@Test
	void unaCotizacionSinItemsNoSePuedeEnviar() {
		Cotizacion vacia = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), AHORA, EN_QUINCE_DIAS, null, null);
		cotizaciones.guardar(vacia);

		assertThatThrownBy(() -> useCase.enviar(vacia.id()))
				.isInstanceOf(CotizacionInvalidaException.class);
		assertThat(enviador.enviadas).isEmpty();
	}

	@Test
	void fallaSiLaCotizacionNoExiste() {
		assertThatThrownBy(() -> useCase.enviar(CotizacionId.nuevo()))
				.isInstanceOf(CotizacionNoEncontradaException.class);
	}

	private static final class FakeGeneradorDeNumero implements GeneradorDeNumeroDeCotizacion {
		private final List<Integer> aniosPedidos = new ArrayList<>();
		private int consecutivo;

		@Override
		public NumeroDeCotizacion siguiente(int anio) {
			aniosPedidos.add(anio);
			return NumeroDeCotizacion.de(anio, ++consecutivo);
		}
	}

	private static final class FakeEnviadorDeCotizaciones implements EnviadorDeCotizaciones {
		private final List<Cotizacion> enviadas = new ArrayList<>();
		private boolean fallar;

		@Override
		public void enviar(Cotizacion cotizacion) {
			if (fallar) {
				throw new IllegalStateException("SMTP caído");
			}
			enviadas.add(cotizacion);
		}
	}

}
