package com.crearcode.leads.infraestructura.documento;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.Cotizacion;
import com.crearcode.leads.dominio.DatosDelCliente;
import com.crearcode.leads.dominio.Dinero;
import com.crearcode.leads.dominio.ItemDeCotizacion;
import com.crearcode.leads.dominio.NumeroDeCotizacion;
import com.crearcode.leads.dominio.Porcentaje;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El PDF se verifica leyéndolo de vuelta: que abra como PDF válido y que
 * su texto contenga lo que el cliente debe ver (HU-48).
 */
class CotizacionPdfAdapterIT {

	private static final Instant AHORA = Instant.parse("2026-08-11T15:00:00Z");
	private static final Instant EN_QUINCE_DIAS = Instant.parse("2026-08-26T15:00:00Z");

	/** Los datos reales del certificado de Cámara de Comercio. */
	private final DatosDeLaEmpresa empresa = new DatosDeLaEmpresa("Crear Code Cesar S.A.S.",
			"901941017-0", "Calle 4B # 20-36, Oficina 303, Barrio Callejas", "Valledupar, Cesar",
			"323 988 5883", "admin@crearcodecesar.com", "crearcodecesar.com");
	private final CotizacionPdfAdapter adapter = new CotizacionPdfAdapter(empresa);

	/** Config incompleta: el PDF debe omitir las líneas, no inventarlas. */
	private final CotizacionPdfAdapter adapterSinDatosFiscales = new CotizacionPdfAdapter(
			new DatosDeLaEmpresa("Crear Code Cesar S.A.S.", "", "", "Valledupar, Cesar", "323 988 5883",
					"admin@crearcodecesar.com", "crearcodecesar.com"));

	private Cotizacion cotizacionEnviada(Porcentaje impuesto) {
		Cotizacion cotizacion = Cotizacion.abrirBorrador(
				new DatosDelCliente("Panaderia El Trigal", new Correo("cliente@empresa.com"), "3001234567",
						"900123456-1"),
				impuesto, AHORA, EN_QUINCE_DIAS, null, "Incluye una capacitacion de dos horas");
		cotizacion.agregarItem(new ItemDeCotizacion("Desarrollo del modulo de pedidos", 1,
				Dinero.de(5_000_000)));
		cotizacion.agregarItem(new ItemDeCotizacion("Horas de acompanamiento", 10, Dinero.de(100_000)));
		cotizacion.enviar(NumeroDeCotizacion.de(2026, 7), AHORA);
		return cotizacion;
	}

	private static String textoDe(byte[] pdf) throws Exception {
		PdfReader lector = new PdfReader(new ByteArrayInputStream(pdf));
		try {
			StringBuilder texto = new StringBuilder();
			PdfTextExtractor extractor = new PdfTextExtractor(lector);
			for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
				texto.append(extractor.getTextFromPage(pagina));
			}
			return texto.toString();
		} finally {
			lector.close();
		}
	}

	@Test
	void generaUnPdfValido() throws Exception {
		byte[] pdf = adapter.generar(cotizacionEnviada(new Porcentaje(19)));

		assertThat(pdf).isNotEmpty();
		assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
		assertThat(new PdfReader(new ByteArrayInputStream(pdf)).getNumberOfPages()).isEqualTo(1);
	}

	@Test
	void elDocumentoLlevaElNumeroLosDatosDelClienteYLosItems() throws Exception {
		String texto = textoDe(adapter.generar(cotizacionEnviada(new Porcentaje(19))));

		assertThat(texto).contains("COTIZACIÓN");
		assertThat(texto).contains("COT-2026-0007");
		assertThat(texto).contains("Crear Code Cesar S.A.S.");
		assertThat(texto).contains("Panaderia El Trigal");
		assertThat(texto).contains("900123456-1");
		assertThat(texto).contains("Desarrollo del modulo de pedidos");
		assertThat(texto).contains("Horas de acompanamiento");
		assertThat(texto).contains("Incluye una capacitacion de dos horas");
	}

	@Test
	void muestraSubtotalImpuestoYTotalCalculados() throws Exception {
		String texto = textoDe(adapter.generar(cotizacionEnviada(new Porcentaje(19))));

		// 5.000.000 + 10 x 100.000 = 6.000.000; IVA 19% = 1.140.000
		assertThat(texto).contains("6.000.000");
		assertThat(texto).contains("IVA (19%)");
		assertThat(texto).contains("1.140.000");
		assertThat(texto).contains("7.140.000");
	}

	// Sin impuesto configurado (el caso mientras el usuario confirma su
	// condicion de IVA) el documento no menciona un IVA que no aplica.
	@Test
	void sinImpuestoNoImprimeLaLineaDeIva() throws Exception {
		String texto = textoDe(adapter.generar(cotizacionEnviada(new Porcentaje(0))));

		assertThat(texto).doesNotContain("IVA");
		assertThat(texto).contains("6.000.000");
	}

	// Decision 18: el documento nunca puede pasar por factura.
	@Test
	void seIdentificaComoCotizacionYNoComoFactura() throws Exception {
		String texto = textoDe(adapter.generar(cotizacionEnviada(new Porcentaje(19))));

		assertThat(texto).contains("no una factura ni una cuenta de cobro");
	}

	@Test
	void imprimeElNitYLaDireccionFiscalDeLaEmpresa() throws Exception {
		String texto = textoDe(adapter.generar(cotizacionEnviada(new Porcentaje(19))));

		assertThat(texto).contains("NIT 901941017-0");
		assertThat(texto).contains("Calle 4B # 20-36, Oficina 303, Barrio Callejas");
		assertThat(texto).contains("Valledupar, Cesar");
	}

	@Test
	void sinNitNiDireccionConfiguradosNoImprimeLineasVacias() throws Exception {
		String texto = textoDe(adapterSinDatosFiscales.generar(cotizacionEnviada(new Porcentaje(19))));

		assertThat(texto).doesNotContain("NIT ");
	}

}
