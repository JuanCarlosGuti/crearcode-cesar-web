package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionVencidaException;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;
import com.crearcode.leads.dominio.TransicionDeEstadoInvalidaException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponderCotizacionUseCaseImplTest {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T10:00:00Z");
	private static final Correo CORREO_DEL_CLIENTE = new Correo("cliente@empresa.com");
	private static final Correo OTRO_CORREO = new Correo("otro@empresa.com");

	private FakeCotizacionRepositorio cotizaciones;
	private FakeSolicitudRepositorio solicitudes;
	private ResponderCotizacionUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		cotizaciones = new FakeCotizacionRepositorio();
		solicitudes = new FakeSolicitudRepositorio();
		useCase = new ResponderCotizacionUseCaseImpl(cotizaciones, solicitudes,
				Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	private SolicitudDeContacto leadContactado() {
		SolicitudDeContacto lead = SolicitudDeContacto.registrar(
				new DatosDeContacto("Ana Perez", "Panaderia El Trigal", CORREO_DEL_CLIENTE,
						new Telefono("3001234567")),
				ServicioDeInteres.DESARROLLO_A_LA_MEDIDA, "Necesito una app",
				new ConsentimientoDatos(true, AHORA, "v1"), AHORA);
		lead.cambiarEstado(EstadoSolicitud.CONTACTADA, AHORA);
		solicitudes.guardar(lead);
		return lead;
	}

	private Cotizacion enviadaCon(SolicitudId origen, Instant validaHasta) {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", CORREO_DEL_CLIENTE, null, null),
				new Porcentaje(19), AHORA, validaHasta, origen, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Desarrollo", 1, Dinero.de(5_000_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 1), AHORA);
		cotizaciones.guardar(cotizacion);
		return cotizacion;
	}

	@Test
	void elClienteAceptaSuCotizacion() {
		Cotizacion cotizacion = enviadaCon(null, EN_QUINCE_DIAS);

		Cotizacion aceptada = useCase.aceptar(cotizacion.id(), CORREO_DEL_CLIENTE);

		assertThat(aceptada.estado()).isEqualTo(EstadoCotizacion.ACEPTADA);
		assertThat(aceptada.respondidaEn()).isEqualTo(AHORA);
	}

	@Test
	void elClienteRechazaSuCotizacion() {
		Cotizacion cotizacion = enviadaCon(null, EN_QUINCE_DIAS);

		assertThat(useCase.rechazar(cotizacion.id(), CORREO_DEL_CLIENTE).estado())
				.isEqualTo(EstadoCotizacion.RECHAZADA);
	}

	// HU-47: aceptar cierra el circulo con el lead, sin trabajo manual.
	@Test
	void aceptarConvierteElLeadDeOrigen() {
		SolicitudDeContacto lead = leadContactado();
		Cotizacion cotizacion = enviadaCon(lead.id(), EN_QUINCE_DIAS);

		useCase.aceptar(cotizacion.id(), CORREO_DEL_CLIENTE);

		assertThat(solicitudes.buscarPorId(lead.id()).orElseThrow().estado())
				.isEqualTo(EstadoSolicitud.CONVERTIDA);
	}

	// Invariante 7: el pipeline comercial no se rompe por el estado del lead.
	@Test
	void siElLeadYaEstabaDescartadoLaAceptacionNoFalla() {
		SolicitudDeContacto lead = leadContactado();
		lead.cambiarEstado(EstadoSolicitud.DESCARTADA, AHORA);
		solicitudes.guardar(lead);
		Cotizacion cotizacion = enviadaCon(lead.id(), EN_QUINCE_DIAS);

		Cotizacion aceptada = useCase.aceptar(cotizacion.id(), CORREO_DEL_CLIENTE);

		assertThat(aceptada.estado()).isEqualTo(EstadoCotizacion.ACEPTADA);
		assertThat(solicitudes.buscarPorId(lead.id()).orElseThrow().estado())
				.isEqualTo(EstadoSolicitud.DESCARTADA);
	}

	@Test
	void rechazarNoTocaElLead() {
		SolicitudDeContacto lead = leadContactado();
		Cotizacion cotizacion = enviadaCon(lead.id(), EN_QUINCE_DIAS);

		useCase.rechazar(cotizacion.id(), CORREO_DEL_CLIENTE);

		assertThat(solicitudes.buscarPorId(lead.id()).orElseThrow().estado())
				.isEqualTo(EstadoSolicitud.CONTACTADA);
	}

	// Invariante 6: una cotizacion ajena se comporta como inexistente.
	@Test
	void otroClienteNoPuedeResponderUnaCotizacionQueNoEsSuya() {
		Cotizacion cotizacion = enviadaCon(null, EN_QUINCE_DIAS);

		assertThatThrownBy(() -> useCase.aceptar(cotizacion.id(), OTRO_CORREO))
				.isInstanceOf(CotizacionNoEncontradaException.class);
		assertThat(cotizaciones.buscarPorId(cotizacion.id()).orElseThrow().estado())
				.isEqualTo(EstadoCotizacion.ENVIADA);
	}

	@Test
	void elCorreoSeComparaSinDistinguirMayusculas() {
		Cotizacion cotizacion = enviadaCon(null, EN_QUINCE_DIAS);

		assertThat(useCase.aceptar(cotizacion.id(), new Correo("Cliente@Empresa.com")).estado())
				.isEqualTo(EstadoCotizacion.ACEPTADA);
	}

	// Invariante 4: la validez se comprueba en el servidor. El escenario
	// real es que el cliente vuelva con el enlace despues del plazo, asi
	// que lo que se mueve es el reloj, no la fecha de validez.
	@Test
	void noSePuedeAceptarUnaCotizacionVencida() {
		Cotizacion cotizacion = enviadaCon(null, EN_QUINCE_DIAS);
		ResponderCotizacionUseCaseImpl elMesQueViene = new ResponderCotizacionUseCaseImpl(
				cotizaciones, solicitudes,
				Clock.fixed(Instant.parse("2026-09-15T10:00:00Z"), ZoneOffset.UTC));

		assertThatThrownBy(() -> elMesQueViene.aceptar(cotizacion.id(), CORREO_DEL_CLIENTE))
				.isInstanceOf(CotizacionVencidaException.class);
		assertThat(cotizaciones.buscarPorId(cotizacion.id()).orElseThrow().estado())
				.isEqualTo(EstadoCotizacion.ENVIADA);
	}

	@Test
	void noSePuedeResponderDosVeces() {
		Cotizacion cotizacion = enviadaCon(null, EN_QUINCE_DIAS);
		useCase.aceptar(cotizacion.id(), CORREO_DEL_CLIENTE);

		assertThatThrownBy(() -> useCase.rechazar(cotizacion.id(), CORREO_DEL_CLIENTE))
				.isInstanceOf(TransicionDeEstadoInvalidaException.class);
	}

	@Test
	void fallaSiLaCotizacionNoExiste() {
		assertThatThrownBy(() -> useCase.aceptar(CotizacionId.nuevo(), CORREO_DEL_CLIENTE))
				.isInstanceOf(CotizacionNoEncontradaException.class);
	}

}
