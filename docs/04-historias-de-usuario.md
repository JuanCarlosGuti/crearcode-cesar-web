# 04 — Historias de usuario

Plantilla obligatoria para cada historia:

> **HU-XX — [Título]**
> Como [rol] quiero [acción] para [beneficio]
> **Criterios de aceptación** (Dado/Cuando/Entonces, mínimo 3, incluye casos tristes)
> **Reglas de negocio asociadas**
> **Notas de UX**
> **Prioridad (MoSCoW)**

Roles usados: **visitante** (dueño/gerente de pyme o responsable de TI
navegando el sitio), **lead** (visitante que deja sus datos), **fundador**
(único usuario del panel admin en v1).

---

## Épica E1 — Contenido público

### HU-01 — Conocer la propuesta de valor en la Home
Como **visitante** quiero **entender en segundos qué hace Crear Code
Cesar** para **decidir si sigo explorando el sitio**.

- Dado que entro a la Home por primera vez, cuando la página carga,
  entonces veo una frase de propuesta de valor visible sin necesidad de
  hacer scroll (above the fold).
- Dado que estoy en un teléfono móvil, cuando abro la Home, entonces la
  propuesta de valor y el CTA principal siguen visibles sin scroll
  horizontal.
- Dado que tengo JavaScript deshabilitado o una conexión lenta, cuando
  abro la Home, entonces el contenido principal (texto de propuesta de
  valor) ya está en el HTML servido (SSR/prerender), no depende de
  hidratación para ser legible.

**Reglas de negocio**: el texto de propuesta de valor es contenido
editable (ver [[08-contenido]]), no un literal en el componente.
**Notas de UX**: cero jerga técnica; frase corta, orientada a beneficio
del negocio del visitante, no a tecnología usada.
**Prioridad**: Must.

### HU-02 — Descubrir los 3 servicios desde la Home
Como **visitante** quiero **ver de un vistazo los 3 servicios que ofrece
la empresa** para **identificar cuál me interesa**.

- Dado que estoy en la Home, cuando llego a la sección de servicios,
  entonces veo las 3 tarjetas (Desarrollo a la medida, IA y
  automatización, Soluciones tecnológicas) con un resumen breve cada una.
- Dado que veo una tarjeta de servicio, cuando hago clic/tap sobre ella,
  entonces navego a la página o sección ancla de detalle de ese servicio.
- Dado que estoy navegando con teclado, cuando llego a las tarjetas con
  Tab, entonces cada una es enfocable y activable con Enter, con foco
  visible.

**Reglas de negocio**: los 3 servicios y su orden son fijos en v1 (no
configurables dinámicamente).
**Notas de UX**: iconografía o imagen simple por servicio; texto de
tarjeta enfocado en el problema que resuelve, no en tecnología.
**Prioridad**: Must.

### HU-03 — Ver prueba social en la Home
Como **visitante** quiero **ver evidencia de que otros ya confiaron en
la empresa** para **reducir mi desconfianza inicial**.

- Dado que estoy en la Home, cuando llego a la sección de prueba social,
  entonces veo logos de clientes y/o testimonios (placeholder en v1,
  editable después).
- Dado que aún no hay logos/testimonios reales cargados, cuando la
  sección se renderiza, entonces se muestra contenido placeholder
  claramente reemplazable, sin romper el layout ni mostrar datos falsos
  como si fueran reales.
- Dado que hay más de 3 testimonios cargados, cuando la sección se
  renderiza, entonces se muestra un subconjunto o carrusel simple sin
  saturar la Home.

**Reglas de negocio**: los testimonios y logos son contenido editable
independiente del código (ver [[08-contenido]]).
**Notas de UX**: placeholders visualmente honestos (no logos inventados
de empresas reales).
**Prioridad**: Should.

### HU-04 — Iniciar contacto desde cualquier página
Como **visitante** quiero **encontrar siempre una forma de contactar**
para **no tener que buscar cómo hacerlo**.

- Dado que estoy en cualquier página del sitio, cuando reviso el
  encabezado o pie de página, entonces veo un CTA doble persistente:
  "Agenda tu consulta gratuita" (→ Contacto) y botón de WhatsApp directo.
- Dado que hago clic en el botón de WhatsApp desde cualquier página,
  cuando se abre WhatsApp (web o app), entonces el mensaje viene
  precargado (ver HU-17).
- Dado que estoy en una página larga (ej. un artículo de blog), cuando
  hago scroll, entonces el CTA sigue accesible sin tener que volver
  arriba (ej. header fijo o botón flotante), sin tapar contenido
  permanentemente.

**Reglas de negocio**: el CTA doble aparece en todas las páginas de
contenido público (no en el panel admin).
**Notas de UX**: el botón de WhatsApp usa el color/ícono reconocible de
la plataforma, sin desentonar con la paleta del sitio (ver
[[07-guia-de-estilo]]).
**Prioridad**: Must.

### HU-05 — Conocer el detalle de un servicio
Como **visitante** quiero **entender en profundidad un servicio
específico** para **decidir si se ajusta a lo que necesito**.

- Dado que entro a la página de un servicio (ej. IA y automatización),
  cuando la página carga, entonces veo: problema que resuelve, qué
  incluye, proceso en 3-4 pasos, entregables y una sección de FAQ.
- Dado que estoy leyendo la FAQ de un servicio, cuando hago clic en una
  pregunta, entonces se expande la respuesta sin recargar la página.
- Dado que llego a la página de un servicio desde un enlace externo
  (buscador), cuando la página carga, entonces el `<title>` y meta
  description son específicos de ese servicio, no genéricos del sitio.
- Dado que termino de leer el detalle del servicio, cuando llego al
  final de la página, entonces encuentro un CTA claro para dar el
  siguiente paso (agendar/WhatsApp).

**Reglas de negocio**: las 3 páginas de servicio comparten la misma
estructura de contenido (problema/incluye/proceso/entregables/FAQ)
definida como esquema de datos (ver ADR-05 en [[02-arquitectura]]).
**Notas de UX**: proceso de 3-4 pasos representado visualmente (timeline
o numerado), no solo como párrafo.
**Prioridad**: Must.

### HU-06 — Explorar el portafolio de casos
Como **visitante** quiero **ver ejemplos de proyectos anteriores** para
**confirmar que la empresa sabe resolver problemas como el mío**.

- Dado que entro a la página de Casos, cuando la página carga, entonces
  veo un listado de casos (2-3 en v1, placeholder) con título y resumen
  de cada uno.
- Dado que aún no hay casos reales publicados, cuando la página se
  renderiza, entonces los casos placeholder están claramente marcados
  como ejemplo, no como clientes reales.
- Dado que hago clic en un caso del listado, cuando navego, entonces
  llego al detalle de ese caso (ver HU-07).

**Reglas de negocio**: cada caso sigue la estructura título/reto/solución/
resultado (ver [[08-contenido]]).
**Notas de UX**: pensado para crecer — el listado debe soportar más
casos sin rediseño cuando el usuario publique reales.
**Prioridad**: Should.

### HU-07 — Leer el detalle de un caso de éxito
Como **visitante** quiero **leer el detalle completo de un caso** para
**entender cómo la empresa aborda un problema similar al mío**.

- Dado que entro al detalle de un caso, cuando la página carga, entonces
  veo reto, solución y resultado claramente diferenciados.
- Dado que el caso no tiene resultado cuantificado todavía (placeholder),
  cuando la página se renderiza, entonces se muestra un texto placeholder
  honesto en vez de una cifra inventada.
- Dado que termino de leer un caso, cuando llego al final, entonces
  encuentro un CTA para agendar consulta y, si aplica, enlace a casos
  relacionados o al listado de portafolio.

**Reglas de negocio**: ninguna cifra o resultado se presenta como real
hasta que el usuario reemplace el placeholder.
**Notas de UX**: formato legible tipo "caso de estudio", no muro de
texto.
**Prioridad**: Should.

### HU-08 — Conocer a la empresa y al fundador
Como **visitante** quiero **saber quién está detrás de la empresa** para
**generar confianza antes de contactar**.

- Dado que entro a "Sobre nosotros", cuando la página carga, entonces
  veo la historia de la empresa, el perfil del fundador (arquitecto de
  software senior + administrador de empresas + instructor SENA) y la
  forma de trabajar (documentar → probar → construir).
- Dado que reviso el perfil del fundador, cuando leo la sección,
  entonces el texto conecta explícitamente el perfil dual con el
  beneficio para el visitante ("hablamos el idioma del negocio, no solo
  el de la técnica"), no solo enumera credenciales.
- Dado que soy del público técnico (responsable de TI), cuando reviso
  esta página, entonces encuentro suficiente detalle técnico/profesional
  para evaluar seriedad, sin que la página se vuelva un CV plano.

**Reglas de negocio**: el nombre del fundador (Juan Carlos Gutiérrez) y
sus credenciales son datos reales, no placeholder.
**Notas de UX**: tono cercano; puede incluir foto (placeholder si no hay
una disponible aún).
**Prioridad**: Must.

### HU-09 — Explorar artículos del blog
Como **visitante** quiero **ver los artículos disponibles** para
**aprender algo útil antes de decidir contactar**.

- Dado que entro a Blog/Recursos, cuando la página carga, entonces veo
  un listado de artículos con título, resumen y fecha.
- Dado que todavía no hay artículos publicados, cuando la página se
  renderiza, entonces se muestra un estado vacío claro (no un error ni
  una página en blanco sin explicación).
- Dado que hay varios artículos, cuando los reviso, entonces están
  ordenados del más reciente al más antiguo.

**Reglas de negocio**: cada artículo es un archivo Markdown con
metadatos (título, fecha, resumen, slug) — ver ADR-05.
**Notas de UX**: estructura mínima en v1, sin categorías/tags todavía
(puede añadirse después sin romper el modelo de contenido).
**Prioridad**: Should.

### HU-10 — Leer un artículo del blog
Como **visitante** quiero **leer un artículo completo** para **aprender
sobre el tema y evaluar el criterio técnico de la empresa**.

- Dado que hago clic en un artículo del listado, cuando la página carga,
  entonces veo el contenido completo renderizado desde Markdown, con
  formato legible (encabezados, listas, código si aplica).
- Dado que llego al artículo desde un buscador, cuando la página carga,
  entonces tiene metadatos SEO propios (title, description, Open Graph)
  distintos de los genéricos del sitio.
- Dado que termino de leer, cuando llego al final del artículo, entonces
  encuentro un CTA hacia contacto o hacia un servicio relacionado.

**Reglas de negocio**: el contenido del artículo vive en Markdown, fuera
de los componentes Angular (ADR-05).
**Notas de UX**: tiempo estimado de lectura opcional, buena tipografía
de lectura larga.
**Prioridad**: Should.

### HU-11 — Consultar las políticas legales
Como **visitante** (o lead) quiero **leer la política de tratamiento de
datos y los términos** para **saber cómo se usan mis datos antes de dejar
mi información**.

- Dado que estoy en cualquier página, cuando busco el enlace a legales
  (normalmente en el pie de página), entonces lo encuentro fácilmente.
- Dado que entro a la política de tratamiento de datos, cuando la página
  carga, entonces el texto cumple con lo exigido por la Ley 1581 de 2012
  y usa los datos reales de la empresa (razón social, domicilio,
  contacto).
- Dado que estoy en el formulario de contacto, cuando reviso el checkbox
  de consentimiento, entonces hay un enlace directo a esta página, no
  solo una mención genérica.

**Reglas de negocio**: el texto legal es borrador para revisión del
usuario/asesor legal, no texto certificado en v1 (ver [[08-contenido]]).
**Notas de UX**: texto largo pero bien estructurado con subtítulos, no
un bloque único.
**Prioridad**: Must.

---

## Épica E2 — Captura de leads

### HU-12 — Enviar el formulario de contacto
Como **visitante** quiero **enviar mis datos y mi necesidad** para
**que la empresa me contacte**.

- Dado que completo todos los campos obligatorios (nombre, correo,
  teléfono, servicio de interés, mensaje) y acepto el consentimiento,
  cuando envío el formulario, entonces se crea una `SolicitudDeContacto`
  en estado `NUEVA` y veo una confirmación clara en pantalla.
- Dado que envío el formulario correctamente, cuando la solicitud se
  registra, entonces se dispara la notificación al fundador (HU-18) sin
  que yo tenga que esperar visiblemente ese proceso (respuesta rápida al
  visitante).
- Dado que el backend no está disponible o falla el envío, cuando
  intento enviar el formulario, entonces veo un mensaje de error claro
  que no pierde los datos que ya escribí, con opción de reintentar o
  contactar por WhatsApp como alternativa.
- Dado que envío el formulario sin marcar el consentimiento de datos,
  cuando intento enviar, entonces el envío se bloquea con un mensaje
  explicando por qué (caso triste).

**Reglas de negocio**: refleja las invariantes de dominio en
[[03-modelo-de-dominio]] (consentimiento obligatorio, datos válidos,
estado inicial `NUEVA`).
**Notas de UX**: formulario corto, agrupado lógicamente, mensaje de
éxito que reduzca ansiedad ("te contactamos en menos de X horas").
**Prioridad**: Must.

### HU-13 — Recibir validación de errores en el formulario
Como **lead** quiero **saber de inmediato si algo está mal en mis datos**
para **corregirlo sin frustrarme**.

- Dado que escribo un correo con formato inválido, cuando salgo del
  campo (blur) o intento enviar, entonces veo un mensaje de error
  específico junto al campo, usando Signal Forms.
- Dado que escribo un teléfono que no cumple el formato colombiano
  esperado, cuando intento enviar, entonces veo un mensaje de error
  específico (no un error genérico "formulario inválido").
- Dado que dejo vacío el campo nombre o mensaje, cuando intento enviar,
  entonces el envío se bloquea y el campo vacío se marca visualmente.
- Dado que corrijo un campo marcado con error, cuando el valor pasa a
  ser válido, entonces el mensaje de error desaparece sin necesidad de
  reenviar todo el formulario.

**Reglas de negocio**: las mismas reglas de validación del VO
`DatosDeContacto` en el dominio se reflejan en el frontend (validación
duplicada a propósito: UX inmediata en cliente + garantía real en
servidor).
**Notas de UX**: mensajes de error en español claro, sin jerga
("Escribe un correo válido, ej. nombre@empresa.com").
**Prioridad**: Must.

### HU-14 — Dar consentimiento de tratamiento de datos
Como **lead** quiero **entender y aceptar explícitamente cómo se usarán
mis datos** para **cumplir con mi derecho a la información (Ley 1581)**.

- Dado que estoy en el formulario, cuando reviso el bloque de
  consentimiento, entonces veo un checkbox no marcado por defecto, con
  texto breve y enlace a la política completa.
- Dado que marco el checkbox y envío, cuando la solicitud se registra,
  entonces se guarda un `ConsentimientoDatos` con `aceptado = true` y
  timestamp de aceptación.
- Dado que no marco el checkbox, cuando intento enviar el formulario,
  entonces el envío no se completa y el foco se mueve al checkbox
  (caso triste, cubre también HU-12).

**Reglas de negocio**: ver invariante 1 en [[03-modelo-de-dominio]] —
ninguna solicitud existe sin consentimiento aceptado.
**Notas de UX**: checkbox nunca premarcado (consentimiento explícito,
no implícito).
**Prioridad**: Must.

### HU-15 — El sistema descarta envíos automatizados (honeypot)
Como **fundador** quiero **que los envíos de bots no generen leads
falsos** para **no perder tiempo revisando spam**.

- Dado que un bot completa el campo oculto honeypot del formulario,
  cuando se envía, entonces la solicitud se descarta silenciosamente
  (sin crear `SolicitudDeContacto` ni notificar), y el bot recibe una
  respuesta de éxito aparente (para no revelar el mecanismo).
- Dado que un visitante humano usa el formulario normalmente, cuando lo
  envía, entonces el campo honeypot permanece vacío y el envío procede
  sin fricción visible para la persona.
- Dado que el honeypot detecta un envío sospechoso, cuando esto ocurre,
  entonces queda un registro técnico (log de aplicación, sin datos
  personales) para poder ajustar el mecanismo si genera falsos
  positivos.

**Reglas de negocio**: el honeypot vive en `aplicacion/` o antes,
nunca en `dominio/` (ver [[03-modelo-de-dominio]] §7).
**Notas de UX**: el campo honeypot es invisible para personas (oculto
por CSS, no `type="hidden"` fácil de detectar por bots simples, y con
`tabindex="-1"`/`aria-hidden` para no afectar accesibilidad).
**Prioridad**: Must.

### HU-16 — El sistema limita envíos repetidos (rate limiting)
Como **fundador** quiero **que no se pueda saturar el formulario con
envíos masivos** para **proteger el correo de notificaciones y la base
de datos**.

- Dado que una misma IP envía el formulario varias veces en un lapso
  corto, cuando supera el límite configurado, entonces las siguientes
  solicitudes se rechazan con un mensaje claro (ej. "ya recibimos tu
  mensaje, te contactaremos pronto") sin crear nuevas solicitudes.
- Dado que un visitante legítimo envía el formulario una vez, cuando lo
  hace, entonces no se ve afectado por el límite de tasa.
- Dado que se alcanza el límite de tasa, cuando ocurre, entonces la
  respuesta HTTP usa un código apropiado (429) y no expone detalles
  internos de la implementación.

**Reglas de negocio**: mecanismo transversal de infraestructura (ver
[[03-modelo-de-dominio]] §7), no una regla del agregado.
**Notas de UX**: el mensaje de bloqueo no debe sonar como error del
usuario ("inténtalo de nuevo más tarde" es preferible a "demasiadas
solicitudes").
**Prioridad**: Should.

### HU-17 — Contactar directamente por WhatsApp
Como **visitante** quiero **escribir por WhatsApp con un solo clic** para
**evitar llenar un formulario si prefiero hablar directo**.

- Dado que hago clic en el botón de WhatsApp, cuando se abre la app o
  WhatsApp Web, entonces el número precargado es 323 988 5883 y el
  mensaje viene prellenado con un texto de contexto (ej. "Hola, vengo del
  sitio web y quiero saber más sobre...").
- Dado que estoy en la página de un servicio específico, cuando hago
  clic en el botón de WhatsApp de esa página, entonces el mensaje
  precargado menciona ese servicio en particular (personalización
  simple por página).
- Dado que estoy en un dispositivo sin WhatsApp instalado, cuando hago
  clic en el botón, entonces se abre WhatsApp Web como alternativa, sin
  quedar en un enlace roto.

**Reglas de negocio**: este canal no pasa por `RegistrarSolicitudUseCase`
— es un enlace externo directo, no genera `SolicitudDeContacto`.
**Notas de UX**: ícono reconocible de WhatsApp, visible en header/footer
y en cada página de servicio.
**Prioridad**: Must.

---

## Épica E3 — Notificación y panel admin

### HU-18 — Fundador recibe notificación de nueva solicitud
Como **fundador** quiero **enterarme por correo apenas llega un lead**
para **responder rápido y no perder la oportunidad**.

- Dado que se registra una nueva `SolicitudDeContacto`, cuando el caso
  de uso `RegistrarSolicitudUseCase` termina exitosamente, entonces se
  envía un correo al fundador con los datos clave de la solicitud
  (nombre, servicio de interés, mensaje).
- Dado que el envío de correo falla (ej. SMTP caído), cuando esto
  ocurre, entonces la solicitud ya quedó persistida de todas formas (la
  notificación no debe ser condición para guardar el lead) y el fallo
  queda registrado en logs para revisión manual.
- Dado que llegan varias solicitudes en poco tiempo, cuando cada una se
  registra, entonces cada una dispara su propia notificación
  independiente (sin agrupar ni perder ninguna).

**Reglas de negocio**: `NotificadorPort` se invoca después de persistir
exitosamente, nunca antes (ver [[03-modelo-de-dominio]] puertos de
salida).
**Notas de UX**: correo con formato legible, no un volcado JSON crudo.
**Prioridad**: Must.

### HU-19 — Fundador inicia sesión en el panel admin
Como **fundador** quiero **acceder de forma segura al panel admin** para
**gestionar las solicitudes sin exponerlas públicamente**.

- Dado que entro a la URL del panel admin sin sesión iniciada, cuando la
  página carga, entonces se me redirige a un formulario de login y no
  veo ningún dato de solicitudes.
- Dado que ingreso usuario y contraseña correctos, cuando envío el
  formulario de login, entonces accedo al panel y mi sesión se mantiene
  según la configuración de expiración definida.
- Dado que ingreso credenciales incorrectas, cuando intento login,
  entonces veo un mensaje de error genérico (sin indicar si el usuario
  existe o no) y no accedo al panel.

**Reglas de negocio**: usuario único en v1 (sin registro ni recuperación
de contraseña automatizada), gestionado con Spring Security (ver
[[02-arquitectura]] §6).
**Notas de UX**: pantalla de login simple, sin necesidad de branding
elaborado (uso interno).
**Prioridad**: Must.

### HU-20 — Fundador ve el listado de solicitudes
Como **fundador** quiero **ver todas las solicitudes recibidas** para
**hacer seguimiento comercial**.

- Dado que estoy autenticado en el panel admin, cuando entro al
  listado, entonces veo todas las solicitudes ordenadas de más reciente
  a más antigua, con nombre, servicio de interés, estado y fecha.
- Dado que no hay solicitudes todavía, cuando entro al listado, entonces
  veo un estado vacío claro, no una tabla rota o un error.
- Dado que hago clic sobre una solicitud del listado, cuando navego,
  entonces veo su detalle completo (incluye mensaje completo y datos de
  contacto).

**Reglas de negocio**: usa `ListarSolicitudesUseCase` (ver
[[03-modelo-de-dominio]]).
**Notas de UX**: tabla o lista simple, con indicador visual de estado
por color/badge.
**Prioridad**: Must.

### HU-21 — Fundador cambia el estado de una solicitud
Como **fundador** quiero **actualizar el estado de una solicitud** para
**reflejar en qué punto del proceso comercial está**.

- Dado que estoy viendo una solicitud en estado `NUEVA`, cuando elijo
  cambiarla a `CONTACTADA`, entonces el cambio se guarda y se refleja
  inmediatamente en el listado.
- Dado que estoy viendo una solicitud en estado `CONVERTIDA` o
  `DESCARTADA` (terminal), cuando reviso las opciones de cambio de
  estado, entonces no se me ofrece ninguna transición posible (opciones
  deshabilitadas u ocultas).
- Dado que intento forzar una transición inválida (ej. vía API
  directamente, saltándome la UI), cuando el backend la procesa,
  entonces la rechaza con un error claro, respetando la máquina de
  estados del dominio (ver [[03-modelo-de-dominio]] §3).

**Reglas de negocio**: usa `CambiarEstadoSolicitudUseCase`; la UI solo
debe ofrecer transiciones válidas según el estado actual.
**Notas de UX**: cambio de estado con confirmación simple (evitar
cambios accidentales de un clic).
**Prioridad**: Must.

### HU-22 — Fundador filtra solicitudes por estado
Como **fundador** quiero **filtrar el listado por estado** para
**enfocarme en las solicitudes que necesitan acción (NUEVA)**.

- Dado que estoy en el listado, cuando selecciono el filtro "NUEVA",
  entonces solo veo solicitudes en ese estado.
- Dado que aplico un filtro y no hay solicitudes en ese estado, cuando
  el filtro se aplica, entonces veo un estado vacío específico para ese
  filtro (no confundible con "no hay solicitudes en absoluto").
- Dado que quito el filtro, cuando lo hago, entonces vuelvo a ver todas
  las solicitudes sin recargar la página completa.

**Reglas de negocio**: usa `ListarSolicitudesUseCase` con el parámetro
de filtro por `EstadoSolicitud`.
**Notas de UX**: filtro accesible desde el mismo listado, sin pantalla
adicional.
**Prioridad**: Could.

---

## Épica E4 — SEO, rendimiento y accesibilidad

### HU-23 — El sitio es indexable en buscadores
Como **visitante potencial** quiero **encontrar el sitio buscando en
Google** para **poder llegar a él sin conocer la URL de antemano**.

- Dado que un buscador rastrea el sitio, cuando accede a cualquier
  página de contenido público, entonces recibe HTML ya renderizado
  (SSR/prerender), con `<title>` y meta description específicos de esa
  página.
- Dado que un buscador busca el sitemap, cuando accede a `/sitemap.xml`,
  entonces encuentra todas las páginas de contenido público listadas.
- Dado que un buscador respeta `robots.txt`, cuando lo consulta,
  entonces las páginas públicas están permitidas para rastreo y las
  rutas del panel admin están explícitamente deshabilitadas.

**Reglas de negocio**: ninguna página del panel admin debe aparecer en
el sitemap ni ser indexable.
**Notas de UX**: no aplica (SEO técnico).
**Prioridad**: Must.

### HU-24 — Compartir una página con vista previa enriquecida
Como **visitante** quiero **que al compartir un enlace del sitio se vea
una vista previa atractiva** para **generar más clics cuando lo comparto
en redes o WhatsApp**.

- Dado que comparto la URL de la Home en WhatsApp o redes, cuando se
  genera la vista previa, entonces aparece título, descripción e imagen
  Open Graph propios de esa página.
- Dado que comparto la URL de un artículo de blog específico, cuando se
  genera la vista previa, entonces el título e imagen corresponden a ese
  artículo, no a los genéricos del sitio.
- Dado que una página no tiene imagen propia definida, cuando se genera
  su vista previa, entonces usa una imagen Open Graph por defecto en vez
  de mostrar una vista previa rota o vacía.

**Reglas de negocio**: los metadatos Open Graph son parte del esquema de
contenido de cada página (ADR-05).
**Notas de UX**: imagen por defecto con la identidad visual de la marca
(ver [[07-guia-de-estilo]]).
**Prioridad**: Should.

### HU-25 — El sitio carga rápido en móvil
Como **visitante** quiero **que el sitio cargue rápido desde mi celular**
para **no abandonar antes de leer el contenido**.

- Dado que abro cualquier página principal desde un móvil con conexión
  normal, cuando se mide con Lighthouse (modo móvil), entonces el
  puntaje de Performance es ≥ 90.
- Dado que una página incluye imágenes, cuando se sirven, entonces están
  optimizadas (formato moderno, tamaño adecuado, carga diferida donde
  aplica) y no bloquean el renderizado del contenido principal.
- Dado que la página termina de cargar visualmente, cuando el visitante
  intenta interactuar (tocar un botón), entonces la respuesta es
  inmediata, sin bloqueo por hidratación pesada.

**Reglas de negocio**: umbral no negociable definido en
[[01-vision-y-alcance]] criterios de éxito.
**Notas de UX**: no aplica directamente, pero impacta percepción de
seriedad de la empresa.
**Prioridad**: Must.

### HU-26 — Navegar el sitio con teclado y lector de pantalla
Como **visitante con alguna discapacidad o que usa tecnología de
asistencia** quiero **poder navegar todo el sitio sin mouse y con lector
de pantalla** para **acceder al mismo contenido que cualquier otro
visitante**.

- Dado que navego solo con teclado, cuando recorro el sitio con Tab,
  entonces todos los elementos interactivos (enlaces, botones, campos de
  formulario) son alcanzables en un orden lógico, con foco siempre
  visible.
- Dado que uso un lector de pantalla, cuando paso por una imagen o
  ícono con significado, entonces tiene texto alternativo descriptivo
  (no vacío ni "imagen").
- Dado que reviso el contraste de color de texto sobre fondo, cuando se
  mide, entonces cumple al menos el nivel AA de WCAG en todos los
  textos relevantes.
- Dado que lleno el formulario de contacto con un lector de pantalla,
  cuando encuentro un error de validación, entonces el error se anuncia
  y está asociado programáticamente al campo correspondiente (caso
  triste de accesibilidad).

**Reglas de negocio**: umbral Lighthouse Accesibilidad ≥ 90 (ver
[[01-vision-y-alcance]]); checklist detallado en [[06-plan-de-pruebas]].
**Notas de UX**: pensar accesibilidad desde el diseño de componentes
base, no como parche final.
**Prioridad**: Must.

---

## Épica E5 — Infraestructura y despliegue

### HU-27 — Levantar el entorno local con un solo comando
Como **desarrollador** (Crear Code Cesar, incluye a Claude Code en
Etapa 2) quiero **levantar backend, frontend y base de datos localmente
de forma simple** para **empezar a trabajar sin fricción de setup**.

- Dado que tengo Docker instalado, cuando ejecuto `docker compose up`
  sobre el `docker-compose.yml` del monorepo, entonces PostgreSQL queda
  disponible con la base y el usuario esperados por el backend.
- Dado que levanto el backend localmente, cuando arranca, entonces
  Flyway aplica automáticamente las migraciones pendientes sobre la
  base local.
- Dado que un desarrollador nuevo clona el repositorio, cuando sigue las
  instrucciones de `CLAUDE.md`/README, entonces logra tener el stack
  completo corriendo sin pasos no documentados.

**Reglas de negocio**: ninguna credencial real en el `docker-compose.yml`
del repositorio (solo valores de desarrollo local, ver
[[02-arquitectura]] §6).
**Notas de UX**: no aplica (experiencia de desarrollador).
**Prioridad**: Must.

### HU-28 — Verificación automática de calidad en cada cambio
Como **fundador** (dueño del proyecto) quiero **que cada cambio se
verifique automáticamente** para **evitar que se rompa algo sin darnos
cuenta**.

- Dado que se sube un cambio al repositorio, cuando se ejecuta la
  verificación, entonces corren los tests de backend (unitarios, de
  aplicación, ArchUnit) y de frontend (Vitest), fallando la verificación
  si alguno falla.
- Dado que un cambio rompe una regla de arquitectura hexagonal (ej. el
  dominio importa Spring), cuando se ejecuta ArchUnit, entonces la
  verificación falla explícitamente señalando la regla violada.
- Dado que todos los tests pasan, cuando la verificación termina,
  entonces queda un reporte claro de qué se ejecutó y en cuánto tiempo.

**Reglas de negocio**: ninguna fase de la Etapa 2 avanza sin esta
verificación en verde (ver [[06-plan-de-pruebas]] y `CLAUDE.md`).
**Notas de UX**: no aplica.
**Prioridad**: Must.

### HU-29 — Desplegar el sitio en un entorno accesible públicamente
Como **fundador** quiero **poder publicar el sitio cuando esté listo**
para **empezar a recibir visitantes reales**.

- Dado que se completan las fases F0-F6 de la Etapa 2, cuando se evalúa
  el despliegue, entonces existe documentación de al menos dos opciones
  de hosting económico (frontend y backend) con costos estimados,
  incluyendo el dominio (~$60.000-80.000 COP/año).
- Dado que el sitio aún no tiene dominio comprado, cuando se prepara el
  despliegue, entonces la configuración es agnóstica al dominio (ver
  ADR-06 en [[02-arquitectura]]) y solo requiere una variable de entorno
  para apuntar al dominio final.
- Dado que el fundador no ha decidido publicar, cuando termina la fase
  F6, entonces el sitio queda completamente funcional en local/staging
  sin forzar una publicación pública hasta tener el visto bueno explícito
  con números de costo.

**Reglas de negocio**: la decisión de publicar se toma al final, con
costos claros (ver fase F7 en [[05-backlog-issues]]).
**Notas de UX**: no aplica.
**Prioridad**: Should (para v1 completo; no bloquea el resto del
desarrollo).

---

## Épica E6 — Cuentas de cliente (Etapa 3, fase F8)

Épica nueva de la v2 (ver [[10-vision-v2]]): los visitantes pueden
crear una cuenta para acceder a mejores servicios (en F9/F10: límites
mayores del asistente IA y el demo de diseño). Rol nuevo: **cliente**
(persona registrada, sin acceso al panel admin).

### HU-30 — Visitante crea una cuenta de cliente
Como **visitante** quiero **registrarme con mi correo y una contraseña**
para **acceder a los servicios para clientes del sitio**.

- Dado que estoy en la página de registro, cuando ingreso un correo
  válido, una contraseña de al menos 10 caracteres (confirmada dos
  veces) y acepto la política de datos, entonces mi cuenta se crea y
  veo un mensaje claro de que debo verificar mi correo.
- Dado que ingreso un correo con formato inválido o una contraseña
  menor a 10 caracteres, cuando intento registrarme, entonces veo el
  error específico junto al campo y no se crea ninguna cuenta.
- Dado que ya existe una cuenta con mi correo, cuando intento
  registrarme de nuevo, entonces veo un mensaje claro que me ofrece
  ir a iniciar sesión o recuperar mi contraseña.
- Dado que soy un bot haciendo registros masivos, cuando supero el
  límite de intentos, entonces recibo un rechazo (429) sin que el
  sistema envíe más correos.

**Reglas de negocio**: rol `CLIENTE`; la cuenta nace sin verificar y
no puede iniciar sesión hasta verificar el correo (HU-31); correo
único (mismo invariante del contexto `usuarios`, ver
[[03-modelo-de-dominio]] Parte 2); registrar un correo existente
responde 409 explícito — trade-off de usabilidad sobre
anti-enumeración, documentado.
**Notas de UX**: formulario con los mismos patrones del formulario de
contacto (labels visibles, errores asociados al campo); checkbox de
consentimiento no premarcado con enlace a la política.
**Prioridad**: Must (para F8).

### HU-31 — Cliente verifica su correo
Como **cliente recién registrado** quiero **verificar mi correo desde
un enlace que me llega** para **activar mi cuenta y poder ingresar**.

- Dado que me registré, cuando abro el enlace del correo de
  verificación antes de 24 horas, entonces mi cuenta queda verificada
  y puedo iniciar sesión.
- Dado que mi enlace venció o ya fue usado, cuando lo abro, entonces
  veo un mensaje único de "enlace inválido o vencido" (sin distinguir
  la causa) con la opción de reenviar el correo.
- Dado que no me llegó el correo, cuando pido reenviarlo, entonces
  llega un enlace nuevo (que invalida los anteriores) — con un límite
  de reenvíos por correo para evitar abuso.
- Dado que intento iniciar sesión sin verificar, cuando ingreso
  credenciales correctas, entonces veo un mensaje claro de que debo
  verificar primero, con la opción de reenviar el correo.

**Reglas de negocio**: token de un solo uso, vigencia 24 h, se guarda
solo su hash (nunca el valor en claro); máximo 3 reenvíos por correo
cada 15 minutos (control en la capa de aplicación, por correo — el
límite por IP es solo respaldo).
**Notas de UX**: la página de verificación actúa sola al abrirla (sin
botones extra en el caso feliz).
**Prioridad**: Must (para F8).

### HU-32 — Cliente recupera su contraseña
Como **cliente** quiero **restablecer mi contraseña si la olvidé**
para **volver a acceder a mi cuenta sin intervención manual**.

- Dado que olvidé mi contraseña, cuando ingreso mi correo en la página
  de recuperación, entonces veo siempre el mismo mensaje genérico ("si
  el correo existe, te llegará un enlace") — exista o no la cuenta.
- Dado que recibí el enlace, cuando lo abro antes de 1 hora y escribo
  una contraseña nueva válida (mínimo 10 caracteres, confirmada),
  entonces mi contraseña cambia y puedo iniciar sesión con ella.
- Dado que el enlace venció o ya fue usado, cuando intento usarlo,
  entonces veo "enlace inválido o vencido" y puedo pedir uno nuevo.

**Reglas de negocio**: token de un solo uso, vigencia 1 h, solo hash
persistido; restablecer la contraseña también marca la cuenta como
verificada (probó ser dueño del correo); los JWT ya emitidos siguen
vigentes hasta su expiración natural (sin denylist, coherente con
ADR-08 — riesgo aceptado y documentado); mismo límite de 3 solicitudes
por correo cada 15 minutos.
**Notas de UX**: nunca revelar si el correo existe; la pantalla de
éxito invita a revisar también la carpeta de spam.
**Prioridad**: Must (para F8).

### HU-33 — Cliente ingresa y ve su cuenta
Como **cliente verificado** quiero **iniciar sesión y ver mi área de
cuenta** para **confirmar mi identidad y acceder a lo que es mío**.

- Dado que estoy verificado, cuando ingreso credenciales correctas en
  la página de ingreso, entonces entro a "Mi cuenta" y veo mi correo.
- Dado que ingreso credenciales incorrectas, cuando envío el
  formulario, entonces veo el mismo mensaje genérico de siempre
  ("correo o contraseña incorrectos") sin revelar cuál falló.
- Dado que soy el fundador (rol ADMIN), cuando ingreso por la página
  de clientes, entonces se me redirige al panel admin.
- Dado que intento abrir "Mi cuenta" sin sesión, cuando navego a esa
  ruta, entonces se me redirige a la página de ingreso.
- Dado que cierro sesión, cuando vuelvo a "Mi cuenta", entonces se me
  pide ingresar de nuevo.

**Reglas de negocio**: mismo endpoint de login y mismo JWT de ADR-08
(el token ya lleva el claim `rol`); un token de rol CLIENTE recibe 403
en los endpoints del panel admin; la sesión vive en `sessionStorage`
(se pierde al cerrar la pestaña, trade-off de ADR-08).
**Notas de UX**: "Mi cuenta" mínima en F8 (correo + cerrar sesión);
sin cambio de contraseña autenticado — se cubre con la recuperación
(HU-32). El header del sitio muestra "Ingresar" o "Mi cuenta" según
la sesión.
**Prioridad**: Must (para F8).
