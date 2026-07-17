import { TestBed } from '@angular/core/testing';

import { Faq } from './faq';

describe('Faq', () => {
  const preguntas = [
    { pregunta: '¿Pregunta uno?', respuesta: 'Respuesta uno' },
    { pregunta: '¿Pregunta dos?', respuesta: 'Respuesta dos' },
  ];

  it('empieza con todas las respuestas colapsadas', async () => {
    const fixture = TestBed.createComponent(Faq);
    fixture.componentRef.setInput('preguntas', preguntas);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.faq__respuesta')).toBeNull();
    const botones = fixture.nativeElement.querySelectorAll('.faq__pregunta');
    botones.forEach((boton: HTMLButtonElement) => expect(boton.getAttribute('aria-expanded')).toBe('false'));
  });

  it('expande la respuesta al hacer click en la pregunta', async () => {
    const fixture = TestBed.createComponent(Faq);
    fixture.componentRef.setInput('preguntas', preguntas);
    await fixture.whenStable();

    const primerBoton = fixture.nativeElement.querySelectorAll('.faq__pregunta')[0] as HTMLButtonElement;
    primerBoton.click();
    await fixture.whenStable();

    expect(primerBoton.getAttribute('aria-expanded')).toBe('true');
    expect(fixture.nativeElement.textContent).toContain('Respuesta uno');
  });

  it('colapsa la respuesta al hacer click de nuevo', async () => {
    const fixture = TestBed.createComponent(Faq);
    fixture.componentRef.setInput('preguntas', preguntas);
    await fixture.whenStable();

    const primerBoton = fixture.nativeElement.querySelectorAll('.faq__pregunta')[0] as HTMLButtonElement;
    primerBoton.click();
    await fixture.whenStable();
    primerBoton.click();
    await fixture.whenStable();

    expect(primerBoton.getAttribute('aria-expanded')).toBe('false');
  });
});
