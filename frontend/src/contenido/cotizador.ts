/**
 * Datos del cotizador interactivo (fase F10a, HU-39).
 * Fuente: docs/08 §Cotizador + prototipo aprobado (docs/10, decisión 10).
 *
 * Los rangos son la PROPUESTA del prototipo — el usuario debe
 * aprobarlos antes de publicar F10a (docs/10, decisión 13).
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
      ayuda: 'La urgencia cambia el equipo asignado.',
      opciones: ['Sin afán, en los próximos meses', 'En 4 a 8 semanas', 'Lo antes posible'],
    },
  ] as readonly PasoDeCotizador[],
  rangosPorAlcance: {
    'Algo puntual, una sola función': 'COP 4 – 9 millones',
    'Varias funciones conectadas': 'COP 9 – 22 millones',
    'Un sistema completo para el negocio': 'COP 22 – 60 millones',
  } as Record<string, string>,
  resultado: {
    etiqueta: 'Rango orientativo para tu caso',
    aclaracion:
      'Cada proyecto se cotiza a la medida: este rango es solo una referencia para que sepas en qué orden de magnitud te mueves. El alcance final lo definimos juntos.',
  },
  ctaContacto: 'Agenda tu consulta gratuita',
  ctaWhatsapp: 'WhatsApp',
  reiniciar: 'Empezar de nuevo',
} as const;
