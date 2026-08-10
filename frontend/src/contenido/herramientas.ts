/**
 * Contenido de la página viva /herramientas (F10a, HU-43).
 * Fuente: docs/08 §Centro de herramientas + prototipo aprobado.
 */

export interface TarjetaDeHerramienta {
  readonly titulo: string;
  readonly descripcion: string;
  readonly nota: string;
  readonly activa: boolean;
}

export const HERRAMIENTAS = {
  titulo: 'Herramientas para tu negocio',
  intro:
    'Prueba la tecnología antes de contratarla. Todo funciona sin registrarte y es gratis; con cuenta tienes más usos cada día.',
  etiquetaMuyPronto: 'Muy pronto',
  tarjetas: [
    {
      titulo: 'Asistente con IA',
      descripcion:
        'Respuestas al instante sobre servicios, plazos y forma de trabajo. Está en la burbuja de abajo a la derecha, en todas las páginas.',
      nota: 'Gratis · sin registro',
      activa: true,
    },
    {
      titulo: 'Cotizador de proyectos',
      descripcion:
        'Tres preguntas y te damos un rango orientativo para tu proyecto, sin compromiso. Está aquí abajo.',
      nota: 'Sin registro · ilimitado',
      activa: true,
    },
    {
      titulo: 'Chatbot para tu negocio',
      descripcion:
        'Escribe el nombre y el rubro de tu empresa y conversa con el bot que podrías tener atendiendo a tus clientes. Está aquí abajo.',
      nota: 'Sin registro · 10 mensajes al día',
      activa: true,
    },
    {
      titulo: 'Diagnóstico digital',
      descripcion:
        'Seis preguntas sobre cómo opera tu negocio y una radiografía con tres oportunidades de automatización. Está aquí abajo.',
      nota: 'Sin registro · 2 al día, 10 con cuenta',
      activa: true,
    },
    {
      titulo: 'Demo de diseño con IA',
      descripcion:
        'Describe tu negocio y recibe un boceto visual de tu futura app o web, con funcionalidades sugeridas.',
      nota: 'Para cuentas gratis',
      activa: false,
    },
  ] as readonly TarjetaDeHerramienta[],
  cuenta: {
    titulo: 'Con cuenta gratis tienes más usos y acceso anticipado',
    texto:
      'Sin tarjeta y sin costos: los límites diarios existen para que las herramientas sigan siendo gratis para todos.',
    cta: 'Crea tu cuenta gratis',
  },
} as const;
