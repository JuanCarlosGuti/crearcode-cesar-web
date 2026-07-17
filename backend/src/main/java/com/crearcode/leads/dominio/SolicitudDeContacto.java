package com.crearcode.leads.dominio;

import java.time.Instant;

/**
 * Agregado raíz del contexto {@code leads}. Identidad propia
 * ({@link SolicitudId}), estado mutable controlado exclusivamente por
 * {@link #cambiarEstado(EstadoSolicitud, Instant)}. No llama al reloj del
 * sistema: todo timestamp llega como parámetro desde la capa de
 * aplicación (ver docs/03-modelo-de-dominio.md invariante 7).
 */
public final class SolicitudDeContacto {

	private final SolicitudId id;
	private final DatosDeContacto datosDeContacto;
	private final ServicioDeInteres servicioDeInteres;
	private final String mensaje;
	private EstadoSolicitud estado;
	private final ConsentimientoDatos consentimiento;
	private final Instant fechaCreacion;
	private Instant fechaUltimaActualizacion;

	private SolicitudDeContacto(SolicitudId id, DatosDeContacto datosDeContacto,
			ServicioDeInteres servicioDeInteres, String mensaje, EstadoSolicitud estado,
			ConsentimientoDatos consentimiento, Instant fechaCreacion, Instant fechaUltimaActualizacion) {
		this.id = id;
		this.datosDeContacto = datosDeContacto;
		this.servicioDeInteres = servicioDeInteres;
		this.mensaje = mensaje;
		this.estado = estado;
		this.consentimiento = consentimiento;
		this.fechaCreacion = fechaCreacion;
		this.fechaUltimaActualizacion = fechaUltimaActualizacion;
	}

	public static SolicitudDeContacto registrar(DatosDeContacto datosDeContacto,
			ServicioDeInteres servicioDeInteres, String mensaje, ConsentimientoDatos consentimiento,
			Instant ahora) {
		if (!consentimiento.aceptado()) {
			throw new ConsentimientoRequeridoException(
					"No se puede registrar una solicitud sin consentimiento aceptado");
		}
		if (mensaje == null || mensaje.isBlank()) {
			throw new DatosDeContactoInvalidosException("El mensaje no puede estar vacío");
		}

		return new SolicitudDeContacto(SolicitudId.nuevo(), datosDeContacto, servicioDeInteres,
				mensaje, EstadoSolicitud.NUEVA, consentimiento, ahora, ahora);
	}

	/**
	 * Reconstituye una solicitud ya existente (por ejemplo, al leerla desde
	 * persistencia) sin volver a aplicar las invariantes de creación de
	 * {@link #registrar}: ya se validaron cuando la solicitud se creó por
	 * primera vez, y el estado puede ser cualquiera, no solo NUEVA.
	 */
	public static SolicitudDeContacto reconstruir(SolicitudId id, DatosDeContacto datosDeContacto,
			ServicioDeInteres servicioDeInteres, String mensaje, EstadoSolicitud estado,
			ConsentimientoDatos consentimiento, Instant fechaCreacion, Instant fechaUltimaActualizacion) {
		return new SolicitudDeContacto(id, datosDeContacto, servicioDeInteres, mensaje, estado,
				consentimiento, fechaCreacion, fechaUltimaActualizacion);
	}

	public void cambiarEstado(EstadoSolicitud nuevoEstado, Instant ahora) {
		if (!estado.puedeTransicionarA(nuevoEstado)) {
			throw new TransicionDeEstadoInvalidaException(
					"No se puede transicionar de %s a %s".formatted(estado, nuevoEstado));
		}
		this.estado = nuevoEstado;
		this.fechaUltimaActualizacion = ahora;
	}

	public SolicitudId id() {
		return id;
	}

	public DatosDeContacto datosDeContacto() {
		return datosDeContacto;
	}

	public ServicioDeInteres servicioDeInteres() {
		return servicioDeInteres;
	}

	public String mensaje() {
		return mensaje;
	}

	public EstadoSolicitud estado() {
		return estado;
	}

	public ConsentimientoDatos consentimiento() {
		return consentimiento;
	}

	public Instant fechaCreacion() {
		return fechaCreacion;
	}

	public Instant fechaUltimaActualizacion() {
		return fechaUltimaActualizacion;
	}

}
