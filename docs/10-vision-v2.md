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

### F8.5 — Rediseño visual y valor de la cuenta (intercalada el 28 jul 2026)

Fase corta intercalada a pedido del usuario tras cerrar F8: el sitio
es correcto pero visualmente plano, y el registro no comunica ningún
beneficio — el usuario quiere que la gente **quiera** registrarse
(registro opcional y atractivo), no que le toque. Decisiones del
usuario (28 jul 2026): **evolucionar la paleta actual** (no rediseño
radical — se mantiene la identidad "Minimal Corporativo" de
[[07-guia-de-estilo]] y lo ganado en F6) y **diseño primero, F9
después** (el sitio se ve atractivo cuanto antes; F9 llena el
beneficio real con el chat IA).

- **Sistema visual evolucionado**: gradientes de marca, sombras de
  elevación, transiciones y micro-animaciones (hover, scroll-reveal)
  — respetando `prefers-reduced-motion`, el contraste AA y los
  puntajes Lighthouse de F6 (detalle en [[07-guia-de-estilo]]
  §Evolución visual).
- **Hero de la Home renovado** y jerarquía de CTAs más atractiva.
- **Sección "Tu cuenta te da más"** en la Home: tarjetas de beneficios
  del registro con CTA a `/registro`. Honesta: el asistente IA (F9) y
  el demo de diseño (F10) se anuncian como "muy pronto" — acceso
  anticipado para registrados; nunca se promete lo que aún no existe.
- **Página `/registro` con beneficios visibles** junto al formulario.
- **No incluye**: cambiar paleta/tipografía base, imágenes de
  contenido (la convención de [[07-guia-de-estilo]] sigue pendiente de
  fotos reales), ni nada del chat IA (eso es F9).

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

**Ampliada el 29 jul 2026 a pedido del usuario**: F10 pasa de "solo el
demo de diseño" a un **Centro de herramientas con IA** — el sitio como
herramienta viva que le demuestra a cada pyme lo que la tecnología
puede hacer por SU negocio (investigación de capas gratuitas: Groq ya
integrado; Gemini 2.5 Flash Image ~500 imágenes/día gratis; Cloudflare
Workers AI 10.000 neurons/día como respaldo documentado; Pollinations
solo para prototipos, sin SLA). Cuatro herramientas, ordenadas de
menor a mayor esfuerzo, cada una con límites diarios al estilo del
asistente (F9) y el registro como beneficio (más usos por día):

- **F10a — Cotizador interactivo** (sin IA): wizard de 3 pasos (tipo
  de proyecto → alcance → urgencia) que termina en un RANGO orientativo
  — nunca cifras exactas — + CTA de contacto/WhatsApp con el resumen
  prellenado. Incluye la página **/herramientas** (centro con las 4
  tarjetas, "Muy pronto" en las que falten).
- **F10b — Simulador "un chatbot para tu negocio"**: el visitante
  escribe nombre y rubro de su empresa y conversa con el chatbot que
  su negocio podría tener (reusa el puerto `GeneradorDeRespuestas` y
  los límites de F9, con plantilla de prompt segura anti-abuso).
- **F10c — Diagnóstico digital**: quiz de ~6 preguntas sobre cómo
  opera el negocio → "radiografía" con 3 oportunidades de
  automatización generada por IA, mostrada EN PANTALLA (el envío por
  correo espera a que el correo de producción se active en el MVP) +
  CTA de contacto.
- **F10d — Demo de diseño con IA** (la estrella, solo registrados):
  formulario breve (sector, qué hace, qué necesita) → boceto visual
  generado (imagen vía Gemini Flash Image, key gratuita
  `GEMINI_API_KEY` solo por entorno) + lista de funcionalidades
  sugeridas (Groq) + opción de variación + CTA "hazlo realidad".
  Anti-abuso: la imagen se muestra como imagen (nunca HTML ejecutable);
  límites diarios propios.
- **No incluye**: editor visual interactivo, export a producción,
  sitios multipágina, memoria entre sesiones.

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

Registradas el 28 jul 2026 (al cerrar F8):

4. **F8.5 intercalada** entre F8 y F9: rediseño visual + comunicar el
   valor de la cuenta. El registro es **opcional y atractivo** — la
   gente debe querer registrarse por lo que obtiene, no sentirse
   obligada.
5. **Alcance visual**: evolucionar la paleta actual (no rediseño
   radical) — se conserva la identidad "Minimal Corporativo" y los
   resultados de F6.
6. **Orden**: diseño primero (F8.5), F9 después.

Registradas el 29 jul 2026 (al arrancar F10):

7. **F10 ampliada a Centro de herramientas con IA** (cotizador,
   simulador de chatbot, diagnóstico digital y demo de diseño):
   "implementemos todo lo que se pueda" con capas gratuitas primero.
8. **Proveedor de imágenes**: Gemini 2.5 Flash Image (capa gratis
   ~500/día, key gratuita) para el demo de diseño; Cloudflare Workers
   AI documentado como respaldo. La `GEMINI_API_KEY` seguirá el mismo
   camino que la de Groq (.env local + dashboard de Render).
9. **Correo de producción pospuesto al MVP** (28 jul): el informe del
   diagnóstico se muestra en pantalla, sin envío por correo en F10.

Registradas el 10 ago 2026 (al aprobar el prototipo del rediseño):

10. **Prototipo visual aprobado como referencia de F10**: el usuario
    generó con una IA de diseño el archivo "Crear Code - Rediseno
    (standalone).html" (Home, página de servicio, centro de
    herramientas y los 5 estados del demo, en desktop 1280 y móvil
    375). Respeta la paleta y los tokens de F8.5; su estructura y
    microcopy quedan portados a docs/05 y docs/08 — el HTML del
    prototipo es referencia visual, NO código a reutilizar (se
    implementa en nuestro stack Angular con nuestras reglas).
11. **El centro de herramientas es una página VIVA**: cotizador,
    diagnóstico y simulador se usan directamente dentro de
    `/herramientas` (como en el prototipo), no en páginas separadas;
    solo el demo de diseño tiene página propia por su peso y su
    bloqueo por cuenta.
12. **Nueva sub-fase F10e — rediseño de Home y páginas de servicio**
    según el prototipo: hero con tarjeta del demo, sección del centro
    de herramientas, sección del asistente con preguntas sugeridas,
    tabla "visitante vs. con cuenta", placeholders honestos de
    casos/equipo, y en servicios breadcrumb + aside del diagnóstico +
    "lo que resolvemos" + "cómo trabajamos".
13. **Ajustes de honestidad sobre el prototipo**: la captura de correo
    del diagnóstico ("Enviarme el informe") queda para el MVP (cuando
    el correo de producción se active) — en F10 el informe va en
    pantalla; los rangos COP del cotizador que muestra el prototipo
    (4–9 / 9–22 / 22–60 millones según alcance) son la PROPUESTA a
    aprobar por el usuario antes de publicar F10a; los límites diarios
    que muestra son copy configurable, la fuente de verdad son las
    variables `ASISTENTE_*`/`DEMO_*`. El hero del prototipo acorta el
    eslogan ("Tecnología que trabaja para tu negocio", sin "no al
    revés") — la revisión del eslogan sigue PENDIENTE como decisión
    aparte del usuario.
14. **Niveles de prueba obligatorios** (pedido del usuario): cada
    issue de F10 cubre sus niveles aplicables — Unit, Component,
    Integration, API y End-to-End — según el mapa de
    [06-plan-de-pruebas.md](06-plan-de-pruebas.md) §7.
15. **Segunda entrega del prototipo aprobada** (10 ago 2026): proyecto
    de Claude Design "Crear Code - Pantallas faltantes"
    (claude.ai/design/p/f2d5fbdc-a74f-485f-86f1-0df716cfd17f).
    Cubre el diagnóstico completo (quiz → analizando → radiografía EN
    PANTALLA → límite), el simulador (conversación, límite de
    mensajes, error), las páginas de cuenta (registro con beneficios,
    ingreso, mi-cuenta con usos del día por herramienta), el widget
    del asistente abierto y el restyling de casos/sobre-nosotros/blog,
    todo en 1280 y 375. Cumple la decisión 13 (radiografía sin campo
    de correo) y el contraste AA. Es la referencia visual de F10b-F10e;
    su microcopy quedó portado a docs/08.
16. **Proveedor de imágenes conmutable** (10 ago 2026): verificado con
    la key real del usuario (válida) que la capa gratis de Gemini ya
    NO incluye modelos de imagen (429 "limit: 0" en todos). El puerto
    `GeneradorDeImagenes` tiene dos adaptadores conmutables por
    `DEMO_PROVEEDOR_IMAGENES`: `pollinations` (interino por defecto:
    gratis, sin key, verificado funcionando — pero SIN SLA) y `gemini`
    (listo para cuando vuelva la cuota o haya billing). La decisión
    del proveedor DE PRODUCCIÓN quedó CERRADA por el usuario el 10 ago
    2026 (opción A): **Pollinations para el MVP** — gratis, sin cuenta,
    verificado; riesgo de SLA aceptado y documentado. Cloudflare
    Workers AI (10k neurons/día gratis, requiere cuenta y token) queda
    como plan B listo si Pollinations falla — el cambio es solo la
    variable `DEMO_PROVEEDOR_IMAGENES`.

17. **Los dos proveedores a la vez, con respaldo automático** (10 ago
    2026, a pedido del usuario — "¿las dos no se puede? ya tenemos
    Cloudflare configurada"): en vez de elegir uno, con
    `DEMO_PROVEEDOR_IMAGENES=cloudflare` el sistema usa **Cloudflare
    Workers AI como primario y cae solo a Pollinations** si el primario
    falla (`GeneradorDeImagenesConRespaldo`), sin que el visitante se
    entere. Como ninguno de los dos tiene SLA en su capa gratis, el
    respaldo es la diferencia entre "el demo no sirve" y "el demo tardó
    un poco más". Qué proveedor se arma vive en un único punto
    (`ConfiguracionDeGeneradorDeImagenes`), no repartido en
    anotaciones condicionales por adaptador. Implementado y probado
    contra stub en ISS-137; **queda en `pollinations` hasta que el
    usuario cargue `CLOUDFLARE_ACCOUNT_ID` y `CLOUDFLARE_API_TOKEN`** y
    se pruebe contra el servicio real — activarlo antes solo añadiría
    un intento fallido a cada generación.

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
