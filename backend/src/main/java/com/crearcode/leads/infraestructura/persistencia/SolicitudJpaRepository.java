package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crearcode.leads.dominio.EstadoSolicitud;

interface SolicitudJpaRepository extends JpaRepository<SolicitudJpaEntity, UUID> {

	List<SolicitudJpaEntity> findByEstado(EstadoSolicitud estado);

}
