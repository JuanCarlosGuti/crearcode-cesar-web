package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import com.crearcode.leads.dominio.PropositoDeToken;

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
 * Modelo de persistencia de un token de correo (fase F8). Igual que las
 * demás entidades JPA, nunca sale de {@code infraestructura/persistencia}:
 * {@link TokenDeUsuarioMapper} traduce en ambas direcciones.
 */
@Entity
@Table(name = "tokens_de_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TokenDeUsuarioJpaEntity {

	@Id
	private UUID id;

	private UUID usuarioId;
	private String valorHash;

	@Enumerated(EnumType.STRING)
	private PropositoDeToken proposito;

	private Instant creadoEn;
	private Instant expiraEn;
	private Instant usadoEn;

}
