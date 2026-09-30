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
    expect(pie?.textContent).toContain(POLITICA_DE_DATOS.version);
    expect(pie?.textContent).toContain('28 de septiembre de 2026');
  });

  it('funciona igual para el documento de terminos de uso', async () => {
    const { el } = await pintar(TERMINOS_DE_USO, '/legales/terminos');

    expect(el.querySelector('h1')?.textContent).toBe(TERMINOS_DE_USO.titulo);
    expect(el.textContent).not.toContain('borrador');
  });

  /**
   * Lo que el visitante escribe en las herramientas sale del pais hacia
   * proveedores de IA. La politica tiene que nombrarlos: sin nombre no
   * hay forma de que nadie ejerza ningun derecho sobre ese dato.
   */
  it('la politica nombra a los encargados del tratamiento y la transferencia internacional', async () => {
    const { el } = await pintar(POLITICA_DE_DATOS, '/legales/politica-de-datos');
    const texto = el.textContent ?? '';

    ['Groq', 'Cloudflare', 'Pollinations', 'Resend', 'Netcup', 'Google'].forEach((encargado) =>
      expect(texto).toContain(encargado),
    );
    expect(texto).toContain('Estados Unidos');
  });
});
