import { CORREO_CORPORATIVO, WHATSAPP_NUMERO, WHATSAPP_NUMERO_INTERNACIONAL } from './legales';

export const EMPRESA = {
  razonSocial: 'Crear Code Cesar S.A.S.',
  ciudad: 'Valledupar, Cesar, Colombia',
  whatsappNumero: WHATSAPP_NUMERO,
  whatsappNumeroInternacional: WHATSAPP_NUMERO_INTERNACIONAL,
  correo: CORREO_CORPORATIVO,
} as const;

export function urlWhatsapp(mensaje: string): string {
  return `https://wa.me/${EMPRESA.whatsappNumeroInternacional}?text=${encodeURIComponent(mensaje)}`;
}
