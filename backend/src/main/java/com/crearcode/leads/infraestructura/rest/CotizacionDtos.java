package com.crearcode.leads.infraestructura.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.ItemDeCotizacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTOs de la API de cotizaciones (F11). Las respuestas llevan los
 * totales YA calculados por el dominio: el frontend los muestra, no los
 * calcula, y nunca los envía de vuelta.
 */
final class CotizacionDtos {

	private CotizacionDtos() {
	}

	record ItemRequest(@NotBlank String descripcion, @Min(1) int cantidad,
			@NotNull @PositiveOrZero BigDecimal valorUnitario) {
	}

	record ClienteRequest(@NotBlank String nombre, @NotBlank String correo, String telefono,
			String identificacion) {
	}

	record NuevoBorradorRequest(UUID origenSolicitudId, @Valid ClienteRequest cliente,
			Integer impuestoPorcentaje, Integer diasDeValidez, String notas,
			@Valid List<ItemRequest> items) {
	}

	record EditarBorradorRequest(@Valid List<ItemRequest> items, String notas) {
	}

	record ItemResponse(String descripcion, int cantidad, BigDecimal valorUnitario, BigDecimal subtotal) {

		static ItemResponse desde(ItemDeCotizacion item) {
			return new ItemResponse(item.descripcion(), item.cantidad(), item.valorUnitario().monto(),
					item.subtotal().monto());
		}
	}

	record CotizacionResponse(UUID id, String numero, EstadoCotizacion estado, UUID origenSolicitudId,
			String clienteNombre, String clienteCorreo, String clienteTelefono,
			String clienteIdentificacion, int impuestoPorcentaje, String notas, Instant creadaEn,
			Instant validaHasta, Instant enviadaEn, Instant respondidaEn, List<ItemResponse> items,
			BigDecimal subtotal, BigDecimal impuesto, BigDecimal total) {

		static CotizacionResponse desde(Cotizacion cotizacion) {
			return new CotizacionResponse(
					cotizacion.id().valor(),
					cotizacion.numero() == null ? null : cotizacion.numero().valor(),
					cotizacion.estado(),
					cotizacion.origen() == null ? null : cotizacion.origen().valor(),
					cotizacion.cliente().nombre(),
					cotizacion.cliente().correo().valor(),
					cotizacion.cliente().telefono(),
					cotizacion.cliente().identificacion(),
					cotizacion.impuesto().valor(),
					cotizacion.notas(),
					cotizacion.creadaEn(),
					cotizacion.validaHasta(),
					cotizacion.enviadaEn(),
					cotizacion.respondidaEn(),
					cotizacion.items().stream().map(ItemResponse::desde).toList(),
					cotizacion.subtotal().monto(),
					cotizacion.impuestoCalculado().monto(),
					cotizacion.total().monto());
		}
	}

}
