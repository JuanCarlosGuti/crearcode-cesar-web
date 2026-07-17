package com.crearcode.leads.infraestructura.persistencia;

import java.util.UUID;

import com.crearcode.leads.dominio.Rol;

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
 * Modelo de persistencia de un usuario. Deliberadamente plano (sin los
 * VOs del dominio): {@link UsuarioMapper} traduce en ambas direcciones.
 * Nunca se expone fuera de {@code infraestructura/persistencia}.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UsuarioJpaEntity {

	@Id
	private UUID id;

	private String correo;
	private String contrasenaHash;

	@Enumerated(EnumType.STRING)
	private Rol rol;

}
