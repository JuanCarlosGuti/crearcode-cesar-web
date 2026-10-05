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
  // Editable desde ISS-226 (decisión 31 de docs/10): los ejemplos que
  // antes eran fijos pasan a ser el texto de ayuda. Los máximos son los
  // del formulario del demo en /herramientas.
  demo: {
    titulo: 'Demo de diseño con IA',
    badge: 'Gratis · para cuentas',
    campos: [
      { id: 'sector', etiqueta: 'Sector', ejemplo: 'Ej. restaurante', maximo: 60 },
      { id: 'queHace', etiqueta: 'Qué hace', ejemplo: 'Ej. domicilios en Valledupar', maximo: 300 },
      {
        id: 'queNecesita',
        etiqueta: 'Qué necesita',
        ejemplo: 'Ej. recibir pedidos sin saturar el WhatsApp',
        maximo: 300,
      },
    ],
    cta: 'Ver mi boceto con IA',
    notaPrivacidad: 'Lo que escribes queda solo en tu navegador hasta que generes tu boceto.',
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
  proyectos: {
    etiqueta: 'Proyectos propios',
    titulo: 'Lo que hemos construido',
    texto:
      'Todavía no mostramos trabajos de clientes: cuando los haya, irán con su nombre y su permiso. Mientras tanto enseñamos lo que sí se puede abrir y revisar — un directorio de negocios en producción, una plataforma de alojamientos, una pasarela de pagos cripto en desarrollo y una app para estudiar la lengua del pueblo Wiwa.',
    enlace: 'Ver los proyectos',
  },
  fundador: {
    etiqueta: 'Quién está detrás',
    titulo: 'Juan Carlos Gutiérrez',
    texto:
      'Administrador de empresas e ingeniero de software backend, con siete años como instructor del SENA. Esa mezcla es la razón por la que aquí se habla de plazos, costos y riesgos, y no solo de tecnología.',
    enlace: 'Cómo trabajamos',
  },
  cierre: {
    titulo: 'Cuéntanos qué te está quitando tiempo',
    texto:
      'Media hora por videollamada, sin costo y sin venta forzada. Salimos con un diagnóstico claro, así no trabajemos juntos.',
  },
} as const;
