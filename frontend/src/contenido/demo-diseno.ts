/**
 * Textos del demo de diseño con IA (F10d, HU-42).
 * Microcopy del prototipo aprobado (decisiones 10 y 15 de docs/10).
 */
export const DEMO_DISENO = {
  titulo: 'Demo de diseño con IA',
  intro:
    'Describe tu negocio y recibe un boceto visual de tu futura app o web, con funcionalidades sugeridas.',
  bloqueado: {
    titulo: 'Crea tu cuenta gratis para ver tu boceto',
    texto:
      'Generar un diseño con IA cuesta procesamiento, así que lo reservamos para cuentas. Toma 30 segundos y no pedimos tarjeta.',
    ctaRegistro: 'Crear cuenta gratis',
    ctaIngreso: 'Ya tengo cuenta, iniciar sesión',
  },
  formulario: {
    etiquetaSector: '¿A qué se dedica tu negocio?',
    placeholderSector: 'Ej. restaurante, ferretería, clínica',
    etiquetaQueHace: '¿Qué hace día a día?',
    placeholderQueHace: 'Ej. vendemos almuerzos y domicilios en Valledupar',
    etiquetaQueNecesita: '¿Qué necesitas resolver?',
    placeholderQueNecesita: 'Ej. que los clientes pidan sin saturar el WhatsApp',
    boton: 'Generar mi boceto',
    nota: 'Tarda unos 30 segundos. No guardamos lo que escribes.',
  },
  generando: 'Dibujando tu boceto…',
  resultado: {
    aclaracion:
      'Basado en lo que nos contaste. Es una referencia visual: el diseño final se define contigo.',
    subtituloFuncionalidades: 'Funcionalidades sugeridas',
    ctaHazloRealidad: 'Hazlo realidad',
    ctaWhatsapp: 'WhatsApp',
    ctaVariacion: 'Pedir otra variación',
    altImagen: 'Boceto generado por IA de la solución propuesta para tu negocio',
  },
  limite:
    'Usaste tus bocetos de hoy. Se reinician mañana a la medianoche — los límites existen para que la herramienta siga siendo gratis.',
  noDisponible:
    'El asistente está descansando. No perdiste ningún boceto: puedes intentarlo otra vez en un minuto.',
  reintentar: 'Intentar de nuevo',
  volverAEmpezar: 'Volver a empezar',
} as const;
