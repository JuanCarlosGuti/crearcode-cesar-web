import { TestBed } from '@angular/core/testing';

import { CLAVE_CONSENTIMIENTO } from '../../contenido/analitica';
import { Analitica, CONFIGURACION_ANALITICA, ConfiguracionAnalitica } from './analitica';

const CONFIGURADA: ConfiguracionAnalitica = { ga4: 'G-DEPRUEBA', clarity: 'clarity-prueba', habilitada: true };

function crear(configuracion: ConfiguracionAnalitica = CONFIGURADA): Analitica {
  TestBed.configureTestingModule({
    providers: [{ provide: CONFIGURACION_ANALITICA, useValue: configuracion }],
  });
  return TestBed.inject(Analitica);
}

function scriptsDeMedicion(): HTMLScriptElement[] {
  return Array.from(document.head.querySelectorAll('script')).filter(
    (script) => script.src.includes('googletagmanager') || script.src.includes('clarity'),
  );
}

describe('Analitica', () => {
  beforeEach(() => {
    localStorage.removeItem(CLAVE_CONSENTIMIENTO);
    scriptsDeMedicion().forEach((script) => script.remove());
    TestBed.resetTestingModule();
  });

  /**
   * La regla dura: ni el script, ni una peticion, ni una cookie antes
   * de que el visitante acepte. Es lo que exige el consentimiento
   * previo y lo que promete la politica de datos del sitio.
   */
  it('no carga absolutamente nada mientras el visitante no responda', () => {
    const analitica = crear();

    expect(analitica.debePreguntar()).toBe(true);
    expect(scriptsDeMedicion()).toHaveLength(0);
  });

  it('no carga nada si el visitante rechaza', () => {
    const analitica = crear();

    analitica.rechazar();

    expect(scriptsDeMedicion()).toHaveLength(0);
    expect(analitica.debePreguntar()).toBe(false);
  });

  it('carga las dos herramientas cuando el visitante acepta', () => {
    const analitica = crear();

    analitica.conceder();

    const fuentes = scriptsDeMedicion().map((script) => script.src);
    expect(fuentes.some((fuente) => fuente.includes('G-DEPRUEBA'))).toBe(true);
    expect(fuentes.some((fuente) => fuente.includes('clarity-prueba'))).toBe(true);
  });

  it('los scripts van con async para no retrasar lo que el visitante vino a leer', () => {
    crear().conceder();

    expect(scriptsDeMedicion().every((script) => script.async)).toBe(true);
  });

  it('no vuelve a cargar los scripts si se concede dos veces', () => {
    const analitica = crear();

    analitica.conceder();
    analitica.conceder();

    expect(scriptsDeMedicion()).toHaveLength(2);
  });

  it('recuerda la decision entre visitas y no vuelve a preguntar', () => {
    crear().conceder();
    TestBed.resetTestingModule();

    const enLaSiguienteVisita = crear();

    expect(enLaSiguienteVisita.debePreguntar()).toBe(false);
    expect(enLaSiguienteVisita.decisionDelVisitante()).toBe('concedido');
  });

  /**
   * Medir las visitas propias mientras se construye el sitio es ruido
   * que despues no se puede separar de los visitantes de verdad.
   */
  it('en desarrollo no mide ni pregunta', () => {
    const analitica = crear({ ...CONFIGURADA, habilitada: false });

    analitica.conceder();

    expect(analitica.debePreguntar()).toBe(false);
    expect(scriptsDeMedicion()).toHaveLength(0);
  });

  /**
   * Es el estado en que queda el repositorio: los ids los pone el
   * dueno. Sin ellos no hay nada que cargar, asi que tampoco tiene
   * sentido pedirle permiso a nadie.
   */
  it('sin ids configurados no pregunta ni carga', () => {
    const analitica = crear({ ga4: '', clarity: '', habilitada: true });

    expect(analitica.configurada()).toBe(false);
    expect(analitica.debePreguntar()).toBe(false);
    analitica.conceder();
    expect(scriptsDeMedicion()).toHaveLength(0);
  });

  it('un evento anterior al consentimiento no se envia ni se guarda para despues', () => {
    const analitica = crear();
    const enviados: unknown[] = [];

    analitica.registrar('whatsapp_click', { origen: 'footer' });
    analitica.conceder();
    (window as unknown as { gtag: (...a: unknown[]) => void }).gtag = (...argumentos) =>
      enviados.push(argumentos);
    analitica.registrar('whatsapp_click', { origen: 'footer' });

    expect(enviados).toHaveLength(1);
  });
});
