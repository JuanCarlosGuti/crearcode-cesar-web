package com.crearcode.leads.dominio;

import java.time.Instant;

/**
 * Un entregable del proyecto (fase F12). Es una entidad interna del
 * agregado {@link Proyecto}: sus mutadores son de paquete y solo los
 * llama el proyecto, que es quien conoce las reglas que cruzan
 * entregables (el avance, la garantía, los pagos).
 */
public final class Entregable {

	private final EntregableId id;
	private final boolean esCambioDeAlcance;
	private DatosDeEntregable datos;
	private EstadoEntregable estado;
	private UrlDeDemo demo;
	private String notaDeAjustes;
	private Instant aprobadoEn;
	private QuienResponde aprobadoPor;

	private Entregable(EntregableId id, DatosDeEntregable datos, boolean esCambioDeAlcance, EstadoEntregable estado,
			UrlDeDemo demo, String notaDeAjustes, Instant aprobadoEn, QuienResponde aprobadoPor) {
		this.id = id;
		this.datos = datos;
		this.esCambioDeAlcance = esCambioDeAlcance;
		this.estado = estado;
		this.demo = demo;
		this.notaDeAjustes = notaDeAjustes;
		this.aprobadoEn = aprobadoEn;
		this.aprobadoPor = aprobadoPor;
	}

	static Entregable nuevo(DatosDeEntregable datos, boolean esCambioDeAlcance) {
		return new Entregable(EntregableId.nuevo(), datos, esCambioDeAlcance, EstadoEntregable.PENDIENTE, null, null,
				null, null);
	}

	public static Entregable reconstruir(EntregableId id, DatosDeEntregable datos, boolean esCambioDeAlcance,
			EstadoEntregable estado, UrlDeDemo demo, String notaDeAjustes, Instant aprobadoEn,
			QuienResponde aprobadoPor) {
		return new Entregable(id, datos, esCambioDeAlcance, estado, demo, notaDeAjustes, aprobadoEn, aprobadoPor);
	}

	void editar(DatosDeEntregable nuevos) {
		this.datos = nuevos;
	}

	void ponerDemo(UrlDeDemo nueva) {
		this.demo = nueva;
	}

	void transicionarA(EstadoEntregable destino, String nota, QuienResponde quien, Instant ahora) {
		if (!estado.puedeTransicionarA(destino)) {
			throw new TransicionDeEstadoInvalidaException(
					"El entregable no puede pasar de " + estado + " a " + destino);
		}
		if (destino == EstadoEntregable.CON_AJUSTES) {
			this.notaDeAjustes = TextoDeProyecto.obligatorio(nota, 1000, "La nota de qué se ajusta");
		}
		if (destino == EstadoEntregable.APROBADO) {
			this.aprobadoEn = ahora;
			this.aprobadoPor = quien;
		}
		this.estado = destino;
	}

	boolean estaPendiente() {
		return estado == EstadoEntregable.PENDIENTE;
	}

	boolean estaAprobado() {
		return estado == EstadoEntregable.APROBADO;
	}

	public EntregableId id() {
		return id;
	}

	public DatosDeEntregable datos() {
		return datos;
	}

	public String nombre() {
		return datos.nombre();
	}

	public String descripcion() {
		return datos.descripcion();
	}

	public Dinero valor() {
		return datos.valor();
	}

	public MomentoDeCobro cobro() {
		return datos.cobro();
	}

	public boolean esCambioDeAlcance() {
		return esCambioDeAlcance;
	}

	public EstadoEntregable estado() {
		return estado;
	}

	public UrlDeDemo demo() {
		return demo;
	}

	public String notaDeAjustes() {
		return notaDeAjustes;
	}

	public Instant aprobadoEn() {
		return aprobadoEn;
	}

	public QuienResponde aprobadoPor() {
		return aprobadoPor;
	}

}
