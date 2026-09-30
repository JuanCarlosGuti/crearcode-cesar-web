package com.crearcode.leads.infraestructura;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.CifradorDeContrasenas;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.DatosDeEntregable;
import com.crearcode.leads.dominio.DatosDeFase;
import com.crearcode.leads.dominio.DescripcionDelProyecto;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.EstadoEntregable;
import com.crearcode.leads.dominio.FaseId;
import com.crearcode.leads.dominio.MedioDePago;
import com.crearcode.leads.dominio.MomentoDeCobro;
import com.crearcode.leads.dominio.NuevaFase;
import com.crearcode.leads.dominio.NuevoPago;
import com.crearcode.leads.dominio.OrigenDePago;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.Rol;
import com.crearcode.leads.dominio.Usuario;
import com.crearcode.leads.dominio.UrlDeDemo;
import com.crearcode.leads.dominio.UsuarioRepositorio;

/**
 * Proyecto de demostración de la fase F12 (ISS-214): un cliente
 * ficticio con tres fases, siete entregables en estados variados y dos
 * pagos, para recorrer el portal en local como lo vería un cliente.
 *
 * <p>
 * Crea también la cuenta del cliente, con una contraseña que está en
 * este archivo. Por eso tiene dos candados: solo existe si se enciende
 * {@code app.demo.proyecto-de-demostracion} —apagado por defecto, y el
 * despliegue no lo enciende—, y aun encendido se niega a arrancar donde
 * no se admiten credenciales de desarrollo, que es exactamente
 * producción ({@code PERMITIR_CREDENCIALES_DE_DESARROLLO=false}). Es una
 * propiedad y no un perfil de Spring porque el proyecto no usa perfiles
 * (ver {@code CredencialesDeArranque}).
 *
 * <p>
 * Nunca es una migración de Flyway: una migración corre en todos los
 * entornos, incluido el de producción.
 */
@Component
@ConditionalOnProperty(name = "app.demo.proyecto-de-demostracion", havingValue = "true")
class CargadorDeProyectoDeDemostracion implements ApplicationRunner {

	static final String CORREO_DEL_CLIENTE = "cliente.demo@crearcode-cesar.local";
	static final String CONTRASENA_DEL_CLIENTE = "demo-del-portal";

	private static final Logger LOG = LoggerFactory.getLogger(CargadorDeProyectoDeDemostracion.class);

	private final ProyectoRepositorio proyectos;
	private final UsuarioRepositorio usuarios;
	private final CifradorDeContrasenas cifrador;
	private final Clock reloj;
	private final String correoDelEquipo;

	CargadorDeProyectoDeDemostracion(ProyectoRepositorio proyectos, UsuarioRepositorio usuarios,
			CifradorDeContrasenas cifrador, Clock reloj, @Value("${app.admin.username}") String correoDelEquipo,
			@Value("${app.seguridad.permitir-credenciales-de-desarrollo}") boolean permitirCredencialesDeDesarrollo) {
		if (!permitirCredencialesDeDesarrollo) {
			throw new IllegalStateException("El proyecto de demostración crea una cuenta con contraseña conocida y "
					+ "no se carga donde PERMITIR_CREDENCIALES_DE_DESARROLLO=false. Apague "
					+ "CARGAR_PROYECTO_DE_DEMOSTRACION en este entorno.");
		}
		this.proyectos = proyectos;
		this.usuarios = usuarios;
		this.cifrador = cifrador;
		this.reloj = reloj;
		this.correoDelEquipo = correoDelEquipo;
	}

	@Override
	public void run(ApplicationArguments argumentos) {
		Correo cliente = new Correo(CORREO_DEL_CLIENTE);
		if (usuarios.buscarPorCorreo(cliente).isEmpty()) {
			usuarios.guardar(Usuario.crear(cliente, cifrador.hash(CONTRASENA_DEL_CLIENTE), Rol.CLIENTE));
		}
		if (!proyectos.listarPorCorreoDelCliente(cliente).isEmpty()) {
			return;
		}
		proyectos.guardar(proyectoDeDemostracion(cliente));
		LOG.warn("Proyecto de demostración cargado. Entra a /ingreso con {} / {} — solo para uso local.",
				CORREO_DEL_CLIENTE, CONTRASENA_DEL_CLIENTE);
	}

	private Proyecto proyectoDeDemostracion(Correo cliente) {
		Instant ahora = Instant.now(reloj);
		LocalDate hoy = LocalDate.now(reloj);
		Instant hace = ahora.minus(Duration.ofDays(20));
		Correo equipo = new Correo(correoDelEquipo);

		Proyecto proyecto = Proyecto.enBlanco(cliente, "Café Valle (demostración)",
				new DescripcionDelProyecto("Tienda en línea de Café Valle",
						"Tu tienda para vender café por internet, con catálogo, carrito y pedidos.",
						hoy.minusDays(30), hoy.plusDays(45)),
				new Porcentaje(19),
				List.of(
						new NuevaFase(new DatosDeFase("Descubrimiento y diseño",
								"Que veas cómo se va a ver tu tienda antes de construirla.", hoy.minusDays(30),
								hoy.minusDays(15)), List.of(
										entregable("Identidad visual", "Logo, colores y tipografía de la tienda.",
												1_200_000, MomentoDeCobro.AL_INICIAR),
										entregable("Diseño de pantallas", "Cómo se ven el inicio, el catálogo y el pago.",
												1_800_000, MomentoDeCobro.AL_APROBAR))),
						new NuevaFase(new DatosDeFase("Construcción de la tienda",
								"Que tus clientes puedan comprar de principio a fin.", hoy.minusDays(14),
								hoy.plusDays(20)), List.of(
										entregable("Catálogo de productos", "Tus cafés con foto, precio y variedades.",
												2_500_000, MomentoDeCobro.AL_APROBAR),
										entregable("Carrito y pagos", "Comprar y pagar desde el celular.", 3_000_000,
												MomentoDeCobro.AL_APROBAR),
										entregable("Panel de pedidos", "Dónde ves y despachas cada pedido.",
												2_000_000, MomentoDeCobro.AL_APROBAR))),
						new NuevaFase(new DatosDeFase("Lanzamiento", "Que la tienda salga al aire y sepas manejarla.",
								hoy.plusDays(21), hoy.plusDays(45)), List.of(
										entregable("Capacitación", "Una sesión para ti y tu equipo.", 500_000,
												MomentoDeCobro.AL_APROBAR)))),
				hace);

		FaseId primera = proyecto.fases().getFirst().id();
		FaseId tercera = proyecto.fases().get(2).id();
		EntregableId identidad = id(proyecto, "Identidad visual");
		EntregableId pantallas = id(proyecto, "Diseño de pantallas");
		EntregableId catalogo = id(proyecto, "Catálogo de productos");
		EntregableId carrito = id(proyecto, "Carrito y pagos");
		EntregableId pedidos = id(proyecto, "Panel de pedidos");

		revisar(proyecto, identidad, hace);
		proyecto.aprobarComoCliente(identidad, cliente, hace.plus(Duration.ofDays(2)));
		revisar(proyecto, pantallas, hace.plus(Duration.ofDays(3)));
		proyecto.cambiarEstadoDeEntregable(pantallas, EstadoEntregable.APROBADO, null, hace.plus(Duration.ofDays(5)));
		proyecto.escribirResumenDeFase(primera, "Quedaron listos el logo, los colores y el diseño de todas las "
				+ "pantallas. Los aprobaste y con eso arrancamos la construcción.", hace.plus(Duration.ofDays(5)));

		revisar(proyecto, catalogo, ahora.minus(Duration.ofDays(1)));
		proyecto.ponerDemo(catalogo, new UrlDeDemo("https://crearcodecesar.com"), ahora.minus(Duration.ofDays(1)));
		proyecto.cambiarEstadoDeEntregable(carrito, EstadoEntregable.EN_CURSO, null, ahora.minus(Duration.ofDays(3)));
		revisar(proyecto, pedidos, ahora.minus(Duration.ofDays(4)));
		proyecto.pedirAjustesComoCliente(pedidos, cliente,
				"Quisiera ver los pedidos del día separados de los anteriores.", ahora.minus(Duration.ofDays(2)));
		proyecto.cambiarEstadoDeEntregable(pedidos, EstadoEntregable.EN_CURSO, null, ahora.minus(Duration.ofDays(2)));
		proyecto.cambiarEstadoDeEntregable(pedidos, EstadoEntregable.EN_REVISION, null, ahora.minus(Duration.ofDays(1)));
		proyecto.pedirAjustesComoCliente(pedidos, cliente, "Faltó el filtro por fecha.", ahora);

		proyecto.agregarEntregable(tercera, entregable("Cupones de descuento",
				"Lo pediste a mitad del proyecto: se agrega con su propio valor.", 800_000,
				MomentoDeCobro.AL_APROBAR), true, ahora);

		proyecto.registrarPago(new NuevoPago(identidad, Dinero.de(1_428_000), hoy.minusDays(28),
				MedioDePago.TRANSFERENCIA, OrigenDePago.MANUAL, "Anticipo", equipo), ahora);
		proyecto.registrarPago(new NuevoPago(pantallas, Dinero.de(1_000_000), hoy.minusDays(10),
				MedioDePago.NEQUI_DAVIPLATA, OrigenDePago.MANUAL, "Abono", equipo), ahora);
		return proyecto;
	}

	private static DatosDeEntregable entregable(String nombre, String descripcion, long valor,
			MomentoDeCobro cobro) {
		return new DatosDeEntregable(nombre, descripcion, Dinero.de(valor), cobro);
	}

	private static EntregableId id(Proyecto proyecto, String nombre) {
		return proyecto.entregables().stream().filter(e -> e.nombre().equals(nombre)).findFirst().orElseThrow().id();
	}

	private static void revisar(Proyecto proyecto, EntregableId entregable, Instant cuando) {
		proyecto.cambiarEstadoDeEntregable(entregable, EstadoEntregable.EN_CURSO, null, cuando);
		proyecto.cambiarEstadoDeEntregable(entregable, EstadoEntregable.EN_REVISION, null, cuando);
	}

}
