import { TestBed } from '@angular/core/testing';

import { DEMO_DISENO } from '../../../contenido/demo-diseno';
import { IdentidadDeMarca as Identidad } from '../../api/demo-api';
import { IdentidadDeMarca } from './identidad-de-marca';

const BARBERIA: Identidad = {
  nombre: 'Barbería El Patrón',
  eslogan: 'Estilo que se nota',
  paleta: 'NOCTURNA',
  logo: { forma: 'ESCUDO', simbolo: '💈', letra: 'CLASICA' },
};

async function pintar(identidad: Identidad) {
  const fixture = TestBed.createComponent(IdentidadDeMarca);
  fixture.componentRef.setInput('identidad', identidad);
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

describe('IdentidadDeMarca (ISS-238, decisión 35)', () => {
  it('muestra el logo, el nombre, el eslogan, los colores y la letra de la marca', async () => {
    const el = await pintar(BARBERIA);

    expect(el.textContent).toContain(DEMO_DISENO.identidad.titulo);
    expect(el.querySelector('.identidad__nombre')?.textContent).toContain('Barbería El Patrón');
    expect(el.textContent).toContain('Estilo que se nota');
    expect(el.textContent).toContain('Nocturna: negro y dorado');
    expect(el.textContent).toContain('Letra clásica');
    const logo = el.querySelector('app-logo-de-marca .logo')!;
    expect(logo.textContent?.trim()).toBe('💈');
    expect(logo.classList).toContain('logo--escudo');
  });

  it('el nombre se escribe con la letra elegida', async () => {
    const el = await pintar(BARBERIA);

    expect(el.querySelector<HTMLElement>('.identidad__nombre')!.style.fontFamily).toContain('Georgia');
  });

  it('sin símbolo, el logo usa la inicial del nombre', async () => {
    const el = await pintar({ ...BARBERIA, logo: { ...BARBERIA.logo, simbolo: '' } });

    expect(el.querySelector('app-logo-de-marca .logo')?.textContent?.trim()).toBe('B');
  });

  it('dice que es una propuesta y recuerda verificar el nombre', async () => {
    const el = await pintar(BARBERIA);

    expect(el.querySelector('.identidad__nota')?.textContent).toContain('Propuesta de referencia');
    expect(el.querySelector('.identidad__nota')?.textContent).toContain('no esté registrado');
  });

  it('una paleta que no conoce toma la de la casa', async () => {
    const el = await pintar({ ...BARBERIA, paleta: 'ARCOIRIS' as Identidad['paleta'] });

    expect(el.textContent).toContain('Fresca: menta y verde oscuro');
  });

  it('el logo es decorativo: el nombre ya está escrito al lado', async () => {
    const el = await pintar(BARBERIA);

    expect(el.querySelector('app-logo-de-marca .logo')?.getAttribute('aria-hidden')).toBe('true');
  });
});
