package com.crearcode.leads.infraestructura.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crearcode.leads.aplicacion.CotizacionNoEncontradaException;
import com.crearcode.leads.dominio.AbrirBorradorDeCotizacionUseCase;
import com.crearcode.leads.dominio.ConsultarCotizacionesUseCase;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.EditarBorradorDeCotizacionUseCase;
import com.crearcode.leads.dominio.EnviarCotizacionUseCase;
import com.crearcode.leads.dominio.EstadoCotizacion;
import com.crearcode.leads.dominio.GeneradorDeDocumento;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NuevoBorrador;
import com.crearcode.leads.dominio.Porcentaje;
import com.crearcode.leads.dominio.SolicitudId;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.CotizacionResponse;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.EditarBorradorRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.ItemRequest;
import com.crearcode.leads.infraestructura.rest.CotizacionDtos.NuevoBorradorRequest;

import jakarta.validation.Valid;

/**
 * API de cotizaciones del equipo (HU-44, HU-45, HU-47). Todo aquí exige
 * rol ADMIN, declarado en SecurityConfig — la vista del cliente vive en
 * {@link MisCotizacionesController}, con su propia autorización.
 */
@RestController
@RequestMapping("/api/cotizaciones")
class CotizacionController {

	private final AbrirBorradorDeCotizacionUseCase abrirBorrador;
	private final EditarBorradorDeCotizacionUseCase editarBorrador;
	private final EnviarCotizacionUseCase enviarCotizacion;
	private final ConsultarCotizacionesUseCase consultar;
	private final GeneradorDeDocumento generadorDeDocumento;
	private final int impuestoPorDefecto;

	CotizacionController(AbrirBorradorDeCotizacionUseCase abrirBorrador,
			EditarBorradorDeCotizacionUseCase editarBorrador, EnviarCotizacionUseCase enviarCotizacion,
			ConsultarCotizacionesUseCase consultar, GeneradorDeDocumento generadorDeDocumento,
			@Value("${app.cotizaciones.impuesto-por-defecto}") int impuestoPorDefecto) {
		this.abrirBorrador = abrirBorrador;
		this.editarBorrador = editarBorrador;
		this.enviarCotizacion = enviarCotizacion;
		this.consultar = consultar;
		this.generadorDeDocumento = generadorDeDocumento;
		this.impuestoPorDefecto = impuestoPorDefecto;
	}

	@PostMapping
	ResponseEntity<CotizacionResponse> abrir(@Valid @RequestBody NuevoBorradorRequest request) {
		DatosDelCliente cliente = request.cliente() == null ? null
				: new DatosDelCliente(request.cliente().nombre(), new Correo(request.cliente().correo()),
						request.cliente().telefono(), request.cliente().identificacion());

		Porcentaje impuesto = new Porcentaje(
				request.impuestoPorcentaje() == null ? impuestoPorDefecto : request.impuestoPorcentaje());

		Cotizacion cotizacion = abrirBorrador.abrir(new NuevoBorrador(
				request.origenSolicitudId() == null ? null : new SolicitudId(request.origenSolicitudId()),
				cliente, impuesto,
				request.diasDeValidez() == null ? 0 : request.diasDeValidez(),
				request.notas(), aItemsDeDominio(request.items())));

		return ResponseEntity.status(HttpStatus.CREATED).body(CotizacionResponse.desde(cotizacion));
	}

	@PutMapping("/{id}")
	CotizacionResponse editar(@PathVariable UUID id, @Valid @RequestBody EditarBorradorRequest request) {
		return CotizacionResponse.desde(
				editarBorrador.editar(new CotizacionId(id), aItemsDeDominio(request.items()), request.notas()));
	}

	@PostMapping("/{id}/envio")
	CotizacionResponse enviar(@PathVariable UUID id) {
		return CotizacionResponse.desde(enviarCotizacion.enviar(new CotizacionId(id)));
	}

	@DeleteMapping("/{id}")
	ResponseEntity<Void> cancelar(@PathVariable UUID id) {
		editarBorrador.cancelar(new CotizacionId(id));
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	List<CotizacionResponse> listar(@RequestParam(required = false) EstadoCotizacion estado) {
		return consultar.listar(estado).stream().map(CotizacionResponse::desde).toList();
	}

	@GetMapping("/{id}")
	CotizacionResponse obtener(@PathVariable UUID id) {
		CotizacionId cotizacionId = new CotizacionId(id);
		return consultar.obtener(cotizacionId).map(CotizacionResponse::desde)
				.orElseThrow(() -> new CotizacionNoEncontradaException(cotizacionId));
	}

	@GetMapping("/{id}/documento")
	ResponseEntity<byte[]> descargar(@PathVariable UUID id) {
		CotizacionId cotizacionId = new CotizacionId(id);
		Cotizacion cotizacion = consultar.obtener(cotizacionId)
				.orElseThrow(() -> new CotizacionNoEncontradaException(cotizacionId));
		return respuestaConPdf(cotizacion, generadorDeDocumento.generar(cotizacion));
	}

	static ResponseEntity<byte[]> respuestaConPdf(Cotizacion cotizacion, byte[] pdf) {
		String nombre = (cotizacion.numero() == null ? "cotizacion-borrador" : cotizacion.numero().valor())
				+ ".pdf";
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
				.body(pdf);
	}

	static List<ItemDeCotizacion> aItemsDeDominio(List<ItemRequest> items) {
		return items == null ? List.of()
				: items.stream()
						.map(item -> new ItemDeCotizacion(item.descripcion(), item.cantidad(),
								new Dinero(item.valorUnitario())))
						.toList();
	}

}
