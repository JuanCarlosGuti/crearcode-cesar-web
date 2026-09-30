package com.crearcode.leads.infraestructura.persistencia;

import java.util.ArrayList;
import java.util.List;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.Entregable;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.Fase;
import com.crearcode.leads.dominio.FaseId;
import com.crearcode.leads.dominio.Pago;
import com.crearcode.leads.dominio.PagoId;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.UrlDeDemo;
import com.crearcode.leads.infraestructura.persistencia.ProyectoJpaEntity.EntregableJpaEntity;
import com.crearcode.leads.infraestructura.persistencia.ProyectoJpaEntity.FaseJpaEntity;
import com.crearcode.leads.infraestructura.persistencia.ProyectoJpaEntity.PagoJpaEntity;

/**
 * Traduce el agregado {@link Proyecto} a su modelo de persistencia y de
 * vuelta. Los identificadores de fases, entregables y pagos son los del
 * dominio —a diferencia de los ítems de cotización—, porque el portal y
 * los pagos los referencian.
 */
final class ProyectoMapper {

	private ProyectoMapper() {
	}

	static ProyectoJpaEntity aEntidad(Proyecto proyecto) {
		ProyectoJpaEntity entidad = new ProyectoJpaEntity();
		entidad.setId(proyecto.id().valor());
		entidad.setClienteCorreo(proyecto.correoDelCliente().valor());
		entidad.setClienteNombre(proyecto.nombreDelCliente());
		entidad.setOrigenCotizacionId(proyecto.origen() == null ? null : proyecto.origen().valor());
		DescripcionDelProyecto descripcion = proyecto.descripcion();
		entidad.setNombre(descripcion.nombre());
		entidad.setDescripcion(descripcion.descripcion());
		entidad.setInicio(descripcion.inicio());
		entidad.setEntregaEstimada(descripcion.entregaEstimada());
		entidad.setImpuestoPorcentaje(proyecto.impuesto().valor());
		entidad.setEstado(proyecto.estado());
		entidad.setFinDeGarantia(proyecto.finDeGarantia());
		entidad.setCreadoEn(proyecto.creadoEn());
		entidad.setActualizadoEn(proyecto.actualizadoEn());

		List<FaseJpaEntity> fases = new ArrayList<>();
		List<Fase> delDominio = proyecto.fases();
		for (int posicion = 0; posicion < delDominio.size(); posicion++) {
			fases.add(aEntidad(delDominio.get(posicion), posicion));
		}
		entidad.setFases(fases);

		List<PagoJpaEntity> pagos = new ArrayList<>();
		List<Pago> pagosDelDominio = proyecto.pagos();
		for (int posicion = 0; posicion < pagosDelDominio.size(); posicion++) {
			Pago pago = pagosDelDominio.get(posicion);
			pagos.add(new PagoJpaEntity(pago.id().valor(), pago.entregable().valor(), posicion, pago.monto().monto(),
					pago.fecha(), pago.medio(), pago.origen(), pago.referencia(), pago.registradoPor().valor()));
		}
		entidad.setPagos(pagos);
		return entidad;
	}

	private static FaseJpaEntity aEntidad(Fase fase, int posicion) {
		DatosDeFase datos = fase.datos();
		List<EntregableJpaEntity> entregables = new ArrayList<>();
		List<Entregable> delDominio = fase.entregables();
		for (int orden = 0; orden < delDominio.size(); orden++) {
			Entregable entregable = delDominio.get(orden);
			entregables.add(new EntregableJpaEntity(entregable.id().valor(), orden, entregable.nombre(),
					entregable.descripcion(), entregable.valor().monto(), entregable.cobro(),
					entregable.esCambioDeAlcance(), entregable.estado(),
					entregable.demo() == null ? null : entregable.demo().valor(), entregable.notaDeAjustes(),
					entregable.aprobadoEn(), entregable.aprobadoPor()));
		}
		return new FaseJpaEntity(fase.id().valor(), posicion, datos.nombre(), datos.objetivo(),
				datos.inicioPlaneado(), datos.finPlaneado(), fase.resumenParaElCliente(), entregables);
	}

	static Proyecto aDominio(ProyectoJpaEntity entidad) {
		List<Fase> fases = entidad.getFases().stream().map(ProyectoMapper::aDominio).toList();
		List<Pago> pagos = entidad.getPagos().stream()
				.map(pago -> new Pago(new PagoId(pago.getId()), new EntregableId(pago.getEntregableId()),
						new Dinero(pago.getMonto()), pago.getFecha(), pago.getMedio(), pago.getOrigen(),
						pago.getReferencia(), new Correo(pago.getRegistradoPor())))
				.toList();

		return Proyecto.reconstruir(
				new ProyectoId(entidad.getId()),
				new Correo(entidad.getClienteCorreo()),
				entidad.getClienteNombre(),
				entidad.getOrigenCotizacionId() == null ? null : new CotizacionId(entidad.getOrigenCotizacionId()),
				new Porcentaje(entidad.getImpuestoPorcentaje()),
				new DescripcionDelProyecto(entidad.getNombre(), entidad.getDescripcion(), entidad.getInicio(),
						entidad.getEntregaEstimada()),
				entidad.getEstado(),
				entidad.getFinDeGarantia(),
				fases,
				pagos,
				entidad.getCreadoEn(),
				entidad.getActualizadoEn());
	}

	private static Fase aDominio(FaseJpaEntity fase) {
		List<Entregable> entregables = fase.getEntregables().stream()
				.map(entregable -> Entregable.reconstruir(
						new EntregableId(entregable.getId()),
						new DatosDeEntregable(entregable.getNombre(), entregable.getDescripcion(),
								new Dinero(entregable.getValor()), entregable.getMomentoDeCobro()),
						entregable.isEsCambioDeAlcance(),
						entregable.getEstado(),
						entregable.getUrlDemo() == null ? null : new UrlDeDemo(entregable.getUrlDemo()),
						entregable.getNotaDeAjustes(),
						entregable.getAprobadoEn(),
						entregable.getAprobadoPor()))
				.toList();
		return Fase.reconstruir(new FaseId(fase.getId()),
				new DatosDeFase(fase.getNombre(), fase.getObjetivo(), fase.getInicioPlaneado(), fase.getFinPlaneado()),
				fase.getResumenParaElCliente(), entregables);
	}

}
