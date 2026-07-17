import { HOME } from '../../contenido/home';
import { SERVICIOS } from '../../contenido/servicios';

export function mensajeWhatsappParaRuta(url: string): string {
  const coincidencia = url.match(/^\/servicios\/([^/?#]+)/);
  const servicio = coincidencia ? SERVICIOS.find((s) => s.slug === coincidencia[1]) : undefined;
  return servicio ? servicio.mensajeWhatsapp : HOME.mensajeWhatsapp;
}
