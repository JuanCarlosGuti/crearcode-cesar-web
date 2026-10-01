package com.crearcode.leads.dominio;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.crearcode.leads.dominio.EstadoProyecto.ACTIVO;
import static com.crearcode.leads.dominio.EstadoProyecto.CERRADO;
import static com.crearcode.leads.dominio.EstadoProyecto.EN_GARANTIA;
import static com.crearcode.leads.dominio.EstadoProyecto.PAUSADO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * La máquina de docs/03 Parte 5 §4. Incluye {@code EN_GARANTIA -> ACTIVO},
 * que el brief no traía: un cambio de alcance agregado durante la
 * garantía (decisión 22) deja otra vez trabajo pendiente.
 */
class EstadoProyectoTest {

	private static final Map<EstadoProyecto, Set<EstadoProyecto>> ESPERADAS = Map.of(
			ACTIVO, EnumSet.of(PAUSADO, EN_GARANTIA),
			PAUSADO, EnumSet.of(ACTIVO),
			EN_GARANTIA, EnumSet.of(ACTIVO, CERRADO),
			CERRADO, EnumSet.noneOf(EstadoProyecto.class));

	@Test
	void cadaTransicionEsValidaSoloSiEstaEnLaTabla() {
		for (EstadoProyecto origen : EstadoProyecto.values()) {
			for (EstadoProyecto destino : EstadoProyecto.values()) {
				assertThat(origen.puedeTransicionarA(destino))
						.as("%s -> %s", origen, destino)
						.isEqualTo(ESPERADAS.get(origen).contains(destino));
			}
		}
	}

	/** Invariante 8: pausado o cerrado, los entregables no se mueven. */
	@Test
	void soloUnProyectoActivoDejaMoverSusEntregables() {
		for (EstadoProyecto estado : EstadoProyecto.values()) {
			assertThat(estado.permiteMoverEntregables()).as("%s", estado).isEqualTo(estado == ACTIVO);
		}
	}

}
