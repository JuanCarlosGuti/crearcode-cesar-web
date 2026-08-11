package com.crearcode.leads.dominio;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CotizacionTest {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T10:00:00Z");
	private static final Instant PASADA_LA_VALIDEZ = Instant.parse("2026-08-27T10:00:00Z");

	private static final DatosDelCliente CLIENTE = new DatosDelCliente(
			"Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null);

	private static Cotizacion borrador() {
		return Cotizacion.abrirBorrador(CLIENTE, new Porcentaje(19), AHORA, EN_QUINCE_DIAS, null, null);
	}

	private static Cotizacion borradorConUnItem() {
		Cotizacion cotizacion = borrador();
		cotizacion.agregarItem(new ItemDeCotizacion("Desarrollo del modulo de pedidos", 1, Dinero.de(5_000_000)));
		return cotizacion;
	}

	private static Cotizacion enviada() {
		Cotizacion cotizacion = borradorConUnItem();
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 1), AHORA);
		return cotizacion;
	}

	@Test
	void naceComoBorradorSinNumeroYSinItems() {
		Cotizacion cotizacion = borrador();

		assertThat(cotizacion.estado()).isEqualTo(EstadoCotizacion.BORRADOR);
		assertThat(cotizacion.numero()).isNull();
		assertThat(cotizacion.items()).isEmpty();
	}

	@Test
	void rechazaUnaValidezQueNoEsPosteriorALaCreacion() {
		assertThatThrownBy(() -> Cotizacion.abrirBorrador(CLIENTE, new Porcentaje(19), AHORA, AHORA, null, null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void rechazaAbrirUnBorradorSinCliente() {
		assertThatThrownBy(() -> Cotizacion.abrirBorrador(null, new Porcentaje(19), AHORA, EN_QUINCE_DIAS, null, null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	// Invariante 2: los totales los calcula el dominio, siempre.
	@Test
	void calculaSubtotalImpuestoYTotalDesdeLosItems() {
		Cotizacion cotizacion = borrador();
		cotizacion.agregarItem(new ItemDeCotizacion("Horas de desarrollo", 40, Dinero.de(100_000)));
		cotizacion.agregarItem(new ItemDeCotizacion("Puesta en marcha", 1, Dinero.de(1_000_000)));

		assertThat(cotizacion.subtotal()).isEqualTo(Dinero.de(5_000_000));
		assertThat(cotizacion.impuestoCalculado()).isEqualTo(Dinero.de(950_000));
		assertThat(cotizacion.total()).isEqualTo(Dinero.de(5_950_000));
	}

	@Test
	void sinImpuestoElTotalEsElSubtotal() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(CLIENTE, new Porcentaje(0), AHORA, EN_QUINCE_DIAS, null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Consultoria", 1, Dinero.de(800_000)));

		assertThat(cotizacion.impuestoCalculado()).isEqualTo(Dinero.CERO);
		assertThat(cotizacion.total()).isEqualTo(Dinero.de(800_000));
	}

	@Test
	void laListaDeItemsQueEntregaNoSePuedeModificarDesdeAfuera() {
		Cotizacion cotizacion = borradorConUnItem();

		assertThatThrownBy(() -> cotizacion.items().clear())
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void quitarUnItemLoSacaDelBorrador() {
		Cotizacion cotizacion = borradorConUnItem();

		cotizacion.quitarItem(0);

		assertThat(cotizacion.items()).isEmpty();
	}

	@Test
	void quitarUnItemQueNoExisteFalla() {
		assertThatThrownBy(() -> borradorConUnItem().quitarItem(5))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	// Invariante 3: no se envia una cotizacion vacia.
	@Test
	void noSePuedeEnviarSinItems() {
		assertThatThrownBy(() -> borrador().enviar(NumeroDeCotizacion.de(2026, 1), AHORA))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void enviarAsignaElNumeroYDejaLaCotizacionEnviada() {
		Cotizacion cotizacion = enviada();

		assertThat(cotizacion.estado()).isEqualTo(EstadoCotizacion.ENVIADA);
		assertThat(cotizacion.numero()).isEqualTo(NumeroDeCotizacion.de(2026, 1));
		assertThat(cotizacion.enviadaEn()).isEqualTo(AHORA);
	}

	@Test
	void noSePuedeEnviarDosVeces() {
		Cotizacion cotizacion = enviada();

		assertThatThrownBy(() -> cotizacion.enviar(NumeroDeCotizacion.de(2026, 2), AHORA))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	// Invariante 1: lo que el cliente vio no puede cambiar despues.
	@Test
	void unaCotizacionEnviadaYaNoSeEdita() {
		Cotizacion cotizacion = enviada();

		assertThatThrownBy(() -> cotizacion.agregarItem(new ItemDeCotizacion("Extra", 1, Dinero.de(1))))
				.isInstanceOf(CotizacionInvalidaException.class);
		assertThatThrownBy(() -> cotizacion.quitarItem(0))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void reemplazarItemsDejaSoloLosNuevos() {
		Cotizacion cotizacion = borradorConUnItem();

		cotizacion.reemplazarItems(List.of(
				new ItemDeCotizacion("Analisis", 1, Dinero.de(500_000)),
				new ItemDeCotizacion("Implementacion", 2, Dinero.de(750_000))));

		assertThat(cotizacion.items()).hasSize(2);
		assertThat(cotizacion.subtotal()).isEqualTo(Dinero.de(2_000_000));
	}

	@Test
	void reemplazarItemsRechazaListasNulasOConNulos() {
		Cotizacion cotizacion = borradorConUnItem();

		assertThatThrownBy(() -> cotizacion.reemplazarItems(null))
				.isInstanceOf(CotizacionInvalidaException.class);
		assertThatThrownBy(() -> cotizacion.reemplazarItems(Arrays.asList(
				new ItemDeCotizacion("Analisis", 1, Dinero.de(500_000)), null)))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void lasNotasSoloSeCambianMientrasEsBorrador() {
		Cotizacion cotizacion = borradorConUnItem();
		cotizacion.cambiarNotas("Incluye dos capacitaciones");
		assertThat(cotizacion.notas()).isEqualTo("Incluye dos capacitaciones");

		cotizacion.enviar(NumeroDeCotizacion.de(2026, 1), AHORA);

		assertThatThrownBy(() -> cotizacion.cambiarNotas("otra cosa"))
				.isInstanceOf(CotizacionInvalidaException.class);
		assertThatThrownBy(() -> cotizacion.reemplazarItems(List.of()))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void elClienteAceptaSuCotizacionVigente() {
		Cotizacion cotizacion = enviada();

		cotizacion.aceptar(AHORA);

		assertThat(cotizacion.estado()).isEqualTo(EstadoCotizacion.ACEPTADA);
		assertThat(cotizacion.respondidaEn()).isEqualTo(AHORA);
	}

	@Test
	void elClienteRechazaSuCotizacionVigente() {
		Cotizacion cotizacion = enviada();

		cotizacion.rechazar(AHORA);

		assertThat(cotizacion.estado()).isEqualTo(EstadoCotizacion.RECHAZADA);
		assertThat(cotizacion.respondidaEn()).isEqualTo(AHORA);
	}

	// Invariante 4: la validez se comprueba en el servidor, no en la
	// interfaz — quien conserve el enlace no puede aceptar fuera de plazo.
	@Test
	void noSePuedeResponderUnaCotizacionVencida() {
		Cotizacion cotizacion = enviada();

		assertThatThrownBy(() -> cotizacion.aceptar(PASADA_LA_VALIDEZ))
				.isInstanceOf(CotizacionVencidaException.class);
		assertThatThrownBy(() -> cotizacion.rechazar(PASADA_LA_VALIDEZ))
				.isInstanceOf(CotizacionVencidaException.class);
	}

	@Test
	void noSePuedeResponderUnBorrador() {
		assertThatThrownBy(() -> borradorConUnItem().aceptar(AHORA))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	@Test
	void noSePuedeResponderDosVeces() {
		Cotizacion cotizacion = enviada();
		cotizacion.aceptar(AHORA);

		assertThatThrownBy(() -> cotizacion.rechazar(AHORA))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	@Test
	void marcarVencidaSoloAplicaCuandoYaPasoLaValidez() {
		Cotizacion cotizacion = enviada();

		assertThatThrownBy(() -> cotizacion.marcarVencida(AHORA))
				.isInstanceOf(CotizacionInvalidaException.class);

		cotizacion.marcarVencida(PASADA_LA_VALIDEZ);

		assertThat(cotizacion.estado()).isEqualTo(EstadoCotizacion.VENCIDA);
	}

	@Test
	void seCancelaTantoUnBorradorComoUnaEnviada() {
		Cotizacion borrador = borradorConUnItem();
		borrador.cancelar(AHORA);
		assertThat(borrador.estado()).isEqualTo(EstadoCotizacion.CANCELADA);

		Cotizacion enviada = enviada();
		enviada.cancelar(AHORA);
		assertThat(enviada.estado()).isEqualTo(EstadoCotizacion.CANCELADA);
	}

	@Test
	void estaVigenteMientrasNoPaseSuValidez() {
		Cotizacion cotizacion = enviada();

		assertThat(cotizacion.estaVigente(AHORA)).isTrue();
		assertThat(cotizacion.estaVigente(PASADA_LA_VALIDEZ)).isFalse();
	}

	@Test
	void recuerdaLaSolicitudQueLeDioOrigenCuandoNaceDeUnLead() {
		SolicitudId origen = SolicitudId.nuevo();
		Cotizacion cotizacion = Cotizacion.abrirBorrador(CLIENTE, new Porcentaje(19), AHORA, EN_QUINCE_DIAS,
				origen, "Incluye capacitacion");

		assertThat(cotizacion.origen()).isEqualTo(origen);
		assertThat(cotizacion.notas()).isEqualTo("Incluye capacitacion");
	}

}
