package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;

/** Datos de partida compartidos por los tests de los casos de uso de proyectos. */
final class CasosDeProyectoDePrueba {

	/** 10:00 en Bogotá. */
	static final Instant AHORA = Instant.parse("2026-10-01T15:00:00Z");
	static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
	static final Clock RELOJ = Clock.fixed(AHORA, BOGOTA);
	static final LocalDate HOY = LocalDate.of(2026, 10, 1);
	static final Correo CLIENTE = new Correo("cliente@ejemplo.co");
	static final Correo OTRO_CLIENTE = new Correo("otro@ejemplo.co");
	static final Correo EQUIPO = new Correo("admin@crearcodecesar.com");
	static final Porcentaje IVA = new Porcentaje(19);

	private CasosDeProyectoDePrueba() {
	}

	static DescripcionDelProyecto descripcion() {
		return new DescripcionDelProyecto("Tienda de Café Valle", null, HOY, HOY.plusMonths(2));
	}

	static Proyecto proyectoConDosEntregables() {
		return Proyecto.enBlanco(CLIENTE, "Café Valle S.A.S.", descripcion(), IVA,
				List.of(new NuevaFase(new DatosDeFase("Fase 1", null, null, null), List.of(
						new DatosDeEntregable("Diseño", null, Dinero.de(1_000_000), MomentoDeCobro.AL_INICIAR),
						new DatosDeEntregable("Catálogo", null, Dinero.de(3_000_000), MomentoDeCobro.AL_APROBAR)))),
				AHORA.minus(Duration.ofDays(3)));
	}

	static EntregableId idDe(Proyecto proyecto, String nombre) {
		return proyecto.entregables().stream()
				.filter(entregable -> entregable.nombre().equals(nombre))
				.findFirst()
				.orElseThrow()
				.id();
	}

	static void llevarARevision(Proyecto proyecto, EntregableId id) {
		Instant antes = AHORA.minus(Duration.ofDays(1));
		proyecto.cambiarEstadoDeEntregable(id, EstadoEntregable.EN_CURSO, null, antes);
		proyecto.cambiarEstadoDeEntregable(id, EstadoEntregable.EN_REVISION, null, antes);
	}

	/** ACEPTADA, con "Módulo de diseño" 2 × 1 M y "Catálogo de productos" 1 × 3 M. */
	static Cotizacion cotizacionAceptada() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(new DatosDelCliente("Café Valle S.A.S.", CLIENTE, null, null),
				IVA, AHORA.minus(Duration.ofDays(5)), AHORA.plus(Duration.ofDays(10)), null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Módulo de diseño", 2, Dinero.de(1_000_000)));
		cotizacion.agregarItem(new ItemDeCotizacion("Catálogo de productos", 1, Dinero.de(3_000_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 1), AHORA.minus(Duration.ofDays(4)));
		cotizacion.aceptar(AHORA.minus(Duration.ofDays(1)));
		return cotizacion;
	}

}
