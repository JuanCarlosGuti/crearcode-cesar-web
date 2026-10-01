package com.crearcode.leads.infraestructura.rest;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.aplicacion.ProyectoNoEncontradoException;
import com.crearcode.leads.dominio.ConsultarProyectosUseCase;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.CrearProyectoUseCase;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EntregableId;
import com.crearcode.leads.dominio.FaseId;
import com.crearcode.leads.dominio.GestionarProyectoUseCase;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoInvalidoException;
import com.crearcode.leads.dominio.UrlDeDemo;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.CambioDeEstadoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.DemoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.DescripcionRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.EntregableRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.FaseRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.MovimientoDeEntregableRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.NuevaFaseRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.NuevoEntregableRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.NuevoProyectoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.PagoRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.PosicionRequest;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ProyectoResponse;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ProyectoResumenResponse;
import com.crearcode.leads.infraestructura.rest.ProyectoDtos.ResumenRequest;

/**
 * API de proyectos del equipo (HU-53 a HU-56). Todo exige rol ADMIN,
 * declarado en SecurityConfig; la vista del cliente vive en
 * {@link MisProyectosController}. Cada cambio devuelve el proyecto
 * entero, ya recalculado, para que el panel no tenga que pedirlo otra
 * vez.
 */
@RestController
@RequestMapping("/api/proyectos")
class ProyectoController {

	private final CrearProyectoUseCase crear;
	private final GestionarProyectoUseCase gestionar;
	private final ConsultarProyectosUseCase consultar;
	private final int impuestoPorDefecto;

	ProyectoController(CrearProyectoUseCase crear, GestionarProyectoUseCase gestionar,
			ConsultarProyectosUseCase consultar,
			@Value("${app.cotizaciones.impuesto-por-defecto}") int impuestoPorDefecto) {
		this.crear = crear;
		this.gestionar = gestionar;
		this.consultar = consultar;
		this.impuestoPorDefecto = impuestoPorDefecto;
	}

	@GetMapping
	List<ProyectoResumenResponse> listar() {
		return consultar.listar().stream().map(ProyectoResumenResponse::desde).toList();
	}

	@GetMapping("/{id}")
	ProyectoResponse obtener(@PathVariable UUID id) {
		ProyectoId proyectoId = new ProyectoId(id);
		return respuesta(consultar.obtener(proyectoId).orElseThrow(() -> new ProyectoNoEncontradoException(proyectoId)));
	}

	/**
	 * HU-53: el detalle de una cotización aceptada pregunta si ya tiene
	 * proyecto, para ofrecer crearlo o llevar al que existe.
	 */
	@GetMapping("/de-cotizacion/{cotizacion}")
	ProyectoResumenResponse deLaCotizacion(@PathVariable UUID cotizacion) {
		CotizacionId cotizacionId = new CotizacionId(cotizacion);
		return consultar.deLaCotizacion(cotizacionId).map(ProyectoResumenResponse::desde)
				.orElseThrow(() -> new CotizacionSinProyectoException(cotizacionId));
	}

	/** HU-53: el plan que propone la cotización, en la misma forma que se envía al crear. */
	@GetMapping("/propuesta")
	List<NuevaFaseRequest> proponer(@RequestParam UUID cotizacion) {
		return crear.proponerPlan(new CotizacionId(cotizacion)).stream().map(NuevaFaseRequest::desde).toList();
	}

	@PostMapping
	ResponseEntity<ProyectoResponse> crear(@Valid @RequestBody NuevoProyectoRequest request) {
		Proyecto proyecto;
		if (request.cotizacionId() != null) {
			proyecto = crear.desdeCotizacion(new CotizacionId(request.cotizacionId()),
					request.descripcion().aDominio(), NuevaFaseRequest.aDominio(request.plan()));
		}
		else {
			if (request.cliente() == null) {
				throw new ProyectoInvalidoException(
						"El proyecto necesita la cotización de origen o los datos del cliente");
			}
			Porcentaje impuesto = new Porcentaje(
					request.impuestoPorcentaje() == null ? impuestoPorDefecto : request.impuestoPorcentaje());
			proyecto = crear.enBlanco(new Correo(request.cliente().correo()), request.cliente().nombre(),
					request.descripcion().aDominio(), impuesto, NuevaFaseRequest.aDominio(request.plan()));
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(respuesta(proyecto));
	}

	@PutMapping("/{id}/descripcion")
	ProyectoResponse cambiarDescripcion(@PathVariable UUID id, @Valid @RequestBody DescripcionRequest request) {
		return respuesta(gestionar.cambiarDescripcion(new ProyectoId(id), request.aDominio()));
	}

	// --- Fases ---

	@PostMapping("/{id}/fases")
	ProyectoResponse agregarFase(@PathVariable UUID id, @Valid @RequestBody FaseRequest request) {
		return respuesta(gestionar.agregarFase(new ProyectoId(id), request.aDominio()));
	}

	@PutMapping("/{id}/fases/{fase}")
	ProyectoResponse editarFase(@PathVariable UUID id, @PathVariable UUID fase,
			@Valid @RequestBody FaseRequest request) {
		return respuesta(gestionar.editarFase(new ProyectoId(id), new FaseId(fase), request.aDominio()));
	}

	@PutMapping("/{id}/fases/{fase}/posicion")
	ProyectoResponse moverFase(@PathVariable UUID id, @PathVariable UUID fase,
			@Valid @RequestBody PosicionRequest request) {
		return respuesta(gestionar.moverFase(new ProyectoId(id), new FaseId(fase), request.posicion()));
	}

	@DeleteMapping("/{id}/fases/{fase}")
	ProyectoResponse quitarFase(@PathVariable UUID id, @PathVariable UUID fase) {
		return respuesta(gestionar.quitarFase(new ProyectoId(id), new FaseId(fase)));
	}

	@PutMapping("/{id}/fases/{fase}/resumen")
	ProyectoResponse escribirResumen(@PathVariable UUID id, @PathVariable UUID fase,
			@Valid @RequestBody ResumenRequest request) {
		return respuesta(gestionar.escribirResumenDeFase(new ProyectoId(id), new FaseId(fase), request.resumen()));
	}

	// --- Entregables ---

	@PostMapping("/{id}/fases/{fase}/entregables")
	ProyectoResponse agregarEntregable(@PathVariable UUID id, @PathVariable UUID fase,
			@Valid @RequestBody NuevoEntregableRequest request) {
		return respuesta(gestionar.agregarEntregable(new ProyectoId(id), new FaseId(fase),
				request.entregable().aDominio(), Boolean.TRUE.equals(request.esCambioDeAlcance())));
	}

	@PutMapping("/{id}/entregables/{entregable}")
	ProyectoResponse editarEntregable(@PathVariable UUID id, @PathVariable UUID entregable,
			@Valid @RequestBody EntregableRequest request) {
		return respuesta(gestionar.editarEntregable(new ProyectoId(id), new EntregableId(entregable),
				request.aDominio()));
	}

	@PutMapping("/{id}/entregables/{entregable}/demo")
	ProyectoResponse ponerDemo(@PathVariable UUID id, @PathVariable UUID entregable,
			@RequestBody DemoRequest request) {
		UrlDeDemo demo = request.url() == null || request.url().isBlank() ? null : new UrlDeDemo(request.url());
		return respuesta(gestionar.ponerDemo(new ProyectoId(id), new EntregableId(entregable), demo));
	}

	@PutMapping("/{id}/entregables/{entregable}/posicion")
	ProyectoResponse moverEntregable(@PathVariable UUID id, @PathVariable UUID entregable,
			@Valid @RequestBody MovimientoDeEntregableRequest request) {
		return respuesta(gestionar.moverEntregable(new ProyectoId(id), new EntregableId(entregable),
				new FaseId(request.faseId()), request.posicion()));
	}

	@DeleteMapping("/{id}/entregables/{entregable}")
	ProyectoResponse quitarEntregable(@PathVariable UUID id, @PathVariable UUID entregable) {
		return respuesta(gestionar.quitarEntregable(new ProyectoId(id), new EntregableId(entregable)));
	}

	@PostMapping("/{id}/entregables/{entregable}/estado")
	ProyectoResponse cambiarEstado(@PathVariable UUID id, @PathVariable UUID entregable,
			@Valid @RequestBody CambioDeEstadoRequest request) {
		return respuesta(gestionar.cambiarEstadoDeEntregable(new ProyectoId(id), new EntregableId(entregable),
				request.estado(), request.nota()));
	}

	// --- Pagos y estado del proyecto ---

	/** Quién registra el pago sale del token, nunca de la petición. */
	@PostMapping("/{id}/pagos")
	ProyectoResponse registrarPago(@PathVariable UUID id, @Valid @RequestBody PagoRequest request,
			Authentication autenticacion) {
		return respuesta(gestionar.registrarPago(new ProyectoId(id), new EntregableId(request.entregableId()),
				new Dinero(request.monto()), request.fecha(), request.medio(), request.referencia(),
				new Correo(autenticacion.getName())));
	}

	@PostMapping("/{id}/pausa")
	ProyectoResponse pausar(@PathVariable UUID id) {
		return respuesta(gestionar.pausar(new ProyectoId(id)));
	}

	@PostMapping("/{id}/reanudacion")
	ProyectoResponse reanudar(@PathVariable UUID id) {
		return respuesta(gestionar.reanudar(new ProyectoId(id)));
	}

	@PostMapping("/{id}/cierre")
	ProyectoResponse cerrar(@PathVariable UUID id) {
		return respuesta(gestionar.cerrar(new ProyectoId(id)));
	}

	private ProyectoResponse respuesta(Proyecto proyecto) {
		return ProyectoResponse.desde(proyecto, consultar.clienteTieneCuenta(proyecto.correoDelCliente()));
	}

}
