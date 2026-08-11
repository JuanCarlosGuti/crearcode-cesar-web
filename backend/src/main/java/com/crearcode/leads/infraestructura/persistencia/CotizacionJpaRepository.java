package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crearcode.leads.dominio.EstadoCotizacion;

interface CotizacionJpaRepository extends JpaRepository<CotizacionJpaEntity, UUID> {

	List<CotizacionJpaEntity> findByEstado(EstadoCotizacion estado);

	@Query("select c from CotizacionJpaEntity c where lower(c.clienteCorreo) = lower(:correo)")
	List<CotizacionJpaEntity> findByClienteCorreo(@Param("correo") String correo);

}
