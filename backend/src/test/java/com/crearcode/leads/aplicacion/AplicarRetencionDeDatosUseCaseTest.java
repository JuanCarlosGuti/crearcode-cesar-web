package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.ConsentimientoDatos;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeContacto;
import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;
import com.crearcode.leads.dominio.SolicitudDeContacto;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.dominio.Telefono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La política v2 (§9) promete que las solicitudes que no se convierten
 * en clientes se borran a los 24 meses del último contacto. Prometerlo
 * y no hacerlo es peor que no prometerlo: el dato sigue ahí, y ahora
 * además hay un documento público diciendo que no.
 */
class AplicarRetencionDeDatosUseCaseTest {

	private static final Instant HOY = Instant.parse("2026-09-28T10:00:00Z");
	private static final Clock RELOJ = Clock.fixed(HOY, ZoneOffset.UTC);

	private static final DatosDeContacto DATOS = new DatosDeContacto(
			"Juan Pérez", null, new Correo("nombre@empresa.com"), new Telefono("3001234567"));

	private FakeSolicitudRepositorio solicitudes;
	private AplicarRetencionDeDatosUseCaseImpl useCase;

	@BeforeEach
	void configurar() {
		solicitudes = new FakeSolicitudRepositorio();
		useCase = new AplicarRetencionDeDatosUseCaseImpl(solicitudes, RELOJ, 24);
	}

	private SolicitudId guardar(EstadoSolicitud estado, Instant ultimaActualizacion) {
		SolicitudId id = SolicitudId.nuevo();
		solicitudes.guardar(SolicitudDeContacto.reconstruir(id, DATOS, ServicioDeInteres.OTRO, "mensaje",
				estado, new ConsentimientoDatos(true, ultimaActualizacion, "v2"),
				ultimaActualizacion, ultimaActualizacion));
		return id;
	}

	@Test
	void borraLasSolicitudesSinConvertirMasViejasQueElPlazo() {
		SolicitudId vieja = guardar(EstadoSolicitud.DESCARTADA, HOY.minus(java.time.Duration.ofDays(800)));

		int borradas = useCase.aplicar();

		assertThat(borradas).isEqualTo(1);
		assertThat(solicitudes.buscarPorId(vieja)).isEmpty();
	}

	@Test
	void respetaLasQueTodaviaEstanDentroDelPlazo() {
		SolicitudId reciente = guardar(EstadoSolicitud.NUEVA, HOY.minus(java.time.Duration.ofDays(30)));

		useCase.aplicar();

		assertThat(solicitudes.buscarPorId(reciente)).isPresent();
	}

	/**
	 * Un lead convertido es un cliente: sus datos son registro comercial
	 * y contable, con un plazo propio de diez años (política v2, §9). Si
	 * esta tarea los borrara, destruiría justo lo que la ley obliga a
	 * conservar.
	 */
	@Test
	void nuncaBorraUnLeadConvertidoPorViejoQueSea() {
		SolicitudId cliente = guardar(EstadoSolicitud.CONVERTIDA, HOY.minus(java.time.Duration.ofDays(3000)));

		int borradas = useCase.aplicar();

		assertThat(borradas).isZero();
		assertThat(solicitudes.buscarPorId(cliente)).isPresent();
	}

	@Test
	void sinNadaQueBorrarNoHaceNadaYLoDice() {
		assertThat(useCase.aplicar()).isZero();
	}

}
