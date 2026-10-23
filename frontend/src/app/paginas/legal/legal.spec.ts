import { TestBed } from '@angular/core/testing';

import { POLITICA_DE_DATOS, TERMINOS_DE_USO } from '../../../contenido/legales';
import { LegalPage } from './legal';

describe('LegalPage', () => {
  async function pintar(documento: typeof POLITICA_DE_DATOS | typeof TERMINOS_DE_USO, ruta: string) {
    const fixture = TestBed.createComponent(LegalPage);
    fixture.componentRef.setInput('documento', documento);
    fixture.componentRef.setInput('ruta', ruta);
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('muestra el titulo y todas las secciones del documento recibido', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');

    expect(el.querySelector('h1')?.textContent).toBe(POLITICA_DE_DATOS.titulo);
    const texto = el.textContent ?? '';
    POLITICA_DE_DATOS.secciones.forEach((seccion) => {
      expect(texto).toContain(seccion.titulo);
      seccion.parrafos.forEach((parrafo) => expect(texto).toContain(parrafo));
    });
  });

  it('cada seccion es un encabezado de verdad, no un parrafo en negrita', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');

    const encabezados = Array.from(el.querySelectorAll('h2')).map((h) => h.textContent?.trim());
    expect(encabezados).toEqual(POLITICA_DE_DATOS.secciones.map((seccion) => seccion.titulo));
  });

  /**
   * El aviso de "esto es un borrador" estuvo publicado en las dos
   * paginas legales: un visitante que abria la politica de datos leia
   * que la empresa no la habia terminado de revisar (auditoria P0-3).
   */
  it('no publica ningun aviso de borrador', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');

    expect(el.textContent).not.toContain('borrador');
  });

  /**
   * La propia politica promete que "la fecha de la version vigente se
   * indica al pie de esta pagina", y no se indicaba en ninguna parte.
   */
  it('indica la version y la fecha de vigencia al pie', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');

    const pie = el.querySelector('.pagina-legal__vigencia');
    expect(pie?.textContent).toContain('v2.2');
    expect(pie?.textContent).toContain('23 de octubre de 2026');
  });

  /**
   * Cada documento lleva su propia fecha. Era una sola constante para
   * los dos, y al cambiar la politica los terminos —que no cambiaron—
   * habrian aparecido con una fecha de vigencia falsa.
   */
  it('los terminos conservan su propia version y fecha', async () => {
    const { el } = await pintar(TERMINOS_DE_USO, '/legales/terminos');

    const pie = el.querySelector('.pagina-legal__vigencia');
    expect(pie?.textContent).toContain('v2');
    expect(pie?.textContent).toContain('28 de septiembre de 2026');
  });

  /**
   * La politica no promete lo que hacen los proveedores de IA con los
   * datos: no se puede verificar ni hacerle seguimiento (decision del
   * dueno, 30 sep 2026). Lo que si promete depende de nosotros, y la
   * proteccion real es pedir que no se escriban datos sensibles.
   */
  it('no promete lo que hacen los proveedores de IA y pide no escribir datos sensibles', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');
    const texto = el.textContent ?? '';

    expect(texto).not.toContain('autorizamos a los proveedores');
    expect(texto).toContain('no usamos tus conversaciones para entrenar modelos propios');
    expect(texto).toContain('se rige por sus propias condiciones de uso');
    expect(texto).toContain('no escribir en estas herramientas datos sensibles');
  });

  it('funciona igual para el documento de terminos de uso', async () => {
    const { el } = await pintar(TERMINOS_DE_USO, '/legales/terminos');

    expect(el.querySelector('h1')?.textContent).toBe(TERMINOS_DE_USO.titulo);
    expect(el.textContent).not.toContain('borrador');
  });

  /**
   * Lo que el visitante escribe en las herramientas sale del pais hacia
   * proveedores de IA, y la politica lo dice. Desde la v2.2 (decision 34
   * de docs/10) los de IA van en generico: la norma no exige nombrar
   * encargados, los derechos se ejercen ante el responsable y quien
   * recibe que esta en el registro interno de docs/11 §1.7. Los demas
   * encargados siguen con nombre.
   */
  it('la politica describe a los encargados y la transferencia internacional', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');
    const texto = el.textContent ?? '';

    ['Resend', 'Netcup', 'Google', 'Cloudflare'].forEach((encargado) => expect(texto).toContain(encargado));
    expect(texto).toContain('Proveedores de modelos de inteligencia artificial');
    expect(texto).toContain('Estados Unidos');
  });

  /**
   * Con Claude, lo que el visitante escribe en el demo si sale del sitio
   * (decision 33). La v2.1 decia lo contrario, y una politica que dice
   * algo falso no puede volver.
   */
  it('dice que el demo de diseno manda el texto del visitante a un modelo de lenguaje', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');
    const texto = el.textContent ?? '';

    expect(texto).toContain('el diagnóstico y el demo de diseño envían tu texto a un modelo de lenguaje');
    expect(texto).not.toContain('no el texto que tú escribiste');
  });
});
