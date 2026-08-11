package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionInvalidaException;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NuevoBorrador;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbrirBorradorDeCotizacionUseCaseImplTest {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final int DIAS_POR_DEFECTO = 15;

	private FakeCotizacionRepositorio cotizaciones;
	private FakeSolicitudRepositorio solicitudes;
	private AbrirBorradorDeCotizacionUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		cotizaciones = new FakeCotizacionRepositorio();
		solicitudes = new FakeSolicitudRepositorio();
		useCase = new AbrirBorradorDeCotizacionUseCaseImpl(cotizaciones, solicitudes,
				Clock.fixed(AHORA, ZoneOffset.UTC), DIAS_POR_DEFECTO);
	}

	private SolicitudDeContacto lead(String empresa) {
		SolicitudDeContacto solicitud = SolicitudDeContacto.registrar(
				new DatosDeContacto("Ana Perez", empresa, new Correo("ana@empresa.com"),
						new Telefono("3001234567")),
				ServicioDeInteres.DESARROLLO_A_LA_MEDIDA, "Necesito una app de pedidos",
				new ConsentimientoDatos(true, AHORA, "v1"), AHORA);
		solicitudes.guardar(solicitud);
		return solicitud;
	}

	@Test
	void abreUnBorradorEnBlancoConLosDatosDelClienteDados() {
		Cotizacion cotizacion = useCase.abrir(new NuevoBorrador(null,
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), 0, null, List.of()));

		assertThat(cotizacion.estado()).isEqualTo(EstadoCotizacion.BORRADOR);
		assertThat(cotizacion.cliente().nombre()).isEqualTo("Panaderia El Trigal");
		assertThat(cotizaciones.cuantasHay()).isEqualTo(1);
	}

	// HU-44: cotizar desde un lead no obliga a reescribir los datos.
	@Test
	void desdeUnLeadTomaLosDatosDeContactoDeLaSolicitud() {
		SolicitudDeContacto solicitud = lead("Panaderia El Trigal");

		Cotizacion cotizacion = useCase.abrir(new NuevoBorrador(solicitud.id(), null,
				new Porcentaje(19), 0, null, List.of()));

		assertThat(cotizacion.origen()).isEqualTo(solicitud.id());
		assertThat(cotizacion.cliente().nombre()).isEqualTo("Panaderia El Trigal");
		assertThat(cotizacion.cliente().correo()).isEqualTo(new Correo("ana@empresa.com"));
		assertThat(cotizacion.cliente().telefono()).isEqualTo("3001234567");
	}

	@Test
	void siElLeadNoTraeEmpresaUsaElNombreDeLaPersona() {
		SolicitudDeContacto solicitud = lead(null);

		Cotizacion cotizacion = useCase.abrir(new NuevoBorrador(solicitud.id(), null,
				new Porcentaje(19), 0, null, List.of()));

		assertThat(cotizacion.cliente().nombre()).isEqualTo("Ana Perez");
	}

	@Test
	void falllaSiElLeadDeOrigenNoExiste() {
		SolicitudId inexistente = SolicitudId.nuevo();

		assertThatThrownBy(() -> useCase.abrir(new NuevoBorrador(inexistente, null,
				new Porcentaje(19), 0, null, List.of())))
				.isInstanceOf(SolicitudNoEncontradaException.class);
	}

	@Test
	void unaCotizacionEnBlancoSinClienteNiLeadNoSePuedeAbrir() {
		assertThatThrownBy(() -> new NuevoBorrador(null, null, new Porcentaje(19), 0, null, List.of()))
				.isInstanceOf(CotizacionInvalidaException.class);
	}

	@Test
	void sinDiasDeValidezUsaElValorPorDefectoDeLaConfiguracion() {
		Cotizacion cotizacion = useCase.abrir(new NuevoBorrador(null,
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), 0, null, List.of()));

		assertThat(cotizacion.validaHasta()).isEqualTo(AHORA.plusSeconds(DIAS_POR_DEFECTO * 24L * 60 * 60));
	}

	@Test
	void respetaLosDiasDeValidezPedidos() {
		Cotizacion cotizacion = useCase.abrir(new NuevoBorrador(null,
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), 30, null, List.of()));

		assertThat(cotizacion.validaHasta()).isEqualTo(AHORA.plusSeconds(30L * 24 * 60 * 60));
	}

	@Test
	void guardaLosItemsQueVienenEnElComando() {
		Cotizacion cotizacion = useCase.abrir(new NuevoBorrador(null,
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), null, null),
				new Porcentaje(19), 0, "Incluye capacitacion",
				List.of(new ItemDeCotizacion("Desarrollo", 1, Dinero.de(5_000_000)))));

		assertThat(cotizacion.items()).hasSize(1);
		assertThat(cotizacion.total()).isEqualTo(Dinero.de(5_950_000));
		assertThat(cotizacion.notas()).isEqualTo("Incluye capacitacion");
	}

}
