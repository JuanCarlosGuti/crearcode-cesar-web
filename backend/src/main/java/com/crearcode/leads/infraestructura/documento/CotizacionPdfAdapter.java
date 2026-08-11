package com.crearcode.leads.infraestructura.documento;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.crearcode.leads.dominio.AsistenteNoDisponibleException;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.GeneradorDeDocumento;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Cotización en PDF (HU-48). El documento se identifica como
 * **cotización**: no es una factura ni una cuenta de cobro, y esa
 * distinción es justamente el alcance de la fase (decisión 18).
 */
@Component
class CotizacionPdfAdapter implements GeneradorDeDocumento {

	private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
			Locale.of("es", "CO"));
	private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

	private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
	private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private static final Font PEQUENA = FontFactory.getFont(FontFactory.HELVETICA, 8);
	private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

	private final DatosDeLaEmpresa empresa;

	CotizacionPdfAdapter(DatosDeLaEmpresa empresa) {
		this.empresa = empresa;
	}

	@Override
	public byte[] generar(Cotizacion cotizacion) {
		try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
			Document documento = new Document(PageSize.LETTER, 48, 48, 48, 48);
			PdfWriter.getInstance(documento, salida);
			documento.open();

			escribirEncabezado(documento, cotizacion);
			escribirDatosDelCliente(documento, cotizacion);
			documento.add(tablaDeItems(cotizacion));
			escribirTotales(documento, cotizacion);
			escribirPie(documento, cotizacion);

			documento.close();
			return salida.toByteArray();
		} catch (Exception fallo) {
			throw new AsistenteNoDisponibleException("No se pudo generar el PDF de la cotización", fallo);
		}
	}

	private void escribirEncabezado(Document documento, Cotizacion cotizacion) {
		Paragraph titulo = new Paragraph("COTIZACIÓN", TITULO);
		documento.add(titulo);

		String numero = cotizacion.numero() == null ? "(borrador)" : cotizacion.numero().valor();
		documento.add(parrafo("N.º " + numero, SUBTITULO, 4));
		documento.add(parrafo("Fecha: " + fecha(cotizacion.creadaEn()), NORMAL, 0));
		documento.add(parrafo("Válida hasta: " + fecha(cotizacion.validaHasta()), NORMAL, 12));

		documento.add(parrafo(empresa.razonSocial(), SUBTITULO, 0));
		if (empresa.tieneNit()) {
			documento.add(parrafo("NIT " + empresa.nit(), NORMAL, 0));
		}
		if (empresa.tieneDireccion()) {
			documento.add(parrafo(empresa.direccion() + ", " + empresa.ciudad(), NORMAL, 0));
		}
		documento.add(parrafo(empresa.telefono() + " · " + empresa.correo() + " · " + empresa.sitioWeb(),
				NORMAL, 16));
	}

	private void escribirDatosDelCliente(Document documento, Cotizacion cotizacion) {
		documento.add(parrafo("Cliente", SUBTITULO, 2));
		documento.add(parrafo(cotizacion.cliente().nombre(), NORMAL, 0));
		if (cotizacion.cliente().identificacion() != null) {
			documento.add(parrafo("Identificación: " + cotizacion.cliente().identificacion(), NORMAL, 0));
		}
		documento.add(parrafo(cotizacion.cliente().correo().valor(), NORMAL, 0));
		if (cotizacion.cliente().telefono() != null) {
			documento.add(parrafo(cotizacion.cliente().telefono(), NORMAL, 0));
		}
		documento.add(parrafo(" ", NORMAL, 8));
	}

	private PdfPTable tablaDeItems(Cotizacion cotizacion) {
		PdfPTable tabla = new PdfPTable(new float[] { 5f, 1f, 2f, 2f });
		tabla.setWidthPercentage(100);
		tabla.setSpacingAfter(12);

		for (String encabezado : new String[] { "Descripción", "Cant.", "Valor unitario", "Subtotal" }) {
			PdfPCell celda = new PdfPCell(new Phrase(encabezado, SUBTITULO));
			celda.setPadding(6);
			celda.setGrayFill(0.92f);
			tabla.addCell(celda);
		}

		for (ItemDeCotizacion item : cotizacion.items()) {
			tabla.addCell(celda(item.descripcion(), Element.ALIGN_LEFT));
			tabla.addCell(celda(String.valueOf(item.cantidad()), Element.ALIGN_CENTER));
			tabla.addCell(celda(pesos(item.valorUnitario()), Element.ALIGN_RIGHT));
			tabla.addCell(celda(pesos(item.subtotal()), Element.ALIGN_RIGHT));
		}
		return tabla;
	}

	private void escribirTotales(Document documento, Cotizacion cotizacion) {
		documento.add(alineadoALaDerecha("Subtotal: " + pesos(cotizacion.subtotal()), NORMAL));
		if (cotizacion.impuesto().valor() > 0) {
			documento.add(alineadoALaDerecha(
					"IVA (" + cotizacion.impuesto().valor() + "%): " + pesos(cotizacion.impuestoCalculado()),
					NORMAL));
		}
		documento.add(alineadoALaDerecha("Total: " + pesos(cotizacion.total()), TOTAL));
	}

	private void escribirPie(Document documento, Cotizacion cotizacion) {
		if (cotizacion.notas() != null && !cotizacion.notas().isBlank()) {
			documento.add(parrafo("Notas", SUBTITULO, 16));
			documento.add(parrafo(cotizacion.notas(), NORMAL, 0));
		}
		documento.add(parrafo(
				"Este documento es una cotización: una propuesta comercial, no una factura ni una cuenta de cobro. "
						+ "Los valores están en pesos colombianos y rigen hasta la fecha de validez indicada.",
				PEQUENA, 24));
	}

	private static Paragraph parrafo(String texto, Font fuente, float espacioDespues) {
		Paragraph parrafo = new Paragraph(texto, fuente);
		parrafo.setSpacingAfter(espacioDespues);
		return parrafo;
	}

	private static Paragraph alineadoALaDerecha(String texto, Font fuente) {
		Paragraph parrafo = new Paragraph(texto, fuente);
		parrafo.setAlignment(Element.ALIGN_RIGHT);
		return parrafo;
	}

	private static PdfPCell celda(String texto, int alineacion) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, NORMAL));
		celda.setPadding(6);
		celda.setHorizontalAlignment(alineacion);
		return celda;
	}

	private static String pesos(Dinero dinero) {
		NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
		formato.setMaximumFractionDigits(0);
		return formato.format(dinero.monto());
	}

	private static String fecha(java.time.Instant instante) {
		return FECHA.format(instante.atZone(ZONA_COLOMBIA));
	}

}
