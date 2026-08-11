package com.crearcode.leads.infraestructura.persistencia;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.TestcontainersConfiguration;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CotizacionRepositorio;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class CotizacionRepositorioIT {

	private static final Instant AHORA = Instant.parse("2026-08-11T10:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T10:00:00Z");

	@Autowired
	private CotizacionRepositorio repositorio;

	private Cotizacion borradorCon(String correo, ItemDeCotizacion... items) {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", new Correo(correo), "3001234567", "900123456-1"),
				new Porcentaje(19), AHORA, EN_QUINCE_DIAS, null, "Incluye capacitacion");
		for (ItemDeCotizacion item : items) {
			cotizacion.agregarItem(item);
		}
		return cotizacion;
	}

	@Test
	void guardaYRecuperaUnaCotizacionCompleta() {
		Cotizacion original = borradorCon("cliente@empresa.com",
				new ItemDeCotizacion("Desarrollo", 2, Dinero.de(2_500_000)),
				new ItemDeCotizacion("Capacitacion", 1, Dinero.de(500_000)));

		repositorio.guardar(original);
		Cotizacion recuperada = repositorio.buscarPorId(original.id()).orElseThrow();

		assertThat(recuperada.id()).isEqualTo(original.id());
		assertThat(recuperada.cliente().nombre()).isEqualTo("Panaderia El Trigal");
		assertThat(recuperada.cliente().identificacion()).isEqualTo("900123456-1");
		assertThat(recuperada.notas()).isEqualTo("Incluye capacitacion");
		assertThat(recuperada.estado()).isEqualTo(EstadoCotizacion.BORRADOR);
		assertThat(recuperada.numero()).isNull();
		assertThat(recuperada.items()).hasSize(2);
		// Los totales se recalculan del lado del dominio, no se persisten.
		assertThat(recuperada.subtotal()).isEqualTo(Dinero.de(5_500_000));
		assertThat(recuperada.total()).isEqualTo(Dinero.de(6_545_000));
	}

	@Test
	void conservaElOrdenDeLosItems() {
		Cotizacion original = borradorCon("cliente@empresa.com",
				new ItemDeCotizacion("Primero", 1, Dinero.de(100)),
				new ItemDeCotizacion("Segundo", 1, Dinero.de(200)),
				new ItemDeCotizacion("Tercero", 1, Dinero.de(300)));

		repositorio.guardar(original);

		assertThat(repositorio.buscarPorId(original.id()).orElseThrow().items())
				.extracting(ItemDeCotizacion::descripcion)
				.containsExactly("Primero", "Segundo", "Tercero");
	}

	@Test
	void reemplazarLosItemsDeUnBorradorNoDejaHuerfanos() {
		Cotizacion original = borradorCon("cliente@empresa.com",
				new ItemDeCotizacion("Viejo", 1, Dinero.de(100)));
		repositorio.guardar(original);

		original.reemplazarItems(List.of(new ItemDeCotizacion("Nuevo", 3, Dinero.de(1_000))));
		repositorio.guardar(original);

		Cotizacion recuperada = repositorio.buscarPorId(original.id()).orElseThrow();
		assertThat(recuperada.items()).hasSize(1);
		assertThat(recuperada.items().get(0).descripcion()).isEqualTo("Nuevo");
		assertThat(recuperada.subtotal()).isEqualTo(Dinero.de(3_000));
	}

	@Test
	void guardaElNumeroYLosInstantesAlEnviar() {
		Cotizacion cotizacion = borradorCon("cliente@empresa.com",
				new ItemDeCotizacion("Desarrollo", 1, Dinero.de(1_000_000)));
		repositorio.guardar(cotizacion);

		cotizacion.enviar(NumeroDeCotizacion.de(2026, 7), AHORA);
		repositorio.guardar(cotizacion);

		Cotizacion recuperada = repositorio.buscarPorId(cotizacion.id()).orElseThrow();
		assertThat(recuperada.numero()).isEqualTo(NumeroDeCotizacion.de(2026, 7));
		assertThat(recuperada.enviadaEn()).isEqualTo(AHORA);
		assertThat(recuperada.estado()).isEqualTo(EstadoCotizacion.ENVIADA);
	}

	@Test
	void filtraPorEstado() {
		repositorio.guardar(borradorCon("uno@empresa.com", new ItemDeCotizacion("A", 1, Dinero.de(1))));
		Cotizacion enviada = borradorCon("dos@empresa.com", new ItemDeCotizacion("B", 1, Dinero.de(1)));
		enviada.enviar(NumeroDeCotizacion.de(2026, 1), AHORA);
		repositorio.guardar(enviada);

		assertThat(repositorio.listarPorEstado(EstadoCotizacion.ENVIADA))
				.extracting(c -> c.cliente().correo().valor())
				.containsExactly("dos@empresa.com");
	}

	// La vista del cliente no puede depender de como escribio su correo.
	@Test
	void buscaPorCorreoDelClienteSinDistinguirMayusculas() {
		repositorio.guardar(borradorCon("Cliente@Empresa.com", new ItemDeCotizacion("A", 1, Dinero.de(1))));

		assertThat(repositorio.listarPorCorreoDelCliente(new Correo("cliente@empresa.com"))).hasSize(1);
	}

	@Test
	void buscarUnaCotizacionInexistenteDevuelveVacio() {
		assertThat(repositorio.buscarPorId(CotizacionId.nuevo())).isEmpty();
	}

}
