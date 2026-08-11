package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionInvalidaException;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditarBorradorDeCotizacionUseCaseImplTest {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T10:00:00Z");

	private FakeCotizacionRepositorio cotizaciones;
	private EditarBorradorDeCotizacionUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		cotizaciones = new FakeCotizacionRepositorio();
		useCase = new EditarBorradorDeCotizacionUseCaseImpl(cotizaciones, Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	private Cotizacion borradorGuardado() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), AHORA, EN_QUINCE_DIAS, null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Analisis", 1, Dinero.de(1_000_000)));
		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	@Test
	void reemplazaItemsYNotasDelBorrador() {
		Cotizacion guardada = borradorGuardado();

		Cotizacion editada = useCase.editar(guardada.id(),
				List.of(new ItemDeCotizacion("Desarrollo", 2, Dinero.de(2_000_000))),
				"Incluye soporte de un mes");

		assertThat(editada.items()).hasSize(1);
		assertThat(editada.subtotal()).isEqualTo(Dinero.de(4_000_000));
		assertThat(editada.notas()).isEqualTo("Incluye soporte de un mes");
	}

	@Test
	void fallaSiLaCotizacionNoExiste() {
		assertThatThrownBy(() -> useCase.editar(CotizacionId.nuevo(), List.of(), null))
				.isInstanceOf(CotizacionNoEncontradaException.class);
	}

	// Invariante 1: lo que el cliente ya vio no se edita.
	@Test
	void noSePuedeEditarUnaCotizacionYaEnviada() {
		Cotizacion guardada = borradorGuardado();
		guardada.enviar(NumeroDeCotizacion.de(2026, 1), AHORA);
		cotizaciones.guardar(guardada);

		assertThatThrownBy(() -> useCase.editar(guardada.id(),
				List.of(new ItemDeCotizacion("Otra cosa", 1, Dinero.de(1))), null))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void cancelarDejaLaCotizacionCancelada() {
		Cotizacion guardada = borradorGuardado();

		useCase.cancelar(guardada.id());

		assertThat(cotizaciones.buscarPorId(guardada.id()).orElseThrow().estado())
				.isEqualTo(EstadoCotizacion.CANCELADA);
	}

	@Test
	void cancelarUnaCotizacionInexistenteFalla() {
		assertThatThrownBy(() -> useCase.cancelar(CotizacionId.nuevo()))
				.isInstanceOf(CotizacionNoEncontradaException.class);
	}

}
