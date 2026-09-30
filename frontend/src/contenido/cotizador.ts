/**
 * Datos del cotizador interactivo (fase F10a, HU-39).
 * Fuente: docs/08 §Cotizador + prototipo aprobado (docs/10, decisión 10).
 *
 * Rangos validados contra el mercado colombiano 2026 y APROBADOS por
 * el usuario el 10 ago 2026 (docs/08 §Cotizador).
 */

export interface PasoDeCotizador {
  readonly clave: 'tipo' | 'alcance' | 'urgencia';
  readonly titulo: string;
  readonly ayuda: string;
  readonly opciones: readonly string[];
}

export const COTIZADOR = {
  titulo: '¿Cuánto podría costar tu proyecto?',
  intro:
    'Tres preguntas y te damos un rango orientativo. La cifra exacta sale de entender tu negocio — la primera consulta es gratis.',
  pasos: [
    {
      clave: 'tipo',
      titulo: '¿Qué tipo de proyecto tienes en mente?',
      ayuda: 'Elige lo más parecido; luego lo afinamos.',
      opciones: [
        'Página web o tienda en línea',
        'Sistema interno a la medida',
        'Automatización con IA',
        'Cobro digital o modernización',
      ],
    },
    {
      clave: 'alcance',
      titulo: '¿Qué tan grande es el alcance?',
      ayuda: 'Piensa en cuántas cosas distintas debe hacer.',
      opciones: [
        'Algo puntual, una sola función',
        'Varias funciones conectadas',
        'Un sistema completo para el negocio',
      ],
    },
    {
      clave: 'urgencia',
      titulo: '¿Para cuándo lo necesitas?',
      ayuda: 'La urgencia cambia cómo se planea el trabajo.',
      opciones: ['Sin afán, en los próximos meses', 'En 4 a 8 semanas', 'Lo antes posible'],
    },
  ] as readonly PasoDeCotizador[],
  /**
   * Rango por TIPO de proyecto y alcance.
   *
   * Antes solo dependia del alcance, asi que las 36 combinaciones del
   * wizard daban 3 resultados y una "pagina web" arrancaba en el mismo
   * piso de COP 4 millones que un sistema a la medida: espantaba justo
   * al cliente mas facil de atender (auditoria del 28 sep 2026,
   * P1-2a).
   *
   * Los anclajes por tipo salen de la investigacion de mercado ya
   * documentada en docs/08 (web corporativa pyme 3,5-8 M, sistemas a
   * medida 8-80 M+, apps 35-80 M); el reparto por alcance dentro de
   * cada tipo es una interpolacion razonable, no un dato medido.
   *
   * Validadas por el dueno tal como estan (30 sep 2026). Son doce
   * numeros y estan todos aqui: cambiarlos no toca ni una linea de
   * codigo.
   */
  rangosPorTipoYAlcance: {
    'Página web o tienda en línea': {
      'Algo puntual, una sola función': 'COP 3,5 – 7 millones',
      'Varias funciones conectadas': 'COP 7 – 15 millones',
      'Un sistema completo para el negocio': 'COP 15 – 35 millones',
    },
    'Sistema interno a la medida': {
      'Algo puntual, una sola función': 'COP 8 – 18 millones',
      'Varias funciones conectadas': 'COP 18 – 40 millones',
      'Un sistema completo para el negocio': 'COP 40 – 80 millones',
    },
    'Automatización con IA': {
      'Algo puntual, una sola función': 'COP 5 – 12 millones',
      'Varias funciones conectadas': 'COP 12 – 30 millones',
      'Un sistema completo para el negocio': 'COP 30 – 70 millones',
    },
    'Cobro digital o modernización': {
      'Algo puntual, una sola función': 'COP 4 – 10 millones',
      'Varias funciones conectadas': 'COP 10 – 25 millones',
      'Un sistema completo para el negocio': 'COP 25 – 60 millones',
    },
  } as Record<string, Record<string, string>>,
  /**
   * La urgencia no mueve el rango: mueve como se planea el trabajo, y
   * decirlo en una frase es mas honesto que inventarle un porcentaje
   * al cliente antes de conocer el proyecto.
   * Decision del dueno (30 sep 2026): sin recargo fijo por urgencia; se
   * avisa y se habla en la consulta.
   */
  notaPorUrgencia: {
    'Lo antes posible':
      'Trabajar contra reloj normalmente sube el costo: hay que reservar el equipo y dejar otras cosas en pausa. Lo hablamos en la primera consulta.',
    'En 4 a 8 semanas': '',
    'Sin afán, en los próximos meses':
      'Con margen de tiempo se puede planear por fases y arrancar con lo que más te urge.',
  } as Record<string, string>,
  resultado: {
    etiqueta: 'Rango orientativo para tu caso',
    aclaracion:
      'Cada proyecto se cotiza a la medida: este rango es solo una referencia para que sepas en qué orden de magnitud te mueves. El alcance final lo definimos juntos.',
  },
  ctaContacto: 'Agenda tu consulta gratuita',
  // Explicita lo que hace el boton: el mensaje ya lleva el resumen
  // y el rango que vio el visitante (auditoria P1-2c).
  ctaWhatsapp: 'Envíame esta estimación por WhatsApp',
  atras: '← Atrás',
  reiniciar: 'Empezar de nuevo',
} as const;
