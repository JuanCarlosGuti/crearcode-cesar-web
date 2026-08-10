/**
 * Contenido del diagnóstico digital (F10c, HU-41).
 * Preguntas y microcopy del prototipo aprobado (decisión 15, docs/10).
 */

export interface PreguntaDeDiagnostico {
  readonly pregunta: string;
  readonly opciones: readonly string[];
}

export const DIAGNOSTICO = {
  titulo: 'Diagnóstico digital',
  intro:
    'Seis preguntas sobre cómo opera tu negocio y te mostramos una radiografía con tres oportunidades de automatización.',
  preguntas: [
    {
      pregunta: '¿Cómo reciben hoy los pedidos o solicitudes de sus clientes?',
      opciones: ['WhatsApp y llamadas', 'Un formulario o sitio web', 'En persona, en el mostrador'],
    },
    {
      pregunta: '¿Dónde guardan la información de clientes y ventas?',
      opciones: ['En Excel o cuadernos', 'En un software que ya usamos', 'Cada quien lleva lo suyo'],
    },
    {
      pregunta: '¿Cuánto tiempo al día se va respondiendo lo mismo?',
      opciones: ['Menos de una hora', 'Entre una y tres horas', 'Buena parte del día'],
    },
    {
      pregunta: '¿Cómo cobran hoy?',
      opciones: ['Efectivo y transferencias', 'Datáfono o link de pago', 'Facturamos y perseguimos el pago'],
    },
    {
      pregunta: '¿Hacen seguimiento a los clientes que no compraron?',
      opciones: ['Sí, de forma ordenada', 'A veces, cuando hay tiempo', 'Casi nunca'],
    },
    {
      pregunta: '¿Qué les duele más hoy?',
      opciones: [
        'Perder pedidos por demora',
        'Errores y reprocesos',
        'No saber qué está pasando en el negocio',
      ],
    },
  ] as readonly PreguntaDeDiagnostico[],
  analizando: 'Analizando tus respuestas…',
  tituloRadiografia: 'Tu radiografía digital',
  subtituloOportunidades: 'Tres oportunidades de automatización',
  notaOrden: 'Ordenadas por lo rápido que verías el cambio.',
  prefijoBeneficio: 'Beneficio: ',
  cierreTitulo: '¿Hablamos de cuál te conviene primero?',
  cierreTexto:
    '30 minutos con alguien del equipo, sin costo y sin compromiso. Si no te sirve, te lo decimos.',
  ctaContacto: 'Agenda tu consulta gratuita',
  ctaWhatsapp: 'WhatsApp',
  reiniciar: 'Volver a empezar',
  notaCorreo: 'Muy pronto: recibirlo por correo.',
  limiteAnonimo:
    'Ya usaste tus diagnósticos de hoy. Con cuenta gratis son más al día — los límites existen para que la herramienta siga siendo gratis para todos.',
  ctaCrearCuenta: 'Crear mi cuenta',
  limiteRegistrado: 'Ya usaste tus diagnósticos de hoy. Vuelve mañana y seguimos.',
  noDisponible:
    'El asistente está descansando. Este intento no te descuenta diagnósticos — vuelve a intentarlo en un momento.',
} as const;
