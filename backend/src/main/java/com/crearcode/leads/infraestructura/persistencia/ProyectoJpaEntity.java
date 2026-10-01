package com.crearcode.leads.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.EstadoProyecto;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.QuienResponde;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de persistencia de un proyecto (F12). Plano como el resto, sin
 * VOs; {@link ProyectoMapper} traduce en ambas direcciones. Ningún total
 * se guarda: se calculan en el dominio.
 *
 * <p>Las colecciones son perezosas a propósito: dos colecciones
 * ansiosas en la misma entidad (fases y pagos) no se pueden traer en una
 * sola consulta. El adaptador lee dentro de su propia transacción.
 */
@Entity
@Table(name = "proyectos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ProyectoJpaEntity {

	@Id
	private UUID id;

	private String clienteCorreo;
	private String clienteNombre;
	private UUID origenCotizacionId;

	private String nombre;
	private String descripcion;
	private LocalDate inicio;
	private LocalDate entregaEstimada;

	private int impuestoPorcentaje;

	@Enumerated(EnumType.STRING)
	private EstadoProyecto estado;

	private Instant finDeGarantia;
	private Instant creadoEn;
	private Instant actualizadoEn;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "proyecto_id", nullable = false)
	@OrderBy("posicion ASC")
	private List<FaseJpaEntity> fases = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "proyecto_id", nullable = false)
	@OrderBy("posicion ASC")
	private List<PagoJpaEntity> pagos = new ArrayList<>();

	@Entity
	@Table(name = "fases_de_proyecto")
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	static class FaseJpaEntity {

		@Id
		private UUID id;

		private int posicion;
		private String nombre;
		private String objetivo;
		private LocalDate inicioPlaneado;
		private LocalDate finPlaneado;
		private String resumenParaElCliente;

		@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
		@JoinColumn(name = "fase_id", nullable = false)
		@OrderBy("posicion ASC")
		private List<EntregableJpaEntity> entregables = new ArrayList<>();

	}

	@Entity
	@Table(name = "entregables_de_proyecto")
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	static class EntregableJpaEntity {

		@Id
		private UUID id;

		private int posicion;
		private String nombre;
		private String descripcion;
		private BigDecimal valor;

		@Enumerated(EnumType.STRING)
		private MomentoDeCobro momentoDeCobro;

		private boolean esCambioDeAlcance;

		@Enumerated(EnumType.STRING)
		private EstadoEntregable estado;

		private String urlDemo;
		private String notaDeAjustes;
		private Instant aprobadoEn;

		@Enumerated(EnumType.STRING)
		private QuienResponde aprobadoPor;

	}

	@Entity
	@Table(name = "pagos_de_proyecto")
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	static class PagoJpaEntity {

		@Id
		private UUID id;

		private UUID entregableId;
		private int posicion;
		private BigDecimal monto;
		private LocalDate fecha;

		@Enumerated(EnumType.STRING)
		private MedioDePago medio;

		@Enumerated(EnumType.STRING)
		private OrigenDePago origen;

		private String referencia;
		private String registradoPor;

	}

}
