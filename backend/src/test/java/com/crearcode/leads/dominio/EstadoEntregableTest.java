package com.crearcode.leads.dominio;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.crearcode.leads.dominio.EstadoEntregable.APROBADO;
import static com.crearcode.leads.dominio.EstadoEntregable.CON_AJUSTES;
import static com.crearcode.leads.dominio.EstadoEntregable.EN_CURSO;
import static com.crearcode.leads.dominio.EstadoEntregable.EN_REVISION;
import static com.crearcode.leads.dominio.EstadoEntregable.PENDIENTE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * La máquina de docs/03 Parte 5 §4, comprobada par por par: cualquier
 * transición que no esté en esta tabla tiene que ser inválida.
 */
class EstadoEntregableTest {

	private static final Map<EstadoEntregable, Set<EstadoEntregable>> ESPERADAS = Map.of(
			PENDIENTE, EnumSet.of(EN_CURSO),
			EN_CURSO, EnumSet.of(EN_REVISION),
			EN_REVISION, EnumSet.of(APROBADO, CON_AJUSTES),
			CON_AJUSTES, EnumSet.of(EN_CURSO),
			APROBADO, EnumSet.noneOf(EstadoEntregable.class));

	@Test
	void cadaTransicionEsValidaSoloSiEstaEnLaTabla() {
		for (EstadoEntregable origen : EstadoEntregable.values()) {
			for (EstadoEntregable destino : EstadoEntregable.values()) {
				assertThat(origen.puedeTransicionarA(destino))
						.as("%s -> %s", origen, destino)
						.isEqualTo(ESPERADAS.get(origen).contains(destino));
			}
		}
	}

	@Test
	void aprobadoEsTerminal() {
		assertThat(APROBADO.esTerminal()).isTrue();
		assertThat(EN_REVISION.esTerminal()).isFalse();
	}

	/** Decisión 27: el cliente solo responde lo que está en revisión. */
	@Test
	void elClienteSoloPuedeResponderUnEntregableEnRevision() {
		for (EstadoEntregable estado : EstadoEntregable.values()) {
			assertThat(estado.esperaRespuestaDelCliente()).as("%s", estado).isEqualTo(estado == EN_REVISION);
		}
	}

}
