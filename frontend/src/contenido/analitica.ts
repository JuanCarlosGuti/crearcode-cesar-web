/**
 * Identificadores de analitica. Vacios = analitica apagada, que es
 * como esta el sitio hoy: no se mide nada (auditoria del 28 sep 2026,
 * P1-1). Para encenderla, el dueno crea la propiedad y pega el id
 * aqui.
 *
 * Son constantes y no variables de entorno por la misma razon que
 * BASE_URL (ADR-06): el sitio se hornea en el prerender, asi que lo
 * que varia tiene que estar en el codigo al construir. Y no son
 * secretos — un id de GA4 o de Clarity viaja en el HTML que recibe
 * cualquier visitante; pasarlo por un secreto del repositorio no
 * escondería nada y solo anadiria una cadena de build-args.
 */
export const ANALITICA = {
  /** Propiedad de Google Analytics 4, con formato `G-XXXXXXXXXX`. */
  ga4: '',
  /** Proyecto de Microsoft Clarity (mapas de calor y grabaciones). */
  clarity: '',
} as const;

/** Clave de la decision del visitante sobre las cookies de medicion. */
export const CLAVE_CONSENTIMIENTO = 'crearcode-consentimiento-analitica';

export const CONSENTIMIENTO = {
  titulo: 'Cookies de medición',
  texto:
    'Usamos herramientas de medición para saber qué partes del sitio sirven y cuáles no. No las cargamos hasta que aceptes, y nunca las usamos para publicidad.',
  aceptar: 'Aceptar',
  rechazar: 'Rechazar',
  enlacePolitica: 'Ver la política de datos',
} as const;
