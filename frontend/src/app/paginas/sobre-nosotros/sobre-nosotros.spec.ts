import { TestBed } from '@angular/core/testing';

import { SOBRE_NOSOTROS, VALORES } from '../../../contenido/sobre-nosotros';
import { SobreNosotrosPage } from './sobre-nosotros';

describe('SobreNosotrosPage', () => {
  it('muestra la historia, el perfil del fundador y la forma de trabajar', async () => {
    const fixture = TestBed.createComponent(SobreNosotrosPage);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain(SOBRE_NOSOTROS.historia);
    expect(texto).toContain(SOBRE_NOSOTROS.fundador.perfil);
    expect(texto).toContain(SOBRE_NOSOTROS.formaDeTrabajar);
  });

  it('enlaza al LinkedIn del fundador', async () => {
    const fixture = TestBed.createComponent(SobreNosotrosPage);
    await fixture.whenStable();

    const enlace = fixture.nativeElement.querySelector(
      `a[href="${SOBRE_NOSOTROS.fundador.linkedinUrl}"]`,
    ) as HTMLAnchorElement;
    expect(enlace).toBeTruthy();
    expect(enlace.target).toBe('_blank');
  });

  it('muestra todos los valores de la empresa', async () => {
    const fixture = TestBed.createComponent(SobreNosotrosPage);
    await fixture.whenStable();

    const texto = fixture.nativeElement.textContent;
    VALORES.forEach((valor) => expect(texto).toContain(valor.titulo));
  });
});
