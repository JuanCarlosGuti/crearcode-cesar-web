package com.crearcode.leads.infraestructura.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.Entregable;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.Fase;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.Pago;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.QuienResponde;

/**
 * DTOs de la API de proyectos (F12). Como en cotizaciones, las
 * respuestas llevan el dinero YA calculado por el dominio —avance,
 * cobro con impuesto, pagado, pendiente, saldo—: el frontend lo muestra
 * y nunca lo calcula ni lo devuelve.
 *
 * <p>Los booleanos de las peticiones son {@code Boolean} y no
 * {@code boolean}: Jackson 3 rechaza un primitivo ausente con un 400, y
 * eso ya costó un incidente con la casilla comercial (auditoría del 28
 * sep 2026).
 */
final class ProyectoDtos {

	private ProyectoDtos() {
	}

	// --- Peticiones ---

	record DescripcionRequest(@NotBlank String nombre, String descripcion, @NotNull LocalDate inicio,
			LocalDate entregaEstimada) {

		DescripcionDelProyecto aDominio() {
			return new DescripcionDelProyecto(nombre, descripcion, inicio, entregaEstimada);
		}
	}

	record FaseRequest(@NotBlank String nombre, String objetivo, LocalDate inicioPlaneado, LocalDate finPlaneado) {

		DatosDeFase aDominio() {
			return new DatosDeFase(nombre, objetivo, inicioPlaneado, finPlaneado);
		}

		static FaseRequest desde(DatosDeFase datos) {
			return new FaseRequest(datos.nombre(), datos.objetivo(), datos.inicioPlaneado(), datos.finPlaneado());
		}
	}

	record EntregableRequest(@NotBlank String nombre, String descripcion, @NotNull @PositiveOrZero BigDecimal valor,
			@NotNull MomentoDeCobro momentoDeCobro) {

		DatosDeEntregable aDominio() {
			return new DatosDeEntregable(nombre, descripcion, new Dinero(valor), momentoDeCobro);
		}

		static EntregableRequest desde(DatosDeEntregable datos) {
			return new EntregableRequest(datos.nombre(), datos.descripcion(), datos.valor().monto(), datos.cobro());
		}
	}

	/** Una fase del plan con sus entregables. Es también la forma de la propuesta desde una cotización. */
	record NuevaFaseRequest(@Valid @NotNull FaseRequest fase, @Valid List<EntregableRequest> entregables) {

		NuevaFase aDominio() {
			return new NuevaFase(fase.aDominio(),
					entregables == null ? List.of() : entregables.stream().map(EntregableRequest::aDominio).toList());
		}

		static NuevaFaseRequest desde(NuevaFase fase) {
			return new NuevaFaseRequest(FaseRequest.desde(fase.datos()),
					fase.entregables().stream().map(EntregableRequest::desde).toList());
		}

		static List<NuevaFase> aDominio(List<NuevaFaseRequest> plan) {
			return plan == null ? List.of() : plan.stream().map(NuevaFaseRequest::aDominio).toList();
		}
	}

	record ClienteDelProyectoRequest(@NotBlank String nombre, @NotBlank String correo) {
	}

	/**
	 * Desde una cotización ({@code cotizacionId}) o en blanco
	 * ({@code cliente} e impuesto, que por defecto es el de las
	 * cotizaciones).
	 */
	record NuevoProyectoRequest(UUID cotizacionId, @Valid ClienteDelProyectoRequest cliente,
			Integer impuestoPorcentaje, @Valid @NotNull DescripcionRequest descripcion,
			@Valid List<NuevaFaseRequest> plan) {
	}

	record NuevoEntregableRequest(@Valid @NotNull EntregableRequest entregable, Boolean esCambioDeAlcance) {
	}

	record PosicionRequest(@NotNull Integer posicion) {
	}

	record MovimientoDeEntregableRequest(@NotNull UUID faseId, @NotNull Integer posicion) {
	}

	record ResumenRequest(@NotBlank String resumen) {
	}

	/** {@code url} nula quita el enlace. */
	record DemoRequest(String url) {
	}

	record CambioDeEstadoRequest(@NotNull EstadoEntregable estado, String nota) {
	}

	record PagoRequest(@NotNull UUID entregableId, @NotNull @Positive BigDecimal monto, @NotNull LocalDate fecha,
			@NotNull MedioDePago medio, String referencia) {
	}

	record AjustesRequest(String nota) {
	}

	// --- Respuestas ---

	record EntregableResponse(UUID id, String nombre, String descripcion, BigDecimal valor, BigDecimal impuesto,
			BigDecimal cobro, MomentoDeCobro momentoDeCobro, boolean esCobrable, BigDecimal pagado,
			BigDecimal pendiente, boolean esCambioDeAlcance, EstadoEntregable estado, String urlDemo,
			String notaDeAjustes, Instant aprobadoEn, QuienResponde aprobadoPor) {

		static EntregableResponse desde(Proyecto proyecto, Entregable entregable) {
			return new EntregableResponse(
					entregable.id().valor(),
					entregable.nombre(),
					entregable.descripcion(),
					entregable.valor().monto(),
					proyecto.impuestoDe(entregable.id()).monto(),
					proyecto.cobroDe(entregable.id()).monto(),
					entregable.cobro(),
					proyecto.esCobrable(entregable.id()),
					proyecto.pagadoDe(entregable.id()).monto(),
					proyecto.pendienteDe(entregable.id()).monto(),
					entregable.esCambioDeAlcance(),
					entregable.estado(),
					entregable.demo() == null ? null : entregable.demo().valor(),
					entregable.notaDeAjustes(),
					entregable.aprobadoEn(),
					entregable.aprobadoPor());
		}
	}

	record FaseResponse(UUID id, String nombre, String objetivo, LocalDate inicioPlaneado, LocalDate finPlaneado,
			String resumenParaElCliente, List<EntregableResponse> entregables) {

		static FaseResponse desde(Proyecto proyecto, Fase fase) {
			return new FaseResponse(fase.id().valor(), fase.nombre(), fase.datos().objetivo(),
					fase.datos().inicioPlaneado(), fase.datos().finPlaneado(), fase.resumenParaElCliente(),
					fase.entregables().stream().map(entregable -> EntregableResponse.desde(proyecto, entregable))
							.toList());
		}
	}

	/** Sin quién lo registró: es un dato interno del equipo, no del cliente. */
	record PagoResponse(UUID id, UUID entregableId, BigDecimal monto, LocalDate fecha, MedioDePago medio,
			OrigenDePago origen, String referencia) {

		static PagoResponse desde(Pago pago) {
			return new PagoResponse(pago.id().valor(), pago.entregable().valor(), pago.monto().monto(), pago.fecha(),
					pago.medio(), pago.origen(), pago.referencia());
		}
	}

	/**
	 * El proyecto completo. {@code clienteTieneCuenta} solo va en la vista
	 * del equipo (HU-53); en la del cliente es nulo.
	 */
	record ProyectoResponse(UUID id, String nombre, String descripcion, LocalDate inicio, LocalDate entregaEstimada,
			String clienteNombre, String clienteCorreo, UUID origenCotizacionId, int impuestoPorcentaje,
			EstadoProyecto estado, Instant finDeGarantia, int avance, BigDecimal total, BigDecimal totalPagado,
			BigDecimal saldo, BigDecimal pendienteDePago, Instant creadoEn, Instant actualizadoEn,
			List<FaseResponse> fases, List<PagoResponse> pagos, Boolean clienteTieneCuenta) {

		static ProyectoResponse desde(Proyecto proyecto, Boolean clienteTieneCuenta) {
			DescripcionDelProyecto descripcion = proyecto.descripcion();
			return new ProyectoResponse(
					proyecto.id().valor(),
					descripcion.nombre(),
					descripcion.descripcion(),
					descripcion.inicio(),
					descripcion.entregaEstimada(),
					proyecto.nombreDelCliente(),
					proyecto.correoDelCliente().valor(),
					proyecto.origen() == null ? null : proyecto.origen().valor(),
					proyecto.impuesto().valor(),
					proyecto.estado(),
					proyecto.finDeGarantia(),
					proyecto.avance(),
					proyecto.total().monto(),
					proyecto.totalPagado().monto(),
					proyecto.saldo().monto(),
					proyecto.pendienteDePago().monto(),
					proyecto.creadoEn(),
					proyecto.actualizadoEn(),
					proyecto.fases().stream().map(fase -> FaseResponse.desde(proyecto, fase)).toList(),
					proyecto.pagos().stream().map(PagoResponse::desde).toList(),
					clienteTieneCuenta);
		}
	}

	record ProyectoResumenResponse(UUID id, String nombre, String clienteNombre, String clienteCorreo,
			EstadoProyecto estado, int avance, BigDecimal total, BigDecimal saldo, BigDecimal pendienteDePago,
			Instant actualizadoEn) {

		static ProyectoResumenResponse desde(Proyecto proyecto) {
			return new ProyectoResumenResponse(proyecto.id().valor(), proyecto.descripcion().nombre(),
					proyecto.nombreDelCliente(), proyecto.correoDelCliente().valor(), proyecto.estado(),
					proyecto.avance(), proyecto.total().monto(), proyecto.saldo().monto(),
					proyecto.pendienteDePago().monto(), proyecto.actualizadoEn());
		}
	}

}
