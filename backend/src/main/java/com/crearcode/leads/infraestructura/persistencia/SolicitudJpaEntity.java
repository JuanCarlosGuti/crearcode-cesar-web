package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import com.crearcode.leads.dominio.EstadoSolicitud;
import com.crearcode.leads.dominio.ServicioDeInteres;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modelo de persistencia de una solicitud de contacto. Deliberadamente
 * plano (sin los VOs del dominio): {@link SolicitudMapper} traduce en
 * ambas direcciones. Nunca se expone fuera de
 * {@code infraestructura/persistencia}.
 */
@Entity
@Table(name = "solicitudes_contacto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SolicitudJpaEntity {

	@Id
	private UUID id;

	private String nombre;
	private String empresa;
	private String correo;
	private String telefono;

	@Enumerated(EnumType.STRING)
	private ServicioDeInteres servicioDeInteres;

	private String mensaje;

	@Enumerated(EnumType.STRING)
	private EstadoSolicitud estado;

	private boolean consentimientoAceptado;
	private Instant consentimientoFechaAceptacion;
	private String consentimientoVersionPolitica;

	private Instant fechaCreacion;
	private Instant fechaUltimaActualizacion;

}
