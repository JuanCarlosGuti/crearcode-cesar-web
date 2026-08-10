/**
 * Contenido de la Home rediseñada (F10e, ISS-133 — prototipo aprobado,
 * decisiones 10-15 de docs/10). El headline sigue siendo el eslogan
 * vigente: su revisión es una decisión PENDIENTE del usuario.
 */
export const HOME = {
  headline: 'Tecnología que trabaja para tu negocio, no al revés.',
  subheadline:
    'Desarrollamos software a la medida, ponemos la inteligencia artificial a trabajar por tu pyme y hacemos que cobrar y modernizar tu negocio sea simple. Sin jerga. Sin que la tecnología te quede grande.',
  gancho:
    'No te contamos lo que la tecnología puede hacer por ti: te lo mostramos aquí mismo, en dos minutos y sin compromiso.',
  mensajeWhatsapp: 'Hola, vengo del sitio web de Crear Code Cesar y quiero saber más sobre cómo pueden ayudarme con mi negocio.',
  demo: {
    titulo: 'Demo de diseño con IA',
    badge: 'Gratis · para cuentas',
    ejemplos: [
      { etiqueta: 'Sector', valor: 'Restaurante' },
      { etiqueta: 'Qué hace', valor: 'Domicilios en Valledupar' },
      { etiqueta: 'Qué necesita', valor: 'Recibir pedidos sin saturar el WhatsApp' },
    ],
    cta: 'Ver mi boceto con IA',
    nota: 'Genera una imagen de referencia, no un producto final.',
  },
  herramientas: {
    titulo: 'Pruébalo con tu propio negocio, ahora mismo',
    intro:
      'Herramientas abiertas, sin registro para empezar. Si creas tu cuenta gratis, tienes más usos cada día y acceso anticipado a lo nuevo.',
    cta: 'Abrir el centro de herramientas',
    tarjetas: [
      {
        titulo: 'Asistente con IA',
        descripcion:
          'Respuestas al instante sobre servicios, plazos y forma de trabajo — en la burbuja de abajo a la derecha.',
        nota: 'Sin registro',
      },
      {
        titulo: 'Cotizador de proyectos',
        descripcion: 'Tres preguntas y un rango orientativo para tu proyecto, sin compromiso.',
        nota: 'Sin registro · ilimitado',
      },
      {
        titulo: 'Chatbot para tu negocio',
        descripcion: 'Conversa con el bot que tu empresa podría tener atendiendo a tus clientes.',
        nota: 'Sin registro · 10 mensajes al día',
      },
      {
        titulo: 'Diagnóstico digital',
        descripcion: 'Seis preguntas y una radiografía con tres oportunidades de automatización.',
        nota: 'Sin registro · 2 al día',
      },
    ],
  },
  asistente: {
    titulo: 'Pregúntale a nuestro asistente antes de escribirnos',
    texto:
      'Está en la burbuja de abajo a la derecha, en todas las páginas. Responde en segundos sobre plazos, forma de trabajo y qué se puede automatizar en tu rubro.',
    invitacion: 'Prueba con una de estas:',
  },
  placeholders: {
    etiqueta: 'Espacio reservado',
    casos: {
      titulo: 'Casos reales de clientes',
      texto:
        'Este espacio queda reservado para proyectos verificables, con nombre y resultado autorizados por cada cliente. No publicamos cifras ni testimonios que no podamos sustentar.',
    },
    equipo: {
      titulo: 'El equipo',
      texto:
        'Fotos y roles del equipo, con material real. Mientras llega, preferimos este aviso a inventar perfiles.',
    },
  },
  cierre: {
    titulo: 'Cuéntanos qué te está quitando tiempo',
    texto:
      'Media hora por videollamada, sin costo y sin venta forzada. Salimos con un diagnóstico claro, así no trabajemos juntos.',
  },
} as const;
