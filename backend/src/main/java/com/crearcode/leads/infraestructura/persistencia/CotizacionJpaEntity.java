package com.crearcode.leads.infraestructura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.crearcode.leads.dominio.EstadoCotizacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de persistencia de una cotización. Plano como el resto (sin
 * VOs); {@link CotizacionMapper} traduce en ambas direcciones. Los
 * totales no se guardan: se calculan desde los ítems.
 */
@Entity
@Table(name = "cotizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CotizacionJpaEntity {

	@Id
	private UUID id;

	private String numero;
	private UUID origenSolicitudId;

	private String clienteNombre;
	private String clienteCorreo;
	private String clienteTelefono;
	private String clienteIdentificacion;

	private int impuestoPorcentaje;

	@Enumerated(EnumType.STRING)
	private EstadoCotizacion estado;

	private String notas;

	private Instant creadaEn;
	private Instant validaHasta;
	private Instant enviadaEn;
	private Instant respondidaEn;

	// orphanRemoval: al reemplazar los ítems de un borrador, los viejos
	// se borran de verdad en vez de quedar huérfanos apuntando a nada.
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
	@JoinColumn(name = "cotizacion_id", nullable = false)
	@OrderBy("posicion ASC")
	private List<ItemDeCotizacionJpaEntity> items = new ArrayList<>();

	@Entity
	@Table(name = "items_de_cotizacion")
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	static class ItemDeCotizacionJpaEntity {

		@Id
		private UUID id;

		private int posicion;
		private String descripcion;
		private int cantidad;
		private BigDecimal valorUnitario;

	}

}
