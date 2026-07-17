import { Servicio } from './tipos';

export const SERVICIOS: readonly Servicio[] = [
  {
    slug: 'desarrollo-a-la-medida',
    nombre: 'Desarrollo a la medida',
    resumenCorto:
      'Tu negocio tiene procesos únicos. Te construimos el software que los resuelve, sin pagar de más por funciones que nunca vas a usar.',
    problema:
      'Los sistemas genéricos casi nunca calzan con la forma en que realmente trabaja tu negocio. Terminas adaptando tu operación al software, en vez de que el software se adapte a ti — y pagando licencias por funciones que jamás usas.',
    incluye: [
      'Análisis de tu proceso actual, sin tecnicismos, en tu idioma.',
      'Diseño de la solución pensada para cómo trabaja tu equipo hoy.',
      'Desarrollo con buenas prácticas de la industria (el mismo estándar que usamos en este propio sitio).',
      'Acompañamiento durante la puesta en marcha, no solo la entrega.',
    ],
    proceso: [
      { titulo: 'Conversamos', descripcion: 'Entendemos tu negocio y tu problema real, sin compromiso.' },
      {
        titulo: 'Documentamos',
        descripcion: 'Dejamos claro qué se va a construir y por qué, antes de escribir una sola línea de código.',
      },
      {
        titulo: 'Construimos y probamos',
        descripcion: 'Desarrollo con pruebas desde el primer día, para que lo que se entrega funcione de verdad.',
      },
      {
        titulo: 'Acompañamos',
        descripcion: 'Te enseñamos a usarlo y quedamos disponibles para ajustes y soporte.',
      },
    ],
    entregables: [
      'Software funcionando, adaptado a tu proceso.',
      'Documentación clara de cómo usarlo (para ti y para tu equipo).',
      'Código de calidad profesional, para que el software crezca con tu negocio sin tener que rehacerlo desde cero.',
    ],
    faq: [
      {
        pregunta: '¿Necesito saber de tecnología para trabajar con ustedes?',
        respuesta: 'No. Hablamos en el idioma de tu negocio, no en el de la técnica.',
      },
      {
        pregunta: '¿Cuánto se demora un proyecto?',
        respuesta:
          'Depende del alcance; te damos un tiempo estimado claro después de la primera conversación, sin compromiso.',
      },
      {
        pregunta: '¿Qué pasa si necesito cambios después de la entrega?',
        respuesta: 'Quedamos disponibles para ajustes y evolución del software; lo conversamos según lo que necesites.',
      },
    ],
    mensajeWhatsapp: 'Hola, quiero saber más sobre desarrollo de software a la medida para mi negocio.',
  },
  {
    slug: 'ia-y-automatizacion',
    nombre: 'IA y automatización para pymes',
    resumenCorto:
      'Atiende por WhatsApp, genera cotizaciones y arma tus reportes casi solos. La inteligencia artificial trabajando para ti, sin que tengas que entender cómo funciona por dentro.',
    problema:
      'Responder WhatsApp, armar cotizaciones, conciliar cuentas y sacar reportes te quita horas todas las semanas — horas que podrías usar en hacer crecer tu negocio, no en tareas repetitivas.',
    incluye: [
      'Automatización de atención por WhatsApp para tu negocio.',
      'Generación automática de cotizaciones.',
      'Conciliaciones y reportes que antes tomaban horas, ahora en minutos.',
      'Implementación con inteligencia artificial de última generación, sin que tengas que entender cómo funciona por dentro.',
    ],
    proceso: [
      { titulo: 'Identificamos', descripcion: 'Las tareas repetitivas que más tiempo te quitan.' },
      { titulo: 'Diseñamos', descripcion: 'La automatización pensada para tu forma de operar.' },
      {
        titulo: 'Implementamos y probamos',
        descripcion: 'Con casos reales de tu negocio antes de activarla del todo.',
      },
      { titulo: 'Ajustamos', descripcion: 'Contigo hasta que funcione exactamente como lo necesitas.' },
    ],
    entregables: [
      'Automatización funcionando sobre tus canales reales (WhatsApp, correo, hojas de cálculo, según el caso).',
      'Capacitación breve para tu equipo sobre cómo supervisarla.',
      'Reportes claros de lo que la automatización está haciendo por ti.',
    ],
    faq: [
      {
        pregunta: '¿Esto reemplaza a mi equipo?',
        respuesta:
          'No. Libera tiempo de tu equipo de las tareas repetitivas para que se enfoque en lo que realmente necesita criterio humano.',
      },
      {
        pregunta: '¿Es complicado de mantener?',
        respuesta: 'No para ti — nosotros nos encargamos de la parte técnica; tú solo ves los resultados.',
      },
      {
        pregunta: '¿Funciona con WhatsApp normal o necesito algo especial?',
        respuesta: 'Se implementa sobre los canales que ya usas; te explicamos qué se necesita en la primera conversación.',
      },
    ],
    mensajeWhatsapp:
      'Hola, quiero saber más sobre cómo automatizar la atención y los procesos de mi negocio con inteligencia artificial.',
  },
  {
    slug: 'soluciones-tecnologicas',
    nombre: 'Soluciones tecnológicas',
    resumenCorto:
      'Cobro digital, integraciones de pago y modernización de sistemas. Ponemos tu negocio al día sin proyectos eternos ni sorpresas.',
    problema:
      'Cobrar digitalmente, integrar pasarelas de pago o modernizar un sistema viejo suena a proyecto largo, caro y riesgoso — y muchas veces ni siquiera sabes por dónde empezar.',
    incluye: [
      'Puesta en marcha de cobro digital para tu negocio.',
      'Integraciones de pago con las plataformas que ya usas o necesitas.',
      'Modernización de sistemas existentes sin parar tu operación.',
      'Consultoría de arquitectura si tu empresa ya tiene un área de TI y necesita refuerzo especializado puntual.',
    ],
    proceso: [
      { titulo: 'Diagnóstico', descripcion: 'Revisamos qué tienes hoy y qué necesitas lograr.' },
      {
        titulo: 'Propuesta clara',
        descripcion: 'Te mostramos el camino más simple y seguro para llegar ahí, sin vueltas innecesarias.',
      },
      {
        titulo: 'Implementación acompañada',
        descripcion: 'Ejecutamos con pruebas y verificación en cada paso, para que no haya sorpresas.',
      },
    ],
    entregables: [
      'Sistema de cobro/integración funcionando y probado.',
      'Documentación de la solución implementada.',
      'Recomendaciones claras si hay pasos futuros de modernización.',
    ],
    faq: [
      {
        pregunta: '¿Puedo empezar solo con el cobro digital y modernizar después?',
        respuesta: 'Sí, cada solución se puede abordar por separado según tu prioridad.',
      },
      {
        pregunta: '¿Trabajan con empresas que ya tienen su propio equipo de TI?',
        respuesta: 'Sí, es nuestro segundo tipo de cliente: reforzamos donde tu equipo necesita experiencia puntual, sin reemplazarlo.',
      },
      {
        pregunta: '¿Es seguro integrar pagos con ustedes?',
        respuesta: 'Trabajamos con las prácticas de seguridad estándar de la industria para el manejo de pagos e integraciones.',
      },
    ],
    mensajeWhatsapp: 'Hola, quiero saber más sobre soluciones tecnológicas y cobro digital para mi negocio.',
  },
] as const;
