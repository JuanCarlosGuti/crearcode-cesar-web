import { DOCUMENT } from '@angular/common';
import { inject } from '@angular/core';

import { EMPRESA } from '../../contenido/empresa';
import { BASE_URL, IMAGEN_OG_DEFECTO } from '../../contenido/sitio';
import { SOBRE_NOSOTROS } from '../../contenido/sobre-nosotros';

const ID_SCRIPT = 'datos-estructurados-empresa';

/**
 * Inyecta en <head> el JSON-LD de la empresa (schema.org
 * ProfessionalService): nombre, NIT, direccion, telefono, fundador y
 * area de servicio. Es lo que Google usa para el resultado local y lo
 * que faltaba por completo (auditoria del 28 sep 2026, seccion 7).
 *
 * Se arma desde las constantes del sitio y no como HTML fijo en
 * index.html, para que el dominio siga viviendo en un solo lugar
 * (ADR-06) y los datos de contacto en otro (contenido/empresa.ts).
 * Idempotente: el prerender y la hidratacion pueden llamarlo dos veces.
 */
export function establecerDatosEstructuradosDeLaEmpresa(): void {
  const documento = inject(DOCUMENT);
  if (documento.getElementById(ID_SCRIPT)) {
    return;
  }

  const datos = {
    '@context': 'https://schema.org',
    '@type': 'ProfessionalService',
    '@id': `${BASE_URL}/#empresa`,
    name: 'Crear Code Cesar',
    legalName: EMPRESA.razonSocial,
    taxID: EMPRESA.nit,
    url: BASE_URL,
    logo: `${BASE_URL}/apple-touch-icon.png`,
    image: `${BASE_URL}${IMAGEN_OG_DEFECTO}`,
    telephone: `+${EMPRESA.whatsappNumeroInternacional}`,
    email: EMPRESA.correo,
    address: {
      '@type': 'PostalAddress',
      streetAddress: EMPRESA.direccion,
      addressLocality: 'Valledupar',
      addressRegion: 'Cesar',
      addressCountry: 'CO',
    },
    areaServed: ['Valledupar', 'Cesar', 'La Guajira', 'Magdalena', 'Colombia'],
    founder: {
      '@type': 'Person',
      name: SOBRE_NOSOTROS.fundador.nombre,
      sameAs: SOBRE_NOSOTROS.fundador.linkedinUrl,
    },
    sameAs: [SOBRE_NOSOTROS.fundador.linkedinUrl, 'https://github.com/JuanCarlosGuti'],
    knowsAbout: [
      'Desarrollo de software a la medida',
      'Inteligencia artificial y automatización para pymes',
      'Cobro digital e integraciones',
    ],
  };

  const script = documento.createElement('script');
  script.id = ID_SCRIPT;
  script.type = 'application/ld+json';
  script.textContent = JSON.stringify(datos);
  documento.head.appendChild(script);
}
