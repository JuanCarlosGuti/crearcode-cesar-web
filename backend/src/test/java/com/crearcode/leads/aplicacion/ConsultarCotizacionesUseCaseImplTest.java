package com.crearcode.leads.aplicacion;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultarCotizacionesUseCaseImplTest {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T10:00:00Z");
	private static final Correo CLIENTE = new Correo("cliente@empresa.com");
	private static final Correo OTRO = new Correo("otro@empresa.com");

	private FakeCotizacionRepositorio cotizaciones;
	private ConsultarCotizacionesUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		cotizaciones = new FakeCotizacionRepositorio();
		useCase = new ConsultarCotizacionesUseCaseImpl(cotizaciones);
	}

	private Cotizacion guardar(Correo correo, boolean enviar) {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Cliente", correo, null, null),
				new Porcentaje(0), AHORA, EN_QUINCE_DIAS, null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Servicio", 1, Dinero.de(1_000_000)));
		if (enviar) {
			cotizacion.enviar(NumeroDeCotizacion.de(2026, 1), AHORA);
		}
		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	@Test
	void elEquipoVeTodasLasCotizaciones() {
		guardar(CLIENTE, true);
		guardar(OTRO, false);

		assertThat(useCase.listar(null)).hasSize(2);
	}

	@Test
	void elEquipoPuedeFiltrarPorEstado() {
		guardar(CLIENTE, true);
		guardar(OTRO, false);

		assertThat(useCase.listar(EstadoCotizacion.ENVIADA)).hasSize(1);
		assertThat(useCase.listar(EstadoCotizacion.BORRADOR)).hasSize(1);
	}

	// Invariante 6: el cliente solo ve lo suyo.
	@Test
	void elClienteSoloVeLasCotizacionesDirigidasASuCorreo() {
		guardar(CLIENTE, true);
		guardar(OTRO, true);

		assertThat(useCase.listarDe(CLIENTE)).hasSize(1);
		assertThat(useCase.listarDe(CLIENTE).get(0).cliente().correo()).isEqualTo(CLIENTE);
	}

	@Test
	void unaCotizacionAjenaSeComportaComoInexistenteParaElCliente() {
		Cotizacion ajena = guardar(OTRO, true);

		assertThat(useCase.obtenerDe(ajena.id(), CLIENTE)).isEmpty();
		assertThat(useCase.obtener(ajena.id())).isPresent();
	}

	@Test
	void obtenerUnaCotizacionInexistenteDevuelveVacio() {
		assertThat(useCase.obtener(CotizacionId.nuevo())).isEmpty();
	}

}
