package com.crearcode.leads.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Una fase (sprint) del proyecto, con sus entregables en orden. Entidad
 * interna del agregado {@link Proyecto}: solo el proyecto la modifica.
 */
public final class Fase {

	private final FaseId id;
	private DatosDeFase datos;
	private String resumenParaElCliente;
	private final List<Entregable> entregables;

	private Fase(FaseId id, DatosDeFase datos, String resumenParaElCliente, List<Entregable> entregables) {
		this.id = id;
		this.datos = datos;
		this.resumenParaElCliente = resumenParaElCliente;
		this.entregables = new ArrayList<>(entregables);
	}

	static Fase nueva(DatosDeFase datos) {
		return new Fase(FaseId.nuevo(), datos, null, List.of());
	}

	public static Fase reconstruir(FaseId id, DatosDeFase datos, String resumenParaElCliente,
			List<Entregable> entregables) {
		return new Fase(id, datos, resumenParaElCliente, entregables);
	}

	void editar(DatosDeFase nuevos) {
		this.datos = nuevos;
	}

	void escribirResumen(String resumen) {
		this.resumenParaElCliente = TextoDeProyecto.obligatorio(resumen, 2000, "El resumen de la fase");
	}

	void agregar(Entregable entregable, int posicion) {
		entregables.add(Math.clamp(posicion, 0, entregables.size()), entregable);
	}

	void agregarAlFinal(Entregable entregable) {
		entregables.add(entregable);
	}

	void quitar(Entregable entregable) {
		entregables.remove(entregable);
	}

	Optional<Entregable> buscar(EntregableId id) {
		return entregables.stream().filter(entregable -> entregable.id().equals(id)).findFirst();
	}

	boolean estaVacia() {
		return entregables.isEmpty();
	}

	public FaseId id() {
		return id;
	}

	public DatosDeFase datos() {
		return datos;
	}

	public String nombre() {
		return datos.nombre();
	}

	public String resumenParaElCliente() {
		return resumenParaElCliente;
	}

	public List<Entregable> entregables() {
		return List.copyOf(entregables);
	}

}
