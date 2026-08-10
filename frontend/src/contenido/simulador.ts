/**
 * Textos del simulador "un chatbot para tu negocio" (F10b, HU-40).
 * Fuente: docs/08 §Centro de herramientas + microcopy de la segunda
 * entrega del prototipo (decisión 15 de docs/10).
 */
export const SIMULADOR = {
  titulo: 'Un chatbot para tu negocio',
  intro:
    'Escribe el nombre y el rubro de tu empresa y conversa con el bot que podrías tener atendiendo a tus clientes.',
  etiquetaNombre: 'Nombre del negocio',
  placeholderNombre: 'Ej. Ferretería La 16',
  etiquetaRubro: 'Rubro',
  placeholderRubro: 'Ej. ferretería, restaurante, clínica',
  sufijoTituloChat: ' · demo',
  tituloChatVacio: 'Tu negocio · demo',
  mensajeInicial:
    '¡Hola! Escribe arriba el nombre y el rubro de tu negocio y pregúntame lo que preguntaría un cliente.',
  avisoNegocioIncompleto: 'Escribe el nombre y el rubro de tu negocio para empezar.',
  placeholderMensaje: 'Escribe un mensaje como si fueras tu cliente…',
  botonEnviar: 'Enviar',
  escribiendo: 'El chatbot está escribiendo…',
  limiteAnonimo:
    'Ya usaste tus mensajes de hoy. Con cuenta gratis son muchos más al día — los límites existen para que la herramienta siga siendo gratis para todos.',
  ctaCrearCuenta: 'Crear mi cuenta',
  limiteRegistrado: 'Ya usaste tus mensajes de hoy. Vuelve mañana y seguimos la conversación.',
  noDisponible:
    'El asistente está descansando. No perdiste ningún mensaje: tu conversación sigue completa aquí arriba y este intento no te descuenta mensajes.',
  notaDemo:
    'El demo usa respuestas de ejemplo. Un chatbot real se entrena con tu catálogo, horarios y forma de atender.',
  notaIa: 'Te responde una IA, no una persona.',
} as const;
