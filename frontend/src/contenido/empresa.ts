import { CORREO_CORPORATIVO, WHATSAPP_NUMERO, WHATSAPP_NUMERO_INTERNACIONAL } from './legales';

export const EMPRESA = {
  razonSocial: 'Crear Code Cesar S.A.S.',
  // NAP completo para el SEO local: hasta sep 2026 la direccion y el NIT
  // solo salian en el PDF de cotizaciones. Mismos valores que los
  // defaults de app.empresa.* en el backend (certificado de Camara de
  // Comercio del 16 jul 2026).
  nit: '901941017-0',
  direccion: 'Calle 4B # 20-36, Oficina 303, Barrio Callejas',
  ciudad: 'Valledupar, Cesar, Colombia',
  whatsappNumero: WHATSAPP_NUMERO,
  whatsappNumeroInternacional: WHATSAPP_NUMERO_INTERNACIONAL,
  correo: CORREO_CORPORATIVO,
} as const;

export function urlWhatsapp(mensaje: string): string {
  return `https://wa.me/${EMPRESA.whatsappNumeroInternacional}?text=${encodeURIComponent(mensaje)}`;
}
