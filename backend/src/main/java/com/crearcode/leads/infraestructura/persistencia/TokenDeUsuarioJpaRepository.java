package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crearcode.leads.dominio.PropositoDeToken;

interface TokenDeUsuarioJpaRepository extends JpaRepository<TokenDeUsuarioJpaEntity, UUID> {

	Optional<TokenDeUsuarioJpaEntity> findByValorHash(String valorHash);

	long countByUsuarioIdAndPropositoAndCreadoEnGreaterThanEqual(UUID usuarioId, PropositoDeToken proposito,
			Instant desde);

	// flush antes y clear despues: el UPDATE masivo no pasa por el
	// contexto de persistencia, y sin esto una lectura posterior en la
	// misma transaccion devolveria entidades cacheadas sin el cambio.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			UPDATE TokenDeUsuarioJpaEntity t
			SET t.usadoEn = :ahora
			WHERE t.usuarioId = :usuarioId
			AND t.proposito = :proposito
			AND t.usadoEn IS NULL
			AND t.expiraEn > :ahora
			""")
	void invalidarActivos(@Param("usuarioId") UUID usuarioId, @Param("proposito") PropositoDeToken proposito,
			@Param("ahora") Instant ahora);

}
