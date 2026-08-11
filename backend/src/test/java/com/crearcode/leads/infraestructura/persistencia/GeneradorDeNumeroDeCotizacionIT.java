package com.crearcode.leads.infraestructura.persistencia;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.GeneradorDeNumeroDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El consecutivo es el único dato del contexto que no puede repetirse ni
 * saltarse (invariante 5), así que se prueba también bajo concurrencia.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class GeneradorDeNumeroDeCotizacionIT {

	@Autowired
	private GeneradorDeNumeroDeCotizacion generador;

	@Test
	void entregaConsecutivosCrecientesDentroDelMismoAnio() {
		int anio = 2101;

		assertThat(generador.siguiente(anio)).isEqualTo(NumeroDeCotizacion.de(anio, 1));
		assertThat(generador.siguiente(anio)).isEqualTo(NumeroDeCotizacion.de(anio, 2));
		assertThat(generador.siguiente(anio)).isEqualTo(NumeroDeCotizacion.de(anio, 3));
	}

	@Test
	void cadaAnioLlevaSuPropiaCuenta() {
		assertThat(generador.siguiente(2102)).isEqualTo(NumeroDeCotizacion.de(2102, 1));
		assertThat(generador.siguiente(2103)).isEqualTo(NumeroDeCotizacion.de(2103, 1));
		assertThat(generador.siguiente(2102)).isEqualTo(NumeroDeCotizacion.de(2102, 2));
	}

	@Test
	void veinteEnviosSimultaneosNoRepitenNiSaltanNumeros() throws Exception {
		int anio = 2104;
		int envios = 20;

		try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
			List<Callable<NumeroDeCotizacion>> tareas = java.util.Collections.nCopies(envios,
					() -> generador.siguiente(anio));

			List<Future<NumeroDeCotizacion>> resultados = pool.invokeAll(tareas);

			List<String> numeros = resultados.stream().map(futuro -> {
				try {
					return futuro.get().valor();
				} catch (Exception fallo) {
					throw new IllegalStateException(fallo);
				}
			}).toList();

			assertThat(numeros).doesNotHaveDuplicates();
			assertThat(numeros).containsExactlyInAnyOrderElementsOf(
					java.util.stream.IntStream.rangeClosed(1, envios)
							.mapToObj(n -> NumeroDeCotizacion.de(anio, n).valor())
							.toList());
		}
	}

}
