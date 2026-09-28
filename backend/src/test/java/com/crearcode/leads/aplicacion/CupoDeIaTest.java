package com.crearcode.leads.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.IdentidadDelVisitante;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Los cuatro casos de uso de IA repetían las mismas ~20 líneas de
 * cupos, cada uno con su propia pareja de contadores: ocho mapas en
 * memoria haciendo lo mismo, y cuatro sitios donde equivocarse con el
 * orden de reserva y liberación. Aquí vive esa regla una sola vez y
 * aquí se prueba una sola vez.
 */
class CupoDeIaTest {

	private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneOffset.UTC);

	private final IdentidadDelVisitante anonimo = IdentidadDelVisitante.anonima("sesion-1");
	private final IdentidadDelVisitante registrado = IdentidadDelVisitante.registrada("cliente@correo.com");

	@Test
	void dejaPasarMientrasHayaCupoYDevuelveLoQueProduceElTrabajo() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 10, 5, 2, 1000);

		assertThat(cupo.ejecutar(anonimo, () -> "respuesta")).isEqualTo("respuesta");
	}

	@Test
	void unRegistradoTieneMasCupoQueUnAnonimo() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 100, 3, 1, 1000);

		cupo.ejecutar(anonimo, () -> "ok");
		assertThatThrownBy(() -> cupo.ejecutar(anonimo, () -> "ok"))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);

		for (int i = 0; i < 3; i++) {
			cupo.ejecutar(registrado, () -> "ok");
		}
		assertThatThrownBy(() -> cupo.ejecutar(registrado, () -> "ok"))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	@Test
	void elLimiteGlobalMandaSobreElPersonal() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 1, 50, 50, 1000);

		cupo.ejecutar(anonimo, () -> "ok");

		assertThatThrownBy(() -> cupo.ejecutar(registrado, () -> "ok"))
				.isInstanceOf(LimiteGlobalAlcanzadoException.class);
	}

	@Test
	void elCupoSeDevuelveCuandoElTrabajoFalla() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 100, 5, 1, 1000);

		assertThatThrownBy(() -> cupo.ejecutar(anonimo, () -> {
			throw new IllegalStateException("el proveedor se cayó");
		})).isInstanceOf(IllegalStateException.class);

		// Si no se devolviera, esta llamada ya no tendría cupo.
		assertThat(cupo.ejecutar(anonimo, () -> "ok")).isEqualTo("ok");
	}

	@Test
	void superarElLimitePersonalNoConsumeCupoGlobal() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 2, 50, 1, 1000);

		cupo.ejecutar(anonimo, () -> "ok");
		assertThatThrownBy(() -> cupo.ejecutar(anonimo, () -> "ok"))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);

		// Al global le debe quedar 1, no 0.
		assertThat(cupo.ejecutar(registrado, () -> "ok")).isEqualTo("ok");
	}

	/**
	 * Borrar sessionStorage devolvia el cupo anonimo entero, cuantas
	 * veces se quisiera (auditoria P0-2a). El techo por red lo corta sin
	 * castigar a una oficina o a una red movil, que comparten IP y
	 * seguirian teniendo su cupo personal cada uno.
	 */
	@Test
	void cambiarDeSesionAnonimaNoDevuelveElCupoPorqueLaRedTieneSuPropioTecho() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 100, 50, 1, 3);
		IdentidadDelVisitante primera = IdentidadDelVisitante.anonima("sesion-1", "red-a");
		IdentidadDelVisitante segunda = IdentidadDelVisitante.anonima("sesion-2", "red-a");
		IdentidadDelVisitante tercera = IdentidadDelVisitante.anonima("sesion-3", "red-a");
		IdentidadDelVisitante cuarta = IdentidadDelVisitante.anonima("sesion-4", "red-a");

		cupo.ejecutar(primera, () -> "ok");
		cupo.ejecutar(segunda, () -> "ok");
		cupo.ejecutar(tercera, () -> "ok");

		assertThatThrownBy(() -> cupo.ejecutar(cuarta, () -> "ok"))
				.isInstanceOf(LimiteDeUsoAlcanzadoException.class);
	}

	@Test
	void otraRedNoPagaElTechoDeLaPrimera() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 100, 50, 1, 1);
		cupo.ejecutar(IdentidadDelVisitante.anonima("sesion-1", "red-a"), () -> "ok");

		assertThat(cupo.ejecutar(IdentidadDelVisitante.anonima("sesion-2", "red-b"), () -> "ok"))
				.isEqualTo("ok");
	}

	/** Quien tiene cuenta ya esta identificado: la red no le aplica. */
	@Test
	void elTechoDeRedNoAlcanzaAQuienTieneCuenta() {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 100, 50, 1, 1);
		cupo.ejecutar(IdentidadDelVisitante.anonima("sesion-1", "red-a"), () -> "ok");

		assertThat(cupo.ejecutar(IdentidadDelVisitante.registrada("cliente@correo.com"), () -> "ok"))
				.isEqualTo("ok");
	}

	/** El trabajo tarda: sin eso no hay solapamiento que probar. */
	private static void dormir() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException interrumpido) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * La razón de ser de la reserva atómica: leer y sumar después dejaba
	 * pasar entera una ráfaga simultánea (auditoría del 28 sep 2026).
	 */
	@Test
	void unaRafagaSimultaneaNoSuperaElCupoGlobal() throws InterruptedException {
		CupoDeIa cupo = new CupoDeIa(RELOJ, 6, 100, 100, 1000);
		AtomicInteger ejecutados = new AtomicInteger();
		CountDownLatch salida = new CountDownLatch(1);
		int peticiones = 18;

		try (ExecutorService hilos = Executors.newFixedThreadPool(peticiones)) {
			for (int i = 0; i < peticiones; i++) {
				hilos.submit(() -> {
					salida.await();
					try {
						cupo.ejecutar(anonimo, () -> {
							ejecutados.incrementAndGet();
							dormir();
							return "ok";
						});
					} catch (RuntimeException limiteAlcanzado) {
						// esperado para los que no alcanzan cupo
					}
					return null;
				});
			}
			salida.countDown();
			hilos.shutdown();
			assertThat(hilos.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
		}

		assertThat(ejecutados.get()).isEqualTo(6);
	}

}
