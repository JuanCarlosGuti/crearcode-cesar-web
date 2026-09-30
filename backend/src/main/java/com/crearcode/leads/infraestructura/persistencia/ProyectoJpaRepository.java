package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crearcode.leads.dominio.EstadoProyecto;

interface ProyectoJpaRepository extends JpaRepository<ProyectoJpaEntity, UUID> {

	/** {@code SELECT ... FOR UPDATE}: quien modifica espera a quien ya estaba modificando. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from ProyectoJpaEntity p where p.id = :id")
	Optional<ProyectoJpaEntity> findByIdParaModificar(@Param("id") UUID id);

	@Query("select p from ProyectoJpaEntity p where lower(p.clienteCorreo) = lower(:correo) order by p.creadoEn desc")
	List<ProyectoJpaEntity> findByClienteCorreo(@Param("correo") String correo);

	Optional<ProyectoJpaEntity> findByOrigenCotizacionId(UUID origenCotizacionId);

	List<ProyectoJpaEntity> findByEstado(EstadoProyecto estado);

	List<ProyectoJpaEntity> findAllByOrderByCreadoEnDesc();

}
