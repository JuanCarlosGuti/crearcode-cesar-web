# 01 — Visión y alcance

## 1. Objetivo del proyecto

Construir el sitio web corporativo de **Crear Code Cesar S.A.S.**, empresa
colombiana de servicios de software con domicilio en Valledupar (Cesar) y
operación en todo el país. El sitio debe cumplir tres funciones a la vez:

1. **Generar leads calificados** de pymes y empresas medianas colombianas
   interesadas en desarrollo a la medida, IA/automatización o soluciones
   tecnológicas (cobro digital, integraciones, modernización).
2. **Explicar sin jerga técnica** qué hace la empresa y por qué confiar en
   ella, a un público que no es técnico y que percibe la tecnología como
   algo caro o complejo.
3. **Servir de vitrina técnica**: el propio sitio (arquitectura hexagonal,
   dominio en español, TDD, Angular moderno) es evidencia de la forma de
   trabajar de Crear Code Cesar frente a un segundo público más técnico
   (áreas de TI de empresas medianas).

Este documento fija el objetivo, los públicos, los mensajes por página, el
alcance de la versión 1 (v1) frente a lo que queda fuera a propósito, y los
criterios con los que mediremos si el sitio cumple su función.

## 2. Públicos objetivo

### Público primario: dueño/gerente de pyme colombiana, perfil no técnico
- Necesita entender **en su idioma** (el del negocio, no el de la técnica)
  qué problema le resuelven y qué gana.
- Desconfía de vendedores de tecnología: busca señales de seriedad (casos,
  proceso claro, garantías, alguien que "hable como yo").
- Decide rápido si el contenido es claro; abandona si detecta jerga o
  promesas vagas.
- Punto de entrada probable: búsqueda en Google ("desarrollo de software
  Valledupar", "automatizar WhatsApp negocio", "cobro digital pyme"),
  referido, o red social del fundador.

### Público secundario: responsable de TI en empresa mediana
- Busca refuerzo especializado puntual (no reemplazo de su equipo).
- Sí valora señales técnicas: arquitectura prolija, buenas prácticas,
  perfil del fundador como arquitecto senior.
- Es más probable que revise "Sobre nosotros" y el blog en busca de
  profundidad técnica real, no solo marketing.

## 3. Mensajes clave por página

| Página | Mensaje clave | Objetivo de la página |
|---|---|---|
| Home | "Hacemos que la tecnología trabaje para tu negocio, sin que te quede grande" — los 3 servicios, prueba social, doble CTA | Enganchar en <10s y dirigir a servicio o contacto |
| Desarrollo a la medida | Tu negocio tiene procesos que ningún software genérico resuelve bien; te lo construimos a tu medida y sin sorpresas | Generar interés + confianza en el proceso |
| IA y automatización para pymes | Puedes automatizar atención, cotizaciones, conciliaciones y reportes sin ser una empresa "tech" | Desmitificar la IA, mostrar casos concretos de uso diario |
| Soluciones tecnológicas / cobro digital | Modernizar o integrar pagos no tiene que ser un proyecto de meses ni un riesgo | Transmitir seguridad y rapidez de implementación |
| Casos / Portafolio | Esto ya lo hicimos, así de bien salió | Prueba social concreta (aunque v1 use placeholders) |
| Sobre nosotros | Un arquitecto senior que también sabe de negocios y que enseña — no es "un programador más" | Diferenciación y confianza personal |
| Blog / Recursos | Te enseñamos, no solo te vendemos | Educación como marketing, SEO de cola larga |
| Contacto | Hablar con nosotros es fácil, rápido y sin compromiso | Conversión (formulario o WhatsApp) |
| Legales | Tus datos están protegidos y sabemos lo que hacemos | Cumplimiento Ley 1581 y confianza |

## 4. Alcance v1

Incluye todo lo descrito en el brief funcional:
- Páginas: Home, 3 páginas/secciones de servicio, Casos/Portafolio (con
  placeholders), Sobre nosotros, Blog/Recursos (estructura mínima),
  Contacto, Legales (política de datos + términos).
- Formulario de contacto que crea una `SolicitudDeContacto`, con
  validación, anti-spam (honeypot + rate limiting) y registro de
  consentimiento (Ley 1581).
- Notificación por correo al fundador cuando llega una solicitud nueva.
- Mini panel admin protegido: listar solicitudes y cambiar su estado
  (NUEVA → CONTACTADA → CONVERTIDA/DESCARTADA).
- SSR/prerender, metadatos por página, sitemap, robots, Open Graph.
- Diseño responsive mobile-first, contenido editable sin tocar código.

## 5. Backlog futuro (v2 — NO se construye en esta fase)

Explícitamente fuera de alcance de v1, para no generar sobre-alcance
durante la Etapa 2:

- **Agendamiento online** (calendario integrado para reservar la consulta
  gratuita). v1 usa WhatsApp/formulario como único canal.
- **Chat con IA** en el sitio (asistente conversacional para visitantes).
  Es coherente con el negocio, pero añade complejidad de producto y de
  moderación que v1 no necesita para validar el sitio.
- **Portal de clientes** (seguimiento de proyectos, facturas, entregables
  para clientes activos). Es un producto en sí mismo, no una página web.

Estas tres líneas quedan documentadas aquí como decisión consciente, no
como olvido. Cualquier HU o issue que las toque debe rechazarse o
redirigirse a un futuro documento de v2 hasta que el usuario lo apruebe
explícitamente.

> **Actualización (20 jul 2026)**: el usuario pidió explícitamente la
> v2 — chat con IA, portal/registro de clientes, demo de diseño con IA
> y gestión interna con facturación. El documento de v2 ya existe:
> [[10-vision-v2]] (Etapa 3, fases F8-F11). Sigue vigente que nada de
> v2 se construye antes de publicar la v1 (fase F7) y sin la
> aprobación explícita de ese documento.

## 6. Criterios de éxito

### Técnicos (verificables en cada entrega)
- **Lighthouse ≥ 90** en Performance, SEO y Accesibilidad (Best Practices
  como referencia adicional, sin umbral duro) en las páginas principales
  (Home, un servicio, Contacto), medidos en modo móvil.
- **Tests en verde**: pirámide de pruebas completa (ver
  [[06-plan-de-pruebas]]) y ArchUnit sin violaciones en cada build.
- **Contenido editable sin tocar componentes**: publicar o corregir un
  texto de servicio, un caso o un artículo de blog no requiere un cambio
  de código en un componente Angular, solo edición de los archivos de
  contenido/datos centralizados.

### De negocio (a evaluar tras publicación, fuera del alcance de código)
- **Leads/mes**: meta inicial orientativa de captar solicitudes de
  contacto calificadas mensuales una vez publicado (cifra objetivo a
  definir por el usuario cuando haya tráfico real; se deja como pendiente
  de negocio, no bloquea la Etapa 1).
- **Tiempo de publicación de contenido**: publicar un caso de éxito o
  artículo de blog nuevo debe tomar minutos (edición de archivo de
  contenido + build), no requerir intervención de un desarrollador.

## 7. Pendientes / placeholders detectados

Estos datos no estaban disponibles al escribir la documentación. Los que
ya se resolvieron quedan registrados aquí como referencia única; los que
siguen pendientes deben completarse antes de publicar el sitio.

| Dato | Estado | Valor / placeholder |
|---|---|---|
| Correo corporativo | Resuelto (temporal) | `crearcodecesar@gmail.com` — correo temporal en Gmail; se reemplaza por un correo con dominio propio cuando se compre el dominio (ver fila siguiente) |
| URL de LinkedIn del fundador | Resuelto | `https://www.linkedin.com/in/juan-carlos-gutierrez-huerfano369582/` |
| Dominio web definitivo | Pendiente (decisión al final, ver [[02-arquitectura]] y Etapa 2 fase F7) | No aplica — diseño agnóstico al dominio |
| Cifra objetivo de leads/mes | Pendiente de decisión de negocio | No aplica |

Datos ya confirmados y usados como reales en toda la documentación:
razón social Crear Code Cesar S.A.S., ciudad Valledupar (Cesar, Colombia),
WhatsApp 323 988 5883, nombre del fundador Juan Carlos Gutiérrez, correo
corporativo temporal crearcodecesar@gmail.com, LinkedIn del fundador.
