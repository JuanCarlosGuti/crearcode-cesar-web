package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.function.BiConsumer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.FaseId;
import com.crearcode.leads.dominio.GestionarProyectoUseCase;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.NuevoPago;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.PagoId;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoInvalidoException;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.UrlDeDemo;

/**
 * Lo que el equipo hace sobre un proyecto desde el panel (HU-54 a
 * HU-56). Casi todo es cargar, delegar en el agregado y guardar: las
 * reglas viven en {@link Proyecto}. Lo propio de esta capa es el reloj,
 * los eventos que disparan los correos y que un pago no se registre con
 * fecha futura.
 */
@Service
class GestionarProyectoUseCaseImpl implements GestionarProyectoUseCase {

	private final ProyectoRepositorio proyectos;
	private final ApplicationEventPublisher eventos;
	private final Clock reloj;

	GestionarProyectoUseCaseImpl(ProyectoRepositorio proyectos, ApplicationEventPublisher eventos, Clock reloj) {
		this.proyectos = proyectos;
		this.eventos = eventos;
		this.reloj = reloj;
	}

	@Override
	@Transactional
	public Proyecto cambiarDescripcion(ProyectoId id, DescripcionDelProyecto descripcion) {
		return modificar(id, (proyecto, ahora) -> proyecto.cambiarDescripcion(descripcion, ahora));
	}

	@Override
	@Transactional
	public Proyecto agregarFase(ProyectoId id, DatosDeFase datos) {
		return modificar(id, (proyecto, ahora) -> proyecto.agregarFase(datos, ahora));
	}

	@Override
	@Transactional
	public Proyecto editarFase(ProyectoId id, FaseId fase, DatosDeFase datos) {
		return modificar(id, (proyecto, ahora) -> proyecto.editarFase(fase, datos, ahora));
	}

	@Override
	@Transactional
	public Proyecto moverFase(ProyectoId id, FaseId fase, int posicion) {
		return modificar(id, (proyecto, ahora) -> proyecto.moverFase(fase, posicion, ahora));
	}

	@Override
	@Transactional
	public Proyecto quitarFase(ProyectoId id, FaseId fase) {
		return modificar(id, (proyecto, ahora) -> proyecto.quitarFase(fase, ahora));
	}

	@Override
	@Transactional
	public Proyecto escribirResumenDeFase(ProyectoId id, FaseId fase, String resumen) {
		return modificar(id, (proyecto, ahora) -> proyecto.escribirResumenDeFase(fase, resumen, ahora));
	}

	@Override
	@Transactional
	public Proyecto agregarEntregable(ProyectoId id, FaseId fase, DatosDeEntregable datos,
			boolean esCambioDeAlcance) {
		return modificar(id,
				(proyecto, ahora) -> proyecto.agregarEntregable(fase, datos, esCambioDeAlcance, ahora));
	}

	@Override
	@Transactional
	public Proyecto editarEntregable(ProyectoId id, EntregableId entregable, DatosDeEntregable datos) {
		return modificar(id, (proyecto, ahora) -> proyecto.editarEntregable(entregable, datos, ahora));
	}

	@Override
	@Transactional
	public Proyecto ponerDemo(ProyectoId id, EntregableId entregable, UrlDeDemo demo) {
		return modificar(id, (proyecto, ahora) -> proyecto.ponerDemo(entregable, demo, ahora));
	}

	@Override
	@Transactional
	public Proyecto moverEntregable(ProyectoId id, EntregableId entregable, FaseId destino, int posicion) {
		return modificar(id,
				(proyecto, ahora) -> proyecto.moverEntregable(entregable, destino, posicion, ahora));
	}

	@Override
	@Transactional
	public Proyecto quitarEntregable(ProyectoId id, EntregableId entregable) {
		return modificar(id, (proyecto, ahora) -> proyecto.quitarEntregable(entregable, ahora));
	}

	/** HU-52: pasar a revisión es lo que le avisa al cliente; lo demás no le escribe. */
	@Override
	@Transactional
	public Proyecto cambiarEstadoDeEntregable(ProyectoId id, EntregableId entregable, EstadoEntregable destino,
			String nota) {
		Proyecto proyecto = modificar(id,
				(cargado, ahora) -> cargado.cambiarEstadoDeEntregable(entregable, destino, nota, ahora));
		if (destino == EstadoEntregable.EN_REVISION) {
			eventos.publishEvent(new EntregableListoParaRevisar(proyecto, entregable));
		}
		return proyecto;
	}

	@Override
	@Transactional
	public Proyecto registrarPago(ProyectoId id, EntregableId entregable, Dinero monto, LocalDate fecha,
			MedioDePago medio, String referencia, Correo registradoPor) {
		if (fecha != null && fecha.isAfter(LocalDate.now(reloj))) {
			throw new ProyectoInvalidoException("La fecha del pago no puede ser futura");
		}
		NuevoPago nuevo = new NuevoPago(entregable, monto, fecha, medio, OrigenDePago.MANUAL, referencia,
				registradoPor);
		Proyecto proyecto = cargar(id);
		PagoId pago = proyecto.registrarPago(nuevo, Instant.now(reloj));
		proyectos.guardar(proyecto);
		eventos.publishEvent(new PagoRegistrado(proyecto, pago));
		return proyecto;
	}

	@Override
	@Transactional
	public Proyecto pausar(ProyectoId id) {
		return modificar(id, Proyecto::pausar);
	}

	@Override
	@Transactional
	public Proyecto reanudar(ProyectoId id) {
		return modificar(id, Proyecto::reanudar);
	}

	@Override
	@Transactional
	public Proyecto cerrar(ProyectoId id) {
		return modificar(id, Proyecto::cerrar);
	}

	/**
	 * Carga, aplica el cambio y guarda. Si el dominio lo rechaza, la
	 * excepción sale antes de guardar: nada queda a medias.
	 */
	private Proyecto modificar(ProyectoId id, BiConsumer<Proyecto, Instant> cambio) {
		Proyecto proyecto = cargar(id);
		cambio.accept(proyecto, Instant.now(reloj));
		proyectos.guardar(proyecto);
		return proyecto;
	}

	private Proyecto cargar(ProyectoId id) {
		return proyectos.buscarPorIdParaModificar(id).orElseThrow(() -> new ProyectoNoEncontradoException(id));
	}

}
