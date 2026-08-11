package com.crearcode.leads.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agregado raíz del contexto {@code cotizaciones} (fase F11). Como
 * {@link SolicitudDeContacto}, **no llama al reloj**: todo instante
 * llega como parámetro desde la capa de aplicación.
 *
 * <p>Dos invariantes mandan sobre el resto: una cotización ENVIADA ya no
 * se edita (lo que el cliente vio no puede cambiar después) y los
 * totales se calculan aquí — nunca entran de fuera.
 */
public final class Cotizacion {

	private final CotizacionId id;
	private final DatosDelCliente cliente;
	private final Porcentaje impuesto;
	private final Instant creadaEn;
	private final Instant validaHasta;
	private final SolicitudId origen;
	private String notas;
	private final List<ItemDeCotizacion> items;
	private EstadoCotizacion estado;
	private NumeroDeCotizacion numero;
	private Instant enviadaEn;
	private Instant respondidaEn;

	private Cotizacion(CotizacionId id, DatosDelCliente cliente, Porcentaje impuesto, Instant creadaEn,
			Instant validaHasta, SolicitudId origen, String notas, List<ItemDeCotizacion> items,
			EstadoCotizacion estado, NumeroDeCotizacion numero, Instant enviadaEn, Instant respondidaEn) {
		this.id = id;
		this.cliente = cliente;
		this.impuesto = impuesto;
		this.creadaEn = creadaEn;
		this.validaHasta = validaHasta;
		this.origen = origen;
		this.notas = notas;
		this.items = new ArrayList<>(items);
		this.estado = estado;
		this.numero = numero;
		this.enviadaEn = enviadaEn;
		this.respondidaEn = respondidaEn;
	}

	public static Cotizacion abrirBorrador(DatosDelCliente cliente, Porcentaje impuesto, Instant creadaEn,
			Instant validaHasta, SolicitudId origen, String notas) {
		if (cliente == null) {
			throw new CotizacionInvalidaException("La cotización necesita los datos del cliente");
		}
		if (impuesto == null) {
			throw new CotizacionInvalidaException("La cotización necesita un porcentaje de impuesto (puede ser 0)");
		}
		if (creadaEn == null || validaHasta == null || !validaHasta.isAfter(creadaEn)) {
			throw new CotizacionInvalidaException("La validez debe ser posterior a la fecha de creación");
		}
		return new Cotizacion(CotizacionId.nuevo(), cliente, impuesto, creadaEn, validaHasta, origen, notas,
				List.of(), EstadoCotizacion.BORRADOR, null, null, null);
	}

	/**
	 * Reconstituye una cotización existente (al leerla de persistencia)
	 * sin volver a aplicar las invariantes de creación: ya se validaron
	 * al abrirla, y el estado puede ser cualquiera.
	 */
	public static Cotizacion reconstruir(CotizacionId id, DatosDelCliente cliente, Porcentaje impuesto,
			Instant creadaEn, Instant validaHasta, SolicitudId origen, String notas,
			List<ItemDeCotizacion> items, EstadoCotizacion estado, NumeroDeCotizacion numero,
			Instant enviadaEn, Instant respondidaEn) {
		return new Cotizacion(id, cliente, impuesto, creadaEn, validaHasta, origen, notas, items, estado,
				numero, enviadaEn, respondidaEn);
	}

	public void agregarItem(ItemDeCotizacion item) {
		exigirBorrador();
		if (item == null) {
			throw new CotizacionInvalidaException("El ítem no puede ser nulo");
		}
		items.add(item);
	}

	public void quitarItem(int posicion) {
		exigirBorrador();
		if (posicion < 0 || posicion >= items.size()) {
			throw new CotizacionInvalidaException("No existe el ítem en la posición " + posicion);
		}
		items.remove(posicion);
	}

	/**
	 * Reemplaza la lista completa: el formulario del panel edita la
	 * cotización entera y guarda, no ítem por ítem.
	 */
	public void reemplazarItems(List<ItemDeCotizacion> nuevos) {
		exigirBorrador();
		// stream().anyMatch en vez de contains(null): las listas inmutables
		// de List.of() lanzan NPE al preguntarles por null.
		if (nuevos == null || nuevos.stream().anyMatch(Objects::isNull)) {
			throw new CotizacionInvalidaException("La lista de ítems no puede ser nula ni contener nulos");
		}
		items.clear();
		items.addAll(nuevos);
	}

	public void cambiarNotas(String nuevas) {
		exigirBorrador();
		this.notas = nuevas;
	}

	public void enviar(NumeroDeCotizacion numero, Instant ahora) {
		transicionarA(EstadoCotizacion.ENVIADA);
		if (items.isEmpty()) {
			throw new CotizacionInvalidaException("No se puede enviar una cotización sin ítems");
		}
		if (numero == null) {
			throw new CotizacionInvalidaException("La cotización necesita su número para poder enviarse");
		}
		this.numero = numero;
		this.enviadaEn = ahora;
		this.estado = EstadoCotizacion.ENVIADA;
	}

	public void aceptar(Instant ahora) {
		responder(EstadoCotizacion.ACEPTADA, ahora);
	}

	public void rechazar(Instant ahora) {
		responder(EstadoCotizacion.RECHAZADA, ahora);
	}

	public void marcarVencida(Instant ahora) {
		transicionarA(EstadoCotizacion.VENCIDA);
		if (estaVigente(ahora)) {
			throw new CotizacionInvalidaException("La cotización todavía está vigente");
		}
		this.estado = EstadoCotizacion.VENCIDA;
	}

	public void cancelar(Instant ahora) {
		transicionarA(EstadoCotizacion.CANCELADA);
		this.estado = EstadoCotizacion.CANCELADA;
		this.respondidaEn = ahora;
	}

	public boolean estaVigente(Instant ahora) {
		return !ahora.isAfter(validaHasta);
	}

	public Dinero subtotal() {
		return items.stream().map(ItemDeCotizacion::subtotal).reduce(Dinero.CERO, Dinero::mas);
	}

	public Dinero impuestoCalculado() {
		return subtotal().porcentaje(impuesto);
	}

	public Dinero total() {
		return subtotal().mas(impuestoCalculado());
	}

	private void responder(EstadoCotizacion respuesta, Instant ahora) {
		transicionarA(respuesta);
		if (!estaVigente(ahora)) {
			throw new CotizacionVencidaException("La cotización venció el " + validaHasta);
		}
		this.estado = respuesta;
		this.respondidaEn = ahora;
	}

	private void transicionarA(EstadoCotizacion destino) {
		if (!estado.puedeTransicionarA(destino)) {
			throw new TransicionDeEstadoInvalidaException(
					"No se puede transicionar de %s a %s".formatted(estado, destino));
		}
	}

	private void exigirBorrador() {
		if (estado != EstadoCotizacion.BORRADOR) {
			throw new CotizacionInvalidaException(
					"Una cotización %s ya no se puede editar".formatted(estado));
		}
	}

	public CotizacionId id() {
		return id;
	}

	public DatosDelCliente cliente() {
		return cliente;
	}

	public Porcentaje impuesto() {
		return impuesto;
	}

	public Instant creadaEn() {
		return creadaEn;
	}

	public Instant validaHasta() {
		return validaHasta;
	}

	public SolicitudId origen() {
		return origen;
	}

	public String notas() {
		return notas;
	}

	/** Copia inmodificable: los ítems solo cambian por los métodos del agregado. */
	public List<ItemDeCotizacion> items() {
		return List.copyOf(items);
	}

	public EstadoCotizacion estado() {
		return estado;
	}

	public NumeroDeCotizacion numero() {
		return numero;
	}

	public Instant enviadaEn() {
		return enviadaEn;
	}

	public Instant respondidaEn() {
		return respondidaEn;
	}

}
