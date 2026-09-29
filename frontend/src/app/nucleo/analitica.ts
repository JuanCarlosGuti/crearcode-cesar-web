import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { InjectionToken, Injectable, PLATFORM_ID, computed, inject, isDevMode, signal } from '@angular/core';

import { ANALITICA, CLAVE_CONSENTIMIENTO } from '../../contenido/analitica';

export type DecisionDeConsentimiento = 'sin-responder' | 'concedido' | 'rechazado';

export interface ConfiguracionAnalitica {
  readonly ga4: string;
  readonly clarity: string;
  /** Falso en desarrollo: medir el trabajo propio ensucia los datos. */
  readonly habilitada: boolean;
}

/**
 * La configuración pasa por un token y no se lee de la constante
 * directamente para que las pruebas puedan sustituirla — con la
 * constante importada no habría forma de probar el camino en que sí
 * hay ids.
 */
export const CONFIGURACION_ANALITICA = new InjectionToken<ConfiguracionAnalitica>('configuracion-analitica', {
  providedIn: 'root',
  factory: () => ({ ga4: ANALITICA.ga4, clarity: ANALITICA.clarity, habilitada: !isDevMode() }),
});

type PropiedadesDeEvento = Record<string, string | number | boolean>;

interface VentanaConMedicion extends Window {
  dataLayer?: unknown[];
  gtag?: (...argumentos: unknown[]) => void;
}

/**
 * Medición del sitio, detrás del consentimiento del visitante.
 *
 * <p>
 * Hasta la auditoría del 28 sep 2026 no se medía absolutamente nada:
 * no había forma de saber si alguien llegaba a las herramientas, si el
 * formulario se abandonaba a medias o por dónde entraban los visitantes
 * — así que tampoco de saber qué valía la pena arreglar.
 *
 * <p>
 * Tres reglas que no son negociables aquí:
 * <ul>
 * <li>No se carga <b>nada</b> antes de que el visitante acepte. Ni el
 * script, ni una petición, ni una cookie: el consentimiento previo es
 * lo que exige la Ley 1581, y además es lo que dice la política de
 * datos del sitio.</li>
 * <li>No se mide en desarrollo. Las visitas propias mientras se
 * construye el sitio son ruido que luego no se puede separar.</li>
 * <li>Nunca se envían datos personales en un evento — ni correo, ni
 * teléfono, ni el texto que alguien escribió en una herramienta. Los
 * eventos dicen qué pasó, no quién.</li>
 * </ul>
 */
@Injectable({ providedIn: 'root' })
export class Analitica {
  private readonly configuracion = inject(CONFIGURACION_ANALITICA);
  private readonly documento = inject(DOCUMENT);
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly decision = signal<DecisionDeConsentimiento>(this.decisionGuardada());
  private cargada = false;

  /** Hay algo que medir y algo que preguntar. */
  readonly configurada = computed(
    () => this.configuracion.habilitada && (this.configuracion.ga4 !== '' || this.configuracion.clarity !== ''),
  );

  readonly decisionDelVisitante = this.decision.asReadonly();

  /** El banner solo aparece si hay medición configurada y no se ha respondido. */
  readonly debePreguntar = computed(() => this.configurada() && this.decision() === 'sin-responder');

  constructor() {
    if (this.decision() === 'concedido') {
      this.cargarSiHaceFalta();
    }
  }

  conceder(): void {
    this.guardarDecision('concedido');
    this.cargarSiHaceFalta();
  }

  rechazar(): void {
    this.guardarDecision('rechazado');
  }

  /**
   * Registra un evento. Si no hay consentimiento, no hace nada y no
   * guarda nada para después: un evento retenido a la espera de un
   * permiso que quizá no llegue es exactamente lo que el permiso
   * intenta evitar.
   */
  registrar(evento: string, propiedades: PropiedadesDeEvento = {}): void {
    if (!this.cargada) {
      return;
    }
    this.ventana()?.gtag?.('event', evento, propiedades);
  }

  /** Vista de página en navegación SPA: sin ella solo se mide la primera. */
  paginaVista(ruta: string): void {
    if (!this.cargada || this.configuracion.ga4 === '') {
      return;
    }
    this.ventana()?.gtag?.('event', 'page_view', { page_path: ruta });
  }

  private cargarSiHaceFalta(): void {
    if (this.cargada || !this.esNavegador || !this.configurada()) {
      return;
    }
    this.cargada = true;
    if (this.configuracion.ga4 !== '') {
      this.cargarGa4(this.configuracion.ga4);
    }
    if (this.configuracion.clarity !== '') {
      this.cargarClarity(this.configuracion.clarity);
    }
  }

  private cargarGa4(id: string): void {
    const ventana = this.ventana();
    if (!ventana) {
      return;
    }
    ventana.dataLayer = ventana.dataLayer ?? [];
    // gtag empuja `arguments` tal cual: con una función flecha y rest
    // el objeto que llega a GA4 no es el mismo y la librería lo ignora.
    ventana.gtag = function gtag() {
      // eslint-disable-next-line prefer-rest-params
      ventana.dataLayer?.push(arguments);
    };
    ventana.gtag('js', new Date());
    // La IP no se guarda completa y no se comparte con otros productos
    // de Google: es lo que la política de datos promete.
    ventana.gtag('config', id, { anonymize_ip: true, allow_google_signals: false });

    // async y al final: la medición nunca puede retrasar lo que el
    // visitante vino a leer.
    const script = this.documento.createElement('script');
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtag/js?id=${id}`;
    this.documento.head.appendChild(script);
  }

  private cargarClarity(id: string): void {
    const script = this.documento.createElement('script');
    script.async = true;
    script.src = `https://www.clarity.ms/tag/${id}`;
    this.documento.head.appendChild(script);
  }

  private ventana(): VentanaConMedicion | null {
    return this.esNavegador ? (this.documento.defaultView as VentanaConMedicion | null) : null;
  }

  private decisionGuardada(): DecisionDeConsentimiento {
    if (!isPlatformBrowser(inject(PLATFORM_ID))) {
      return 'sin-responder';
    }
    try {
      const guardada = localStorage.getItem(CLAVE_CONSENTIMIENTO);
      return guardada === 'concedido' || guardada === 'rechazado' ? guardada : 'sin-responder';
    } catch {
      // Navegación privada o almacenamiento bloqueado: se vuelve a
      // preguntar, que es el lado seguro.
      return 'sin-responder';
    }
  }

  private guardarDecision(decision: DecisionDeConsentimiento): void {
    this.decision.set(decision);
    if (!this.esNavegador) {
      return;
    }
    try {
      localStorage.setItem(CLAVE_CONSENTIMIENTO, decision);
    } catch {
      // Si no se puede recordar, se volverá a preguntar en la próxima
      // visita. Molesto, pero no roto.
    }
  }
}
