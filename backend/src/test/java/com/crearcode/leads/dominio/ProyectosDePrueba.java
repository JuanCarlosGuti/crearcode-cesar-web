package com.crearcode.leads.dominio;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos de partida para los tests del agregado {@link Proyecto}. Montos
 * redondos a propósito: el impuesto del 19 % sobre ellos da cifras que
 * se pueden verificar de cabeza.
 */
final class ProyectosDePrueba {

	static final Instant AHORA = Instant.parse("2026-10-01T15:00:00Z");
	static final Instant DESPUES = AHORA.plus(Duration.ofDays(1));
	static final Correo CLIENTE = new Correo("cliente@ejemplo.co");
	static final Correo OTRO_CLIENTE = new Correo("otro@ejemplo.co");
	static final Correo EQUIPO = new Correo("admin@crearcodecesar.com");
	static final Porcentaje IVA = new Porcentaje(19);
	static final Porcentaje SIN_IMPUESTO = new Porcentaje(0);

	private ProyectosDePrueba() {
	}

	static DescripcionDelProyecto descripcion() {
		return new DescripcionDelProyecto("Tienda en línea de Café Valle", "Tu tienda para vender café por internet",
				LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15));
	}

	static DatosDeFase fase(String nombre) {
		return new DatosDeFase(nombre, "Que puedas ver el avance", LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 10, 15));
	}

	static DatosDeEntregable entregable(String nombre, long valor, MomentoDeCobro cobro) {
		return new DatosDeEntregable(nombre, "Descripción para el cliente", Dinero.de(valor), cobro);
	}

	/** Proyecto en blanco con una fase y dos entregables: 1 M al iniciar y 3 M al aprobar. */
	static Proyecto enBlancoConDosEntregables(Porcentaje impuesto) {
		return Proyecto.enBlanco(CLIENTE, "Café Valle S.A.S.", descripcion(), impuesto,
				List.of(new NuevaFase(fase("Fase 1"), List.of(
						entregable("Diseño", 1_000_000, MomentoDeCobro.AL_INICIAR),
						entregable("Catálogo", 3_000_000, MomentoDeCobro.AL_APROBAR)))),
				AHORA);
	}

	static Proyecto vacio() {
		return Proyecto.enBlanco(CLIENTE, "Café Valle S.A.S.", descripcion(), IVA, List.of(), AHORA);
	}

	static EntregableId idDe(Proyecto proyecto, String nombre) {
		return proyecto.entregables().stream()
				.filter(entregable -> entregable.nombre().equals(nombre))
				.findFirst()
				.orElseThrow()
				.id();
	}

	/** Lleva un entregable de PENDIENTE a EN_REVISION, como lo haría el equipo. */
	static void llevarARevision(Proyecto proyecto, EntregableId id) {
		proyecto.cambiarEstadoDeEntregable(id, EstadoEntregable.EN_CURSO, null, AHORA);
		proyecto.cambiarEstadoDeEntregable(id, EstadoEntregable.EN_REVISION, null, AHORA);
	}

	static void aprobarComoEquipo(Proyecto proyecto, EntregableId id) {
		llevarARevision(proyecto, id);
		proyecto.cambiarEstadoDeEntregable(id, EstadoEntregable.APROBADO, null, AHORA);
	}

	/** Cotización ACEPTADA con dos ítems: 2 × 1 M y 1 × 3 M, subtotal 5 M. */
	static Cotizacion cotizacionAceptada() {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Café Valle S.A.S.", CLIENTE, null, null), IVA, AHORA.minus(Duration.ofDays(5)),
				AHORA.plus(Duration.ofDays(10)), null, null);
		cotizacion.agregarItem(new ItemDeCotizacion("Módulo de diseño", 2, Dinero.de(1_000_000)));
		cotizacion.agregarItem(new ItemDeCotizacion("Catálogo de productos", 1, Dinero.de(3_000_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 1), AHORA.minus(Duration.ofDays(4)));
		cotizacion.aceptar(AHORA.minus(Duration.ofDays(1)));
		return cotizacion;
	}

}
