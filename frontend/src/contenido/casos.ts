import { Caso } from './tipos';

/**
 * Proyectos propios y de codigo abierto (28 sep 2026). Reemplazan a los
 * tres casos placeholder.
 *
 * No son trabajos de clientes y el texto no finge que lo sean: la misma
 * regla que saco los testimonios ficticios en F10e. Para una empresa
 * que todavia no ha vendido, esto pesa mas que un espacio reservado —
 * un visitante puede entrar a uparya.co, abrir la app de damana o leer
 * el codigo de la pasarela de pagos. Un testimonio no se puede
 * comprobar; esto si.
 *
 * Regla de entrada: cada proyecto trae un enlace que funciona. El que
 * no lo tenga, no entra (por eso quedo fuera la app de monday, cuyo
 * backend no tiene cara publica).
 */
export const CASOS: readonly Caso[] = [
  {
    slug: 'uparya',
    titulo: 'UparYa — directorio de negocios de Valledupar',
    linea: 'Desarrollo a la medida',
    reto: 'Los negocios de Valledupar se anuncian en grupos de WhatsApp y publicaciones que se pierden en un día. Quien busca un servicio no tiene dónde comparar, y quien lo presta no tiene dónde quedarse visible.',
    solucion:
      'Directorio local con ficha por negocio (fotos, ubicación y botón de WhatsApp), buscador que tolera tildes, reseñas verificadas por correo, cola de moderación y planes Gratis/Pro/Premium con límites parametrizables. Las herramientas de IA comprueban el cupo del plan antes de llamar al proveedor, con un presupuesto diario global como freno de costo.',
    resultado:
      'En producción, con las 28 historias obligatorias cerradas y 653 pruebas automatizadas: 389 unitarias, 107 de aceptación HTTP, 26 de integración contra PostgreSQL real y 131 de componente.',
    stack: 'NestJS 11 · Angular 22 SSR · Prisma · PostgreSQL · Cloudflare R2',
    enlace: { url: 'https://uparya.co', etiqueta: 'Visitar uparya.co' },
    metaDescripcion:
      'UparYa, directorio de negocios de Valledupar: planes, moderación, reseñas y herramientas de IA con cupo por plan. Emprendimiento propio de Crear Code Cesar, en producción.',
  },
  {
    slug: 'corpus-damana',
    titulo: 'Corpus Damana — estudiar la lengua del pueblo Wiwa',
    linea: 'IA y automatización',
    reto: 'El damana (dʉmʉna), lengua del pueblo Wiwa de la Sierra Nevada, tiene pocos hablantes y casi ningún material digital de estudio. Un traductor de IA genérico se inventa palabras que no existen, que es peor que no traducir.',
    solucion:
      'Aplicación de estudio construida sobre un corpus real: concordancias, diccionario de frecuencias, tarjetas con repetición espaciada y traducción asistida por IA anclada al corpus — el modelo responde con lo que está documentado o dice que no lo sabe.',
    resultado:
      'En producción. Es el mismo patrón de anclaje que usa el asistente de este sitio, que por eso tampoco inventa precios.',
    stack: 'NestJS · Angular · Groq',
    enlace: { url: 'https://corpus-damana.onrender.com', etiqueta: 'Abrir la aplicación' },
    metaDescripcion:
      'Corpus Damana: app de estudio del damana, lengua del pueblo Wiwa, con traducción por IA anclada a un corpus real. Proyecto propio de Crear Code Cesar.',
  },
  {
    slug: 'pasarela-cripto',
    titulo: 'Pasarela de Pagos Cripto — cobrar en cripto, recibir pesos',
    linea: 'Soluciones tecnológicas',
    reto: 'Muchos colombianos tienen saldo en criptomonedas, pero casi ningún comercio lo acepta: nadie quiere volatilidad, contabilidad cripto ni riesgo legal.',
    solucion:
      'El comercio cobra con un QR y recibe pesos colombianos en su cuenta. La plataforma nunca custodia fondos: solo orquesta el pago, y la conversión y el KYC los hace un proveedor de rampa ya regulado. Backend en arquitectura hexagonal con reglas verificadas por ArchUnit, webhooks idempotentes y pruebas con Testcontainers.',
    resultado:
      'En desarrollo, con el código abierto para revisar: backend y frontend en repositorios públicos.',
    stack: 'Java 25 · Spring Boot · PostgreSQL · Angular 22',
    enlace: { url: 'https://github.com/JuanCarlosGuti/pasarela-Cripto', etiqueta: 'Ver el código' },
    metaDescripcion:
      'Pasarela de Pagos Cripto: el comercio cobra en criptomonedas y recibe pesos colombianos, sin custodia de fondos. Proyecto propio de Crear Code Cesar, en desarrollo.',
  },
  {
    slug: 'cesar-travel',
    titulo: 'Cesar Travel — alojamientos del Cesar y La Guajira',
    linea: 'Desarrollo a la medida',
    reto: 'Una plataforma de alojamientos necesita catálogo por municipio, disponibilidad por fechas, reservas, reseñas y conversación entre viajero y anfitrión, sin que ninguna de esas piezas se pise con las demás.',
    solucion:
      'La misma plataforma construida dos veces a propósito: un monolito NestJS + Angular que corre donde haya Node, y una versión en microservicios —seis servicios Java 25 con Spring Boot 4, MySQL y React— pensada para infraestructura propia.',
    resultado:
      'Las dos versiones públicas y ejecutables. Sirve para enseñar el criterio con el que trabajamos: la arquitectura se elige por el despliegue que toca, no por moda.',
    stack: 'NestJS · Angular · Java 25 · Spring Boot 4 · MySQL · React',
    enlace: { url: 'https://github.com/JuanCarlosGuti/cesar-travel', etiqueta: 'Ver el código' },
    metaDescripcion:
      'Cesar Travel: plataforma de alojamientos del Cesar y La Guajira, construida como monolito NestJS y como microservicios en Java 25. Código abierto de Crear Code Cesar.',
  },
];
