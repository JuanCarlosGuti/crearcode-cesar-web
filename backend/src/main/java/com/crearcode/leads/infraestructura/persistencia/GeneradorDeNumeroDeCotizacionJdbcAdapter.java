package com.crearcode.leads.infraestructura.persistencia;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.GeneradorDeNumeroDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;

/**
 * Consecutivo por año resuelto en la base de datos. El
 * {@code UPDATE ... RETURNING} es atómico y bloquea la fila del año, así
 * que dos envíos simultáneos no pueden llevarse el mismo número
 * (invariante 5 del contexto). Una secuencia de Postgres no serviría:
 * habría que reiniciarla cada año.
 */
@Component
class GeneradorDeNumeroDeCotizacionJdbcAdapter implements GeneradorDeNumeroDeCotizacion {

	private final JdbcClient jdbc;

	GeneradorDeNumeroDeCotizacionJdbcAdapter(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public NumeroDeCotizacion siguiente(int anio) {
		// ON CONFLICT DO NOTHING: el primer envío del año crea la fila y
		// los siguientes la encuentran, sin condición de carrera.
		jdbc.sql("INSERT INTO consecutivos_de_cotizacion (anio, ultimo_numero) VALUES (:anio, 0) "
				+ "ON CONFLICT (anio) DO NOTHING")
				.param("anio", anio)
				.update();

		Integer consecutivo = jdbc.sql("UPDATE consecutivos_de_cotizacion "
				+ "SET ultimo_numero = ultimo_numero + 1 WHERE anio = :anio RETURNING ultimo_numero")
				.param("anio", anio)
				.query(Integer.class)
				.single();

		return NumeroDeCotizacion.de(anio, consecutivo);
	}

}
