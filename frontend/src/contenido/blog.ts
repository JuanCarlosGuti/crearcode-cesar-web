import { ArticuloBlog } from './tipos';

/**
 * Artículos de ejemplo — guían el tono editorial hasta que se publiquen
 * artículos reales (ver docs/08-contenido.md). Cada `cuerpoMarkdown` se
 * renderiza con `marked` (ver blog-articulo.page.ts).
 */
export const ARTICULOS: readonly ArticuloBlog[] = [
  {
    slug: 'senales-automatizar-atencion-whatsapp',
    titulo: '5 señales de que tu negocio ya necesita automatizar su atención por WhatsApp',
    resumen:
      'Cómo identificar el momento exacto en que responder manualmente te está costando ventas, y qué hacer al respecto.',
    fecha: '2026-06-15',
    cuerpoMarkdown: `Si administras un negocio en Colombia, lo más probable es que WhatsApp ya sea tu principal canal de ventas. El problema no es el canal — es que responder cada mensaje a mano deja de ser sostenible mucho antes de lo que uno cree.

Estas son cinco señales de que ya es momento de automatizar:

## 1. Respondes las mismas preguntas todos los días

Precio, horario, disponibilidad, formas de pago. Si el 80% de tus mensajes son variaciones de las mismas cinco preguntas, ese tiempo se puede recuperar.

## 2. Pierdes mensajes fuera de horario

Un cliente que escribe a las 9pm y no recibe respuesta hasta el otro día muchas veces ya compró en otro lado.

## 3. Tu equipo se distrae de tareas que sí necesitan criterio humano

Cada minuto respondiendo preguntas repetitivas es un minuto que no se usa en cerrar ventas o resolver casos complejos.

## 4. No sabes cuántos mensajes se te están escapando

Sin un registro claro, es fácil subestimar cuántas oportunidades se pierden por demoras en la respuesta.

## 5. Estás creciendo, pero contratar más personal para atender WhatsApp no es rentable todavía

Automatizar las respuestas repetitivas es casi siempre más barato y más rápido de poner en marcha que contratar y capacitar a alguien más.

Si te identificaste con dos o más de estas señales, es un buen momento para conversar. [Agenda tu consulta gratuita](/contacto) o escríbenos por WhatsApp.`,
  },
  {
    slug: 'cobro-digital-por-donde-empezar',
    titulo: 'Cobro digital para tu negocio: por dónde empezar sin complicarte',
    resumen: 'Una guía simple, sin jerga técnica, para dar el primer paso hacia el cobro digital en tu pyme.',
    fecha: '2026-06-01',
    cuerpoMarkdown: `Cobrar digitalmente suena a proyecto grande, pero casi siempre se puede empezar con pasos pequeños y seguros.

## Paso 1: define qué quieres resolver primero

No es lo mismo querer aceptar transferencias y códigos QR que integrar una pasarela de pago dentro de una app propia. Empieza por el problema más urgente, no por la solución más completa.

## Paso 2: revisa qué ya tienes

Muchos negocios ya tienen una cuenta bancaria empresarial y solo necesitan activar herramientas que ya existen, sin desarrollar nada nuevo.

## Paso 3: valida la seguridad

Cualquier solución de cobro digital que uses debe cumplir con las prácticas estándar de seguridad de la industria — esto no es negociable, sin importar el tamaño de tu negocio.

## Paso 4: prueba con un grupo pequeño de clientes

Antes de anunciarlo a todos, prueba el flujo completo con algunos clientes de confianza para detectar fricciones.

## Paso 5: crece desde ahí

Una vez el primer paso funciona bien, es mucho más fácil evaluar si necesitas integraciones más completas o modernizar otros sistemas.

Si quieres que te ayudemos a dar el primer paso, [agenda tu consulta gratuita](/contacto) o escríbenos por WhatsApp.`,
  },
];
