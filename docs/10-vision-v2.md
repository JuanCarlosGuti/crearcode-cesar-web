# 10 — Visión v2 (Etapa 3): de sitio vitrina a plataforma

Pedido por el usuario el 20 jul 2026: que el sitio deje de ser solo
estático y se convierta en una herramienta real — clientes que se
registran, un asistente de IA que responde las preguntas comunes y
escala a un humano, un demo de diseño asistido por IA para que un
visitante "pruebe" lo que podría tener, y una parte interna de gestión
con roles (diseño, desarrollo, facturación). Esto retoma exactamente la
visión que motivó ADR-08 en la fase F5 ("la idea es que la app escale y
puedan gestionar varias cosas empleados o personal con ciertos roles…
que sirva como herramienta de gestión… probablemente se facture y
contabilice con ella misma").

Este documento es el equivalente de la Etapa 1 para la v2: se aprueba
antes de escribir código, y es la fuente de verdad viva de la Etapa 3.
La descomposición fina en issues (ISS-083 en adelante) se hace al
arrancar cada fase, no toda por adelantado — las decisiones abiertas de
cada fase se cierran en ese momento.

## 1. Principio rector: primero se publica la v1

La Etapa 3 **no reemplaza ni adelanta** el cierre de la fase F7
(ISS-082: comprar dominio + publicar en Render). El sitio v1 se publica
primero; la plataforma se construye sobre el sitio vivo, fase por fase,
con las mismas reglas duras de siempre: TDD real, ArchUnit en verde, y
el OK explícito del usuario para pasar de cada fase a la siguiente.

## 2. Fases de la Etapa 3 (orden decidido por el usuario)

La numeración continúa la de la Etapa 2 (F0-F7).

### F8 — Cuentas de cliente

Registro público de clientes para "obtener mejores servicios" — y,
técnicamente, el prerequisito para controlar el gasto de IA por usuario
en F9/F10.

- Registro con correo + contraseña, verificación por correo (reusa el
  `NotificadorPort`/infra de correo de F2) y recuperación de contraseña
  (hoy no existe).
- Rol `CLIENTE` nuevo — el enum `Rol` de F5 se diseñó extensible para
  esto. Decisión de modelado a cerrar al arrancar: un rol por usuario
  (modelo actual) vs. conjunto de roles (`Set<Rol>`).
- Área "Mi cuenta" en el frontend, fuera de SSR/prerender igual que el
  panel admin (`RenderMode.Client`).
- Rate limiting en registro y verificación (la infra de reglas
  múltiples de F5 ya lo soporta).
- **No incluye**: login social (Google/etc.), 2FA, roles internos
  adicionales (van en F11).

### F9 — Asistente de IA (chatbot de preguntas frecuentes)

Widget de chat en el sitio público que responde las preguntas comunes
y escala a un humano cuando corresponde.

- **Anclado al contenido real del sitio**: el prompt de sistema se
  arma desde `contenido/` (las FAQs por servicio y los textos de
  [[08-contenido]] ya existen). Regla dura en el prompt: **nunca
  inventar precios ni promesas** — en la prueba real del 20 jul 2026 el
  modelo se inventó "paquetes desde $500.000 COP" sin que ese precio
  exista en ninguna parte; sin anclaje esto pasaría en producción.
- **Escalamiento a humano**: cuando la pregunta sale del contenido (o
  pide precio/cotización), el asistente redirige al CTA de WhatsApp que
  ya existe (`mensajeWhatsappParaRuta`) o al formulario de contacto.
- **Arquitectura hexagonal, proveedor detrás de un puerto**: el dominio
  define el puerto (ej. `GeneradorDeRespuestas`), la infraestructura
  implementa el adaptador de Groq (API compatible con OpenAI). Cambiar
  de proveedor después = escribir otro adaptador, cero cambios en
  dominio/aplicación. Se documentará como **ADR-10** al implementarlo.
- **La key nunca llega al navegador**: el flujo es navegador → proxy
  `/api` del frontend (ADR-09) → backend → Groq. Igual que todo lo
  demás, un solo origen público.
- **Límites de uso** (reusa el `RateLimitingFilter` de reglas
  múltiples): anónimos con límite bajo por IP; usuarios registrados
  (F8) con límite diario mayor. La capa gratis de Groq (~1.000
  peticiones/día en el modelo 70B, ver §3) es el techo global.
- **No incluye**: memoria entre sesiones, herramientas/function
  calling, voz, entrenamiento propio.

### F10 — Demo de diseño con IA

La herramienta diferenciadora: un usuario registrado describe su
negocio y recibe una propuesta visual básica generada por IA.

- Formulario breve (tipo de negocio, estilo deseado, colores) → la IA
  genera una propuesta de landing auto-contenida (HTML/CSS sin
  JavaScript) + paleta + estructura sugerida.
- Render en un `<iframe sandbox>` (sin scripts) — el HTML generado por
  IA nunca se ejecuta con privilegios en el sitio.
- N generaciones/día por usuario registrado (control de costo y
  abuso); las propuestas se guardan en su cuenta; CTA claro:
  "convierte esto en un proyecto real con nosotros".
- Solo para registrados (decisión de F8 + control de tokens).
- **No incluye**: editor visual interactivo, export a producción,
  sitios multipágina, imágenes generadas.

### F11 — Gestión interna: roles, cotizaciones y cuentas de cobro

La parte de herramienta de gestión para el equipo (hoy: el fundador).

- Roles internos `FACTURACION`, `DISENO`, `DESARROLLO` con matriz de
  permisos sobre el pipeline que ya existe (leads) y el nuevo:
  lead → cotización → proyecto → cuenta de cobro.
- **Alcance decidido por el usuario**: cotizaciones y cuentas de cobro
  generadas por la propia app (PDF), $0/mes. La **facturación
  electrónica DIAN queda explícitamente fuera** de esta primera
  versión: requiere un proveedor tecnológico autorizado (Alegra tiene
  API y planes de facturación desde ~$18.000 COP/mes; Siigo desde
  ~$191.000 COP/mes) y se integra cuando haya volumen real de facturas
  que lo justifique.
- **No incluye**: contabilidad completa, nómina electrónica,
  integración DIAN.

## 3. Proveedor de IA: Groq (decidido)

El usuario entregó una API key de **Groq** (groq.com — inferencia
ultrarrápida de modelos abiertos; no confundir con Grok de xAI, que
también se evaluó). Validada el 20 jul 2026 contra la API real:
`llama-3.3-70b-versatile` (contexto 131K) responde en español fluido y
rápido.

**Capa gratis de Groq** (jul 2026): ~30 peticiones/minuto y ~1.000
peticiones/día en el modelo 70B (14.400/día en modelos menores como
`llama-3.1-8b-instant`). Más que suficiente para el arranque; si el
tráfico crece, las alternativas siguen siendo baratas.

Comparativa consultada (jul 2026) — queda registrada para el día que
haya que escalar o cambiar de proveedor (solo cambia el adaptador):

| Proveedor | Modelo ref. | USD por 1M tokens (in/out) | Capa gratis |
|---|---|---|---|
| **Groq (elegido)** | Llama 3.3 70B | — (gratis en el arranque) | ~1.000 req/día |
| Google Gemini | 3 Flash / Flash-Lite | — | 1.000-1.500 req/día |
| DeepSeek | V4-Flash | $0,14 / $0,28 | No |
| xAI (Grok) | Grok 4.1 Fast | $0,20 / $0,50 | Solo $25 promo inicial (el programa de $150/mes cerró para cuentas nuevas) |
| Anthropic | Claude Haiku 4.5 | $1 / $5 | No |
| Anthropic | Claude Sonnet 5 | $3 / $15 (intro $2/$10 hasta 31 ago 2026) | No |

Orden de magnitud del costo si se pasara a pago: una conversación de
FAQ (~1.800 tokens) cuesta ~$1-2 COP en DeepSeek y ~$12 COP en Haiku.
Incluso con cientos de conversaciones al mes, el costo de IA es de
miles de pesos, no de cientos de miles — el free tier de Groq es para
arrancar sin fricción, no una restricción permanente.

**Manejo de la key** (regla dura):

- Solo en variables de entorno: `.env` local (gitignored, ya
  configurado) y variable `GROQ_API_KEY` en Render el día del
  despliegue. **Nunca en el repositorio ni en el navegador.**
- La key se compartió por chat al configurarla — es buena práctica
  rotarla en la consola de Groq periódicamente, y obligatorio rotarla
  si se sospecha exposición.

## 4. Consideraciones transversales

- **Privacidad (Ley 1581)**: los mensajes del chat viajan a la API de
  Groq (servidores en EE.UU.). Antes de lanzar F9: verificar los
  términos de datos de Groq (si usan el contenido de la capa gratis
  para entrenamiento), actualizar la política de tratamiento de datos
  del sitio para reflejar ese tercero, e instruir al asistente a no
  solicitar datos personales en el chat (para eso está el formulario,
  que ya tiene consentimiento explícito).
- **Sleep de Render (capa gratis)**: el backend dormido tarda 30-60s en
  despertar — el primer mensaje del chat puede sentirse lento. Se
  mitiga con UX ("conectando…") en F9; si en la práctica ahuyenta
  usuarios, es el primer candidato a justificar la capa paga de Render
  (~$7 USD/mes por servicio, ver [[09-despliegue]]).
- **Abuso y costos**: todo endpoint de IA queda detrás del rate
  limiting existente + límites por usuario; el free tier de Groq actúa
  de tope global natural (si se agota, el chat degrada a "escríbenos
  por WhatsApp", nunca a error).
- **Las reglas del proyecto no cambian**: hexagonal con ArchUnit, TDD
  real, incrementos pequeños, docs primero, OK explícito por fase.

## 5. Decisiones del usuario registradas (20 jul 2026)

1. **Orden**: F8 Cuentas → F9 Chat IA → F10 Demo diseño → F11 Gestión
   interna.
2. **Proveedor IA**: Groq, capa gratis, key entregada y validada;
   detrás de puerto intercambiable.
3. **Facturación**: cotizaciones y cuentas de cobro propias (PDF);
   DIAN vía proveedor autorizado queda para después, documentado.

## Aprobación

**Aprobado explícitamente por el usuario el 27 jul 2026** ("si
aprobada arranca por fa"), el mismo día de la publicación de la v1 en
Render (backend y frontend en producción, flujo de contacto verificado
extremo a extremo contra la base real de Neon). Prerequisito de
publicación cumplido; único pendiente de la Etapa 2 que sigue abierto:
la compra del dominio propio (no bloquea la Etapa 3).

## Fuentes consultadas (jul 2026)

- [Groq — free tier y límites](https://tokenmix.ai/blog/groq-free-tier-limits-2026)
- [Gemini API — free tier](https://dev.to/hiyoyok/gemini-api-cheatsheet-2026-free-tier-limits-models-and-endpoints-in-one-place-2god)
- [DeepSeek API — precios](https://www.nxcode.io/resources/news/deepseek-api-pricing-complete-guide-2026)
- [xAI Grok API — precios y créditos](https://mem0.ai/blog/xai-grok-api-pricing)
- Precios de Claude: referencia oficial de Anthropic (skill claude-api, jun 2026)
- [Alegra — facturación electrónica DIAN](https://www.alegra.com/colombia/facturacion-electronica/)
- [Alanube — API de facturación DIAN](https://www.alanube.co/colombia/)
