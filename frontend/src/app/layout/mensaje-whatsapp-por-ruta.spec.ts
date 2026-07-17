import { HOME } from '../../contenido/home';
import { SERVICIOS } from '../../contenido/servicios';
import { mensajeWhatsappParaRuta } from './mensaje-whatsapp-por-ruta';

describe('mensajeWhatsappParaRuta', () => {
  it('devuelve el mensaje generico de home para la ruta raiz', () => {
    expect(mensajeWhatsappParaRuta('/')).toBe(HOME.mensajeWhatsapp);
  });

  it('devuelve el mensaje generico de home para rutas que no son de servicio', () => {
    expect(mensajeWhatsappParaRuta('/sobre-nosotros')).toBe(HOME.mensajeWhatsapp);
  });

  it('devuelve el mensaje propio del servicio cuando la ruta es de una pagina de servicio', () => {
    const servicio = SERVICIOS[1];
    expect(mensajeWhatsappParaRuta(`/servicios/${servicio.slug}`)).toBe(servicio.mensajeWhatsapp);
  });

  it('devuelve el mensaje generico si el slug de servicio no existe', () => {
    expect(mensajeWhatsappParaRuta('/servicios/no-existe')).toBe(HOME.mensajeWhatsapp);
  });
});
