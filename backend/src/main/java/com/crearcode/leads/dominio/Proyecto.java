package com.crearcode.leads.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado raíz del contexto {@code proyectos} (fase F12): lo que nace
 * cuando una cotización se acepta y el cliente sigue desde su cuenta.
 * Como {@link Cotizacion}, **no llama al reloj**: todo instante llega
 * como parámetro.
 *
 * <p>Las reglas están en docs/03 Parte 5 y las decisiones 21 a 30 de
 * docs/10. Las que más pesan: el dinero lo calcula este agregado —el
 * avance, lo que se cobra con su impuesto, lo pagado y el saldo—, lo que
 * el cliente aceptó en la cotización no se cambia en silencio, y un
 * pago nunca deja un entregable pagado de más.
 */
public final class Proyecto {

	/**
	 * Decisión 23 de docs/10. Pública porque el pie del PDF de cotización
	 * la cita: un solo número, para que el documento y el proyecto nunca
	 * digan cosas distintas.
	 */
	public static final Duration GARANTIA = Duration.ofDays(60);

	private final ProyectoId id;
	private final Correo correoDelCliente;
	private final String nombreDelCliente;
	private final CotizacionId origen;
	private final Porcentaje impuesto;
	private final Instant creadoEn;
	private final List<Fase> fases;
	private final List<Pago> pagos;
	private DescripcionDelProyecto descripcion;
	private EstadoProyecto estado;
	private Instant finDeGarantia;
	private Instant actualizadoEn;

	private Proyecto(ProyectoId id, Correo correoDelCliente, String nombreDelCliente, CotizacionId origen,
			Porcentaje impuesto, DescripcionDelProyecto descripcion, EstadoProyecto estado, Instant finDeGarantia,
			List<Fase> fases, List<Pago> pagos, Instant creadoEn, Instant actualizadoEn) {
		this.id = id;
		this.correoDelCliente = correoDelCliente;
		this.nombreDelCliente = nombreDelCliente;
		this.origen = origen;
		this.impuesto = impuesto;
		this.descripcion = descripcion;
		this.estado = estado;
		this.finDeGarantia = finDeGarantia;
		this.fases = new ArrayList<>(fases);
		this.pagos = new ArrayList<>(pagos);
		this.creadoEn = creadoEn;
		this.actualizadoEn = actualizadoEn;
	}

	// --- Creación ---

	public static Proyecto enBlanco(Correo correoDelCliente, String nombreDelCliente,
			DescripcionDelProyecto descripcion, Porcentaje impuesto, List<NuevaFase> plan, Instant ahora) {
		if (correoDelCliente == null) {
			throw new ProyectoInvalidoException("El proyecto necesita el correo del cliente");
		}
		return crear(correoDelCliente, TextoDeProyecto.obligatorio(nombreDelCliente, 120, "El nombre del cliente"),
				null, descripcion, impuesto, plan, ahora);
	}

	/**
	 * Invariantes 5 y 6: solo desde una cotización ACEPTADA, y los
	 * entregables iniciales suman exactamente su subtotal. Se puede
	 * reorganizar lo acordado; cambiarlo, no. (Que sea un solo proyecto
	 * por cotización lo comprueba el caso de uso, que ve el repositorio.)
	 */
	public static Proyecto desdeCotizacion(Cotizacion cotizacion, DescripcionDelProyecto descripcion,
			List<NuevaFase> plan, Instant ahora) {
		if (cotizacion == null || cotizacion.estado() != EstadoCotizacion.ACEPTADA) {
			throw new ProyectoInvalidoException("Solo se crea un proyecto desde una cotización aceptada");
		}
		Proyecto proyecto = crear(cotizacion.cliente().correo(), cotizacion.cliente().nombre(), cotizacion.id(),
				descripcion, cotizacion.impuesto(), plan, ahora);
		Dinero aceptado = cotizacion.subtotal();
		Dinero planeado = proyecto.valorDeLosEntregables();
		if (!planeado.equals(aceptado)) {
			Dinero diferencia = planeado.esMayorQue(aceptado) ? planeado.menos(aceptado) : aceptado.menos(planeado);
			throw new ProyectoInvalidoException("Los entregables suman " + planeado.monto().toPlainString()
					+ " y la cotización aceptada " + aceptado.monto().toPlainString() + ": hay una diferencia de "
					+ diferencia.monto().toPlainString());
		}
		return proyecto;
	}

	private static Proyecto crear(Correo correo, String nombreDelCliente, CotizacionId origen,
			DescripcionDelProyecto descripcion, Porcentaje impuesto, List<NuevaFase> plan, Instant ahora) {
		if (descripcion == null) {
			throw new ProyectoInvalidoException("El proyecto necesita nombre y fechas");
		}
		if (impuesto == null) {
			throw new ProyectoInvalidoException("El proyecto necesita su porcentaje de impuesto (puede ser 0)");
		}
		if (plan == null || plan.stream().anyMatch(java.util.Objects::isNull)) {
			throw new ProyectoInvalidoException("El plan no puede ser nulo ni contener fases nulas");
		}
		if (ahora == null) {
			throw new ProyectoInvalidoException("El proyecto necesita su fecha de creación");
		}
		List<Fase> fases = new ArrayList<>();
		for (NuevaFase nueva : plan) {
			Fase fase = Fase.nueva(nueva.datos());
			nueva.entregables().forEach(datos -> fase.agregarAlFinal(Entregable.nuevo(datos, false)));
			fases.add(fase);
		}
		return new Proyecto(ProyectoId.nuevo(), correo, nombreDelCliente, origen, impuesto, descripcion,
				EstadoProyecto.ACTIVO, null, fases, List.of(), ahora, ahora);
	}

	/**
	 * Reconstituye un proyecto leído de persistencia sin volver a aplicar
	 * las invariantes de creación: ya se validaron al crearlo.
	 */
	public static Proyecto reconstruir(ProyectoId id, Correo correoDelCliente, String nombreDelCliente,
			CotizacionId origen, Porcentaje impuesto, DescripcionDelProyecto descripcion, EstadoProyecto estado,
			Instant finDeGarantia, List<Fase> fases, List<Pago> pagos, Instant creadoEn, Instant actualizadoEn) {
		return new Proyecto(id, correoDelCliente, nombreDelCliente, origen, impuesto, descripcion, estado,
				finDeGarantia, fases, pagos, creadoEn, actualizadoEn);
	}

	// --- Descripción y fases ---

	public void cambiarDescripcion(DescripcionDelProyecto nueva, Instant ahora) {
		exigirNoCerrado();
		if (nueva == null) {
			throw new ProyectoInvalidoException("El proyecto necesita nombre y fechas");
		}
		this.descripcion = nueva;
		tocar(ahora);
	}

	public FaseId agregarFase(DatosDeFase datos, Instant ahora) {
		exigirNoCerrado();
		if (datos == null) {
			throw new ProyectoInvalidoException("La fase necesita sus datos");
		}
		Fase fase = Fase.nueva(datos);
		fases.add(fase);
		tocar(ahora);
		return fase.id();
	}

	public void editarFase(FaseId faseId, DatosDeFase datos, Instant ahora) {
		exigirNoCerrado();
		if (datos == null) {
			throw new ProyectoInvalidoException("La fase necesita sus datos");
		}
		fase(faseId).editar(datos);
		tocar(ahora);
	}

	public void moverFase(FaseId faseId, int posicion, Instant ahora) {
		exigirNoCerrado();
		Fase fase = fase(faseId);
		fases.remove(fase);
		fases.add(Math.clamp(posicion, 0, fases.size()), fase);
		tocar(ahora);
	}

	public void quitarFase(FaseId faseId, Instant ahora) {
		exigirNoCerrado();
		Fase fase = fase(faseId);
		if (!fase.estaVacia()) {
			throw new ProyectoInvalidoException("Una fase con entregables no se quita: primero muévalos o quítelos");
		}
		fases.remove(fase);
		tocar(ahora);
	}

	public void escribirResumenDeFase(FaseId faseId, String resumen, Instant ahora) {
		exigirNoCerrado();
		fase(faseId).escribirResumen(resumen);
		tocar(ahora);
	}

	// --- Entregables del plan (decisiones 22 y 29) ---

	/**
	 * Agrega un entregable a una fase. En un proyecto nacido de una
	 * cotización, o en uno que ya está en garantía, todo lo nuevo es un
	 * cambio de alcance (decisión 22): lo aceptado no se reescribe, y el
	 * trabajo nuevo durante la garantía reactiva el proyecto.
	 */
	public EntregableId agregarEntregable(FaseId faseId, DatosDeEntregable datos, boolean esCambioDeAlcance,
			Instant ahora) {
		exigirNoCerrado();
		if (datos == null) {
			throw new ProyectoInvalidoException("El entregable necesita sus datos");
		}
		Fase fase = fase(faseId);
		if (!esCambioDeAlcance && origen != null) {
			throw new ProyectoInvalidoException(
					"Lo que el cliente aceptó en la cotización no se cambia: agréguelo como cambio de alcance");
		}
		if (!esCambioDeAlcance && estado == EstadoProyecto.EN_GARANTIA) {
			throw new ProyectoInvalidoException("En garantía, el trabajo nuevo solo entra como cambio de alcance");
		}
		Entregable entregable = Entregable.nuevo(datos, esCambioDeAlcance);
		fase.agregarAlFinal(entregable);
		if (estado == EstadoProyecto.EN_GARANTIA) {
			transicionarProyectoA(EstadoProyecto.ACTIVO);
			this.finDeGarantia = null;
		}
		tocar(ahora);
		return entregable.id();
	}

	/**
	 * Nombre y descripción se editan siempre. Valor y momento de cobro,
	 * solo mientras el entregable está PENDIENTE y sin pagos, y nunca si
	 * es parte de lo que el cliente aceptó en la cotización.
	 */
	public void editarEntregable(EntregableId entregableId, DatosDeEntregable nuevos, Instant ahora) {
		exigirNoCerrado();
		if (nuevos == null) {
			throw new ProyectoInvalidoException("El entregable necesita sus datos");
		}
		Entregable entregable = entregable(entregableId);
		boolean cambiaLoQueSeCobra = !nuevos.valor().equals(entregable.valor())
				|| nuevos.cobro() != entregable.cobro();
		if (cambiaLoQueSeCobra) {
			exigirQueSePuedaCambiarLoQueSeCobra(entregable);
		}
		entregable.editar(nuevos);
		tocar(ahora);
	}

	public void ponerDemo(EntregableId entregableId, UrlDeDemo demo, Instant ahora) {
		exigirNoCerrado();
		entregable(entregableId).ponerDemo(demo);
		tocar(ahora);
	}

	public void moverEntregable(EntregableId entregableId, FaseId destino, int posicion, Instant ahora) {
		exigirNoCerrado();
		Entregable entregable = entregable(entregableId);
		Fase faseDestino = fase(destino);
		faseDe(entregableId).quitar(entregable);
		faseDestino.agregar(entregable, posicion);
		tocar(ahora);
	}

	/** Decisión 29: solo se quita lo que está PENDIENTE y sin pagos. */
	public void quitarEntregable(EntregableId entregableId, Instant ahora) {
		exigirNoCerrado();
		Entregable entregable = entregable(entregableId);
		exigirQueSePuedaCambiarLoQueSeCobra(entregable);
		faseDe(entregableId).quitar(entregable);
		entrarEnGarantiaSiTermino(ahora);
		tocar(ahora);
	}

	private void exigirQueSePuedaCambiarLoQueSeCobra(Entregable entregable) {
		if (!entregable.estaPendiente()) {
			throw new ProyectoInvalidoException("Un entregable con trabajo hecho ya no cambia de valor ni se quita");
		}
		if (!pagadoDe(entregable.id()).esCero()) {
			throw new ProyectoInvalidoException("Un entregable con pagos ya no cambia de valor ni se quita");
		}
		if (origen != null && !entregable.esCambioDeAlcance()) {
			throw new ProyectoInvalidoException(
					"Lo que el cliente aceptó en la cotización no se cambia ni se quita");
		}
	}

	// --- Estados de los entregables ---

	/** Lo que mueve el equipo desde el panel (invariante 8: solo con el proyecto ACTIVO). */
	public void cambiarEstadoDeEntregable(EntregableId entregableId, EstadoEntregable destino, String nota,
			Instant ahora) {
		exigirQueSePuedanMoverEntregables();
		entregable(entregableId).transicionarA(destino, nota, QuienResponde.EQUIPO, ahora);
		entrarEnGarantiaSiTermino(ahora);
		tocar(ahora);
	}

	/** Decisión 27: el cliente aprueba lo que está en revisión. */
	public void aprobarComoCliente(EntregableId entregableId, Correo cliente, Instant ahora) {
		responderComoCliente(entregableId, cliente, EstadoEntregable.APROBADO, null, ahora);
	}

	/** Decisión 27: el cliente devuelve lo que está en revisión, diciendo qué ajustar. */
	public void pedirAjustesComoCliente(EntregableId entregableId, Correo cliente, String nota, Instant ahora) {
		responderComoCliente(entregableId, cliente, EstadoEntregable.CON_AJUSTES, nota, ahora);
	}

	private void responderComoCliente(EntregableId entregableId, Correo cliente, EstadoEntregable destino,
			String nota, Instant ahora) {
		if (!perteneceA(cliente)) {
			throw new ProyectoInvalidoException("El proyecto no es de este cliente");
		}
		exigirQueSePuedanMoverEntregables();
		Entregable entregable = entregable(entregableId);
		if (!entregable.estado().esperaRespuestaDelCliente()) {
			throw new TransicionDeEstadoInvalidaException("Este entregable no está esperando tu revisión");
		}
		entregable.transicionarA(destino, nota, QuienResponde.CLIENTE, ahora);
		entrarEnGarantiaSiTermino(ahora);
		tocar(ahora);
	}

	private void exigirQueSePuedanMoverEntregables() {
		if (!estado.permiteMoverEntregables()) {
			throw new ProyectoInvalidoException("Con el proyecto " + estado + " los entregables no cambian de estado");
		}
	}

	/** Decisión 23: aprobado el último entregable, 60 días de garantía. */
	private void entrarEnGarantiaSiTermino(Instant ahora) {
		List<Entregable> todos = entregables();
		if (estado == EstadoProyecto.ACTIVO && !todos.isEmpty() && todos.stream().allMatch(Entregable::estaAprobado)) {
			transicionarProyectoA(EstadoProyecto.EN_GARANTIA);
			this.finDeGarantia = ahora.plus(GARANTIA);
		}
	}

	// --- Estado del proyecto ---

	public void pausar(Instant ahora) {
		transicionarProyectoA(EstadoProyecto.PAUSADO);
		tocar(ahora);
	}

	public void reanudar(Instant ahora) {
		transicionarProyectoA(EstadoProyecto.ACTIVO);
		tocar(ahora);
	}

	/** Cierre manual: solo desde la garantía, antes de que venza. */
	public void cerrar(Instant ahora) {
		transicionarProyectoA(EstadoProyecto.CERRADO);
		tocar(ahora);
	}

	/**
	 * Lo llama el programador diario. Devuelve si lo cerró, para que el
	 * caso de uso guarde solo los que cambiaron.
	 */
	public boolean cerrarSiVencioLaGarantia(Instant ahora) {
		if (estado != EstadoProyecto.EN_GARANTIA || ahora.isBefore(finDeGarantia)) {
			return false;
		}
		cerrar(ahora);
		return true;
	}

	private void transicionarProyectoA(EstadoProyecto destino) {
		if (!estado.puedeTransicionarA(destino)) {
			throw new TransicionDeEstadoInvalidaException("El proyecto no puede pasar de " + estado + " a " + destino);
		}
		this.estado = destino;
	}

	private void exigirNoCerrado() {
		if (estado == EstadoProyecto.CERRADO) {
			throw new ProyectoInvalidoException("Un proyecto cerrado ya no cambia");
		}
	}

	// --- Pagos (invariante 4) ---

	/**
	 * Se registra en cualquier estado del proyecto (invariante 8: el
	 * dinero llega cuando llega), también por adelantado. Lo único que no
	 * se permite es pasarse de lo que se cobra por el entregable.
	 */
	public PagoId registrarPago(NuevoPago nuevo, Instant ahora) {
		if (nuevo == null) {
			throw new ProyectoInvalidoException("El pago necesita sus datos");
		}
		EntregableId entregableId = nuevo.entregable();
		entregable(entregableId);
		Dinero saldoDelEntregable = cobroDe(entregableId).menos(pagadoDe(entregableId));
		if (saldoDelEntregable.esCero()) {
			throw new ProyectoInvalidoException("Ese entregable ya está pagado completo");
		}
		if (nuevo.monto().esMayorQue(saldoDelEntregable)) {
			throw new ProyectoInvalidoException("El pago supera lo que falta por pagar de ese entregable: "
					+ saldoDelEntregable.monto().toPlainString());
		}
		Pago pago = Pago.desde(nuevo);
		pagos.add(pago);
		tocar(ahora);
		return pago.id();
	}

	// --- Cálculos (invariantes 1-3, ADR-15) ---

	/** Porcentaje entero, redondeado hacia abajo: nunca muestra 100 sin estar todo aprobado. */
	public int avance() {
		Dinero total = valorDeLosEntregables();
		if (total.esCero()) {
			return 0;
		}
		Dinero aprobado = entregables().stream()
				.filter(Entregable::estaAprobado)
				.map(Entregable::valor)
				.reduce(Dinero.CERO, Dinero::mas);
		return aprobado.monto()
				.multiply(BigDecimal.valueOf(100))
				.divide(total.monto(), 0, RoundingMode.DOWN)
				.intValueExact();
	}

	public Dinero impuestoDe(EntregableId entregableId) {
		return entregable(entregableId).valor().porcentaje(impuesto);
	}

	/** Lo que el cliente paga por el entregable: su valor más el impuesto del proyecto. */
	public Dinero cobroDe(EntregableId entregableId) {
		return entregable(entregableId).valor().mas(impuestoDe(entregableId));
	}

	/** Invariante 2: el anticipo desde que existe el proyecto; lo demás, al aprobarse. */
	public boolean esCobrable(EntregableId entregableId) {
		Entregable entregable = entregable(entregableId);
		return entregable.cobro() == MomentoDeCobro.AL_INICIAR || entregable.estaAprobado();
	}

	public Dinero pagadoDe(EntregableId entregableId) {
		return pagos.stream()
				.filter(pago -> pago.entregable().equals(entregableId))
				.map(Pago::monto)
				.reduce(Dinero.CERO, Dinero::mas);
	}

	/** Lo que ya se puede cobrar y no se ha pagado. Cero si todavía no es cobrable. */
	public Dinero pendienteDe(EntregableId entregableId) {
		if (!esCobrable(entregableId)) {
			return Dinero.CERO;
		}
		return cobroDe(entregableId).menos(pagadoDe(entregableId));
	}

	public Dinero total() {
		return entregables().stream().map(entregable -> cobroDe(entregable.id())).reduce(Dinero.CERO, Dinero::mas);
	}

	public Dinero totalPagado() {
		return pagos.stream().map(Pago::monto).reduce(Dinero.CERO, Dinero::mas);
	}

	public Dinero saldo() {
		return total().menos(totalPagado());
	}

	public Dinero pendienteDePago() {
		return entregables().stream().map(entregable -> pendienteDe(entregable.id())).reduce(Dinero.CERO, Dinero::mas);
	}

	private Dinero valorDeLosEntregables() {
		return entregables().stream().map(Entregable::valor).reduce(Dinero.CERO, Dinero::mas);
	}

	// --- Consultas ---

	public boolean perteneceA(Correo correo) {
		return correo != null && correoDelCliente.valor().equalsIgnoreCase(correo.valor());
	}

	public Fase fase(FaseId faseId) {
		return fases.stream()
				.filter(fase -> fase.id().equals(faseId))
				.findFirst()
				.orElseThrow(() -> new ProyectoInvalidoException("La fase no es de este proyecto"));
	}

	public Entregable entregable(EntregableId entregableId) {
		return fases.stream()
				.flatMap(fase -> fase.buscar(entregableId).stream())
				.findFirst()
				.orElseThrow(() -> new ProyectoInvalidoException("El entregable no es de este proyecto"));
	}

	private Fase faseDe(EntregableId entregableId) {
		return fases.stream()
				.filter(fase -> fase.buscar(entregableId).isPresent())
				.findFirst()
				.orElseThrow(() -> new ProyectoInvalidoException("El entregable no es de este proyecto"));
	}

	/** Todos los entregables, en el orden del plan. */
	public List<Entregable> entregables() {
		return fases.stream().flatMap(fase -> fase.entregables().stream()).toList();
	}

	private void tocar(Instant ahora) {
		this.actualizadoEn = ahora;
	}

	public ProyectoId id() {
		return id;
	}

	public Correo correoDelCliente() {
		return correoDelCliente;
	}

	public String nombreDelCliente() {
		return nombreDelCliente;
	}

	public CotizacionId origen() {
		return origen;
	}

	public Porcentaje impuesto() {
		return impuesto;
	}

	public DescripcionDelProyecto descripcion() {
		return descripcion;
	}

	public EstadoProyecto estado() {
		return estado;
	}

	public Instant finDeGarantia() {
		return finDeGarantia;
	}

	public List<Fase> fases() {
		return List.copyOf(fases);
	}

	public List<Pago> pagos() {
		return List.copyOf(pagos);
	}

	public Instant creadoEn() {
		return creadoEn;
	}

	public Instant actualizadoEn() {
		return actualizadoEn;
	}

}
