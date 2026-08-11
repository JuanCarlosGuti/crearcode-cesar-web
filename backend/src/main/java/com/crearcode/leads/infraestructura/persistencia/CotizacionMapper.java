package com.crearcode.leads.infraestructura.persistencia;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.infraestructura.persistencia.CotizacionJpaEntity.ItemDeCotizacionJpaEntity;

final class CotizacionMapper {

	private CotizacionMapper() {
	}

	static CotizacionJpaEntity aEntidad(Cotizacion cotizacion) {
		CotizacionJpaEntity entidad = new CotizacionJpaEntity();
		entidad.setId(cotizacion.id().valor());
		entidad.setNumero(cotizacion.numero() == null ? null : cotizacion.numero().valor());
		entidad.setOrigenSolicitudId(cotizacion.origen() == null ? null : cotizacion.origen().valor());
		entidad.setClienteNombre(cotizacion.cliente().nombre());
		entidad.setClienteCorreo(cotizacion.cliente().correo().valor());
		entidad.setClienteTelefono(cotizacion.cliente().telefono());
		entidad.setClienteIdentificacion(cotizacion.cliente().identificacion());
		entidad.setImpuestoPorcentaje(cotizacion.impuesto().valor());
		entidad.setEstado(cotizacion.estado());
		entidad.setNotas(cotizacion.notas());
		entidad.setCreadaEn(cotizacion.creadaEn());
		entidad.setValidaHasta(cotizacion.validaHasta());
		entidad.setEnviadaEn(cotizacion.enviadaEn());
		entidad.setRespondidaEn(cotizacion.respondidaEn());

		List<ItemDeCotizacionJpaEntity> items = new ArrayList<>();
		List<ItemDeCotizacion> delDominio = cotizacion.items();
		for (int posicion = 0; posicion < delDominio.size(); posicion++) {
			ItemDeCotizacion item = delDominio.get(posicion);
			items.add(new ItemDeCotizacionJpaEntity(UUID.randomUUID(), posicion, item.descripcion(),
					item.cantidad(), item.valorUnitario().monto()));
		}
		entidad.setItems(items);
		return entidad;
	}

	static Cotizacion aDominio(CotizacionJpaEntity entidad) {
		List<ItemDeCotizacion> items = entidad.getItems().stream()
				.map(item -> new ItemDeCotizacion(item.getDescripcion(), item.getCantidad(),
						new Dinero(item.getValorUnitario())))
				.toList();

		return Cotizacion.reconstruir(
				new CotizacionId(entidad.getId()),
				new DatosDelCliente(entidad.getClienteNombre(), new Correo(entidad.getClienteCorreo()),
						entidad.getClienteTelefono(), entidad.getClienteIdentificacion()),
				new Porcentaje(entidad.getImpuestoPorcentaje()),
				entidad.getCreadaEn(),
				entidad.getValidaHasta(),
				entidad.getOrigenSolicitudId() == null ? null : new SolicitudId(entidad.getOrigenSolicitudId()),
				entidad.getNotas(),
				items,
				entidad.getEstado(),
				entidad.getNumero() == null ? null : new NumeroDeCotizacion(entidad.getNumero()),
				entidad.getEnviadaEn(),
				entidad.getRespondidaEn());
	}

}
