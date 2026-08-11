# 02 — Arquitectura

## 1. Diagrama de la solución

```
                         ┌─────────────────────────────┐
                         │        Visitante web         │
                         └──────────────┬───────────────┘
                                        │ HTTPS
                                        ▼
                    ┌───────────────────────────────────────┐
                    │     frontend — Angular 22 (SSR)        │
                    │  standalone components · signals       │
                    │  contenido desacoplado (data/*.ts/.md)  │
                    └──────────────────┬──────────────────────┘
                                        │ REST/JSON (fetch)
                                        ▼
                    ┌───────────────────────────────────────┐
                    │  backend — Spring Boot 4.1 / Java 25   │
                    │                                         │
                    │  infraestructura/rest  (controladores)  │
                    │            │                            │
                    │            ▼                            │
                    │  aplicacion/  (casos de uso,             │
                    │                @Transactional)           │
                    │            │                            │
                    │            ▼                            │
                    │  dominio/  (modelo + puertos,             │
                    │             sin Spring ni JPA)            │
                    │            ▲                            │
                    │            │ implementa puertos           │
                    │  infraestructura/persistencia (JPA+mapper)│
                    │  infraestructura/notificacion (correo)    │
                    └──────────────────┬──────────────────────┘
                                        │
                          ┌─────────────┴─────────────┐
                          ▼                           ▼
                 ┌─────────────────┐         ┌──────────────────┐
                 │  PostgreSQL      │         │  Servidor SMTP    │
                 │  (Flyway)        │         │  (notificación)   │
                 └─────────────────┘         └──────────────────┘
```

El panel admin usa el mismo backend (endpoints protegidos con Spring
Security) y se sirve desde el mismo frontend Angular, en rutas separadas
del contenido público.

## 2. Estructura de carpetas del monorepo

```
web-empresa/
├── docs/                      # Esta documentación (fuente de verdad viva)
├── frontend/                  # Angular 22, SSR/prerender
│   ├── src/
│   │   ├── app/
│   │   │   ├── paginas/       # Componentes de página (standalone)
│   │   │   ├── componentes/   # UI reutilizable (cards, CTA, formulario)
│   │   │   ├── admin/         # Panel admin (rutas protegidas)
│   │   │   ├── api/           # Clientes HTTP tipados por recurso (SolicitudesApi, AuthApi)
│   │   │   └── nucleo/        # Sesión, guards, interceptores (ver ADR-08)
│   │   ├── contenido/         # Datos/markdown centralizados (ver ADR-05)
│   │   └── ...
│   └── ...
├── backend/                   # Spring Boot 4.1, Java 25, Maven
│   └── src/main/java/com/crearcode/leads/
│       ├── dominio/           # Modelo + puertos — sin Spring ni JPA
│       ├── aplicacion/        # Casos de uso — @Transactional aquí
│       └── infraestructura/
│           ├── rest/          # Controladores REST, DTOs, mapeo HTTP
│           ├── persistencia/  # Entidades JPA, repos Spring Data, mappers
│           ├── notificacion/  # Adaptador de correo (implementa NotificadorPort)
│           └── seguridad/     # Configuración Spring Security (panel admin)
├── docker-compose.yml         # PostgreSQL local
└── CLAUDE.md
```

## 3. Arquitectura hexagonal del backend

Paquete raíz `com.crearcode.leads`, con tres capas y una única dirección
de dependencia permitida: **infraestructura → aplicación → dominio**.
Nunca al revés.

- **`dominio/`**: modelo (`SolicitudDeContacto`, VOs, `EstadoSolicitud`)
  y puertos (interfaces de entrada y salida). No importa nada de
  `org.springframework.*` ni `jakarta.persistence.*`. Es Java puro,
  testeable sin levantar contexto de Spring.
- **`aplicacion/`**: implementa los casos de uso (puertos de entrada)
  orquestando el dominio y los puertos de salida. Aquí vive
  `@Transactional`. Depende de `dominio/`, nunca de
  `infraestructura/`.
- **`infraestructura/`**: adaptadores concretos.
  - `rest/`: controladores Spring MVC que traducen HTTP ↔ casos de uso.
  - `persistencia/`: entidades JPA (distintas de las del dominio) +
    mappers explícitos hacia/desde el modelo de dominio + implementación
    de `SolicitudRepositorio`.
  - `notificacion/`: implementación de `NotificadorPort` con
    `JavaMailSender` o equivalente.
  - `seguridad/`: configuración de Spring Security para el panel admin.

### Regla de dependencias verificada con ArchUnit

Desde el primer commit de código (Etapa 2, fase F0) existe un test
ArchUnit que falla el build si:
1. Cualquier clase en `dominio/` importa algo de `org.springframework.*`
   o `jakarta.persistence.*`.
2. Cualquier clase en `dominio/` depende de una clase en
   `infraestructura/`.
3. Cualquier clase en `aplicacion/` depende de una clase en
   `infraestructura/` (debe depender solo de las interfaces de puerto,
   definidas en `dominio/`).

Este test es en sí mismo un issue temprano del backlog (ver
[[05-backlog-issues]], fase F0) y debe estar en verde antes de escribir
cualquier caso de uso.

## 4. Estructura del frontend (Angular 22)

- **Standalone components** en todas partes, sin `NgModule`.
- **Zoneless** (sin Zone.js) — el estado reactivo se maneja con
  `signal`/`computed`/`effect`, no con detección de cambios basada en
  monkey-patching de APIs del navegador.
- **Contenido desacoplado**: los textos de cada página (headlines,
  descripciones de servicio, FAQs, casos, artículos de blog) viven en
  `frontend/src/contenido/` como archivos de datos TypeScript tipados o
  Markdown, no incrustados en los componentes. Publicar o corregir un
  texto es editar un archivo de contenido, no un componente (ver
  ADR-05 y ADR-06 más abajo, y [[01-vision-y-alcance]] criterio de éxito
  de tiempo de publicación).
- **Signal Forms** para el formulario de contacto (validación reactiva
  basada en signals, sin `ReactiveFormsModule` clásico).
- **SSR + prerender**: páginas de contenido estático (Home, servicios,
  sobre nosotros, legales) se prerrenderizan en build; páginas con datos
  dinámicos (panel admin) usan SSR o quedan como client-side detrás de
  login, ya que no necesitan SEO.
- **Vitest** como test runner (reemplazo oficial de Karma en el
  ecosistema Angular).

## 5. Decisiones de arquitectura (ADRs breves)

### ADR-01 — Angular 22 zoneless / signals-first
**Decisión**: usar el modo zoneless (sin Zone.js) y signals como modelo
de reactividad por defecto, evitando patrones legacy (`ngOnChanges`
extensivo, `BehaviorSubject` para estado simple de UI).
**Motivo**: es el camino por defecto en proyectos Angular nuevos desde
esta versión; reduce overhead de detección de cambios (relevante en
Lighthouse Performance) y deja un código más simple de leer para el
público técnico que revise el sitio como vitrina.
**Consecuencia**: cualquier librería de terceros que dependa de Zone.js
queda descartada salvo justificación explícita.

### ADR-02 — SSR/prerender obligatorio
**Decisión**: todas las páginas de contenido público se sirven
prerrenderizadas o vía SSR, nunca solo client-side rendering.
**Motivo**: requisito no negociable de SEO (el público busca en Google
términos como "desarrollo de software Valledupar"); un sitio SPA sin SSR
pierde indexación y empeora Core Web Vitals.
**Consecuencia**: el build de frontend incluye un paso de
prerender/SSR; cualquier página nueva de contenido debe registrarse en
las rutas prerenderizables.

### ADR-03 — Hexagonal en el backend pese a un dominio pequeño
**Decisión**: aplicar arquitectura hexagonal estricta (dominio aislado,
puertos, ArchUnit) aunque el contexto `leads` tenga un solo agregado.
**Motivo**: dos razones. (1) El sitio es vitrina técnica de Crear Code
Cesar frente a su público de TI — debe demostrar la misma disciplina que
la empresa promete en proyectos grandes. (2) El dominio va a crecer
(casos/portafolio dinámico, blog con backend propio, portal de clientes
en v2); empezar desacoplado evita una migración dolorosa después.
**Consecuencia**: más archivos y ceremonia que un CRUD directo con JPA;
se acepta ese costo a cambio de las dos razones anteriores.

### ADR-04 — Java 25 (LTS) + virtual threads
**Decisión**: usar Java 25 sobre la versión estable más reciente de
Spring Boot compatible, aprovechando virtual threads para I/O
concurrente (peticiones REST, envío de correo) y las mejoras de
arranque/memoria del JDK pensando en contenedores pequeños de bajo
costo.
**Motivo**: es la LTS vigente más reciente al iniciar el proyecto; los
virtual threads simplifican el modelo de concurrencia sin reescribir a
reactivo, y un footprint de memoria menor reduce el costo de hosting en
la fase de despliegue (ver [[05-backlog-issues]] fase F7).
**Consecuencia**: Lombok, ArchUnit y plugins de Maven deben verificarse
compatibles con JDK 25 al generar el proyecto en la Etapa 2 (issue
explícito en fase F0); versiones desactualizadas de estas librerías
fallan en JDKs nuevos.

### ADR-07 — Spring Boot 4.1.x en vez de Spring Boot 3
**Decisión**: usar **Spring Boot 4.1.x** (versión estable actual) en
vez de Spring Boot 3.x, que era la elección original documentada al
cierre de la Etapa 1.
**Motivo**: al iniciar la Etapa 2 (16 jul 2026) se verificó contra
Maven Central y las notas oficiales de Spring que **3.5.16** —la
última versión de la serie 3.x— fue también su última versión con
soporte OSS: la generación 3.x llegó a fin de soporte comunitario el
30 de junio de 2026, dos semanas antes de iniciar el desarrollo. Spring
Boot 4.1.x es la serie con soporte activo, exige Java 17 como mínimo y
tiene compatibilidad verificada con Java 25 (incluida compilación
nativa). Decisión confirmada explícitamente por el usuario ante esta
disyuntiva (no se asumió).
**Consecuencia**: cualquier mención previa a "Spring Boot 3" en esta
documentación se actualiza a Spring Boot 4.1.x. El impacto en la
arquitectura hexagonal es mínimo: `dominio/` no depende de Spring en
ningún caso (ver regla de dependencias, sección 3), así que el cambio
de versión solo afecta a `infraestructura/` (namespaces, configuración,
posibles APIs renombradas de Spring Framework 7) y no al modelo de
dominio ya documentado en [[03-modelo-de-dominio]].

### ADR-05 — Contenido desacoplado de los componentes
**Decisión**: todo texto editorial (no transaccional) vive en archivos
de datos/Markdown separados de los componentes Angular.
**Motivo**: el criterio de éxito de "tiempo de publicación de contenido"
([[01-vision-y-alcance]]) exige que publicar un caso o artículo no
dependa de un desarrollador tocando un componente.
**Consecuencia**: se define un esquema de datos tipado para cada tipo de
contenido (servicio, caso, artículo) antes de escribir los componentes
que lo consumen — este esquema se detalla como issue temprano en fase F3.

### ADR-06 — Diseño agnóstico al dominio web
**Decisión**: nada en la arquitectura (URLs absolutas, metadatos Open
Graph) asume un dominio específico; todo se parametriza vía variables
de entorno/configuración. (No hace falta CORS: ver ADR-09.)
**Motivo**: el dominio aún no está comprado; el desarrollo es 100% local
y la decisión de despliegue se toma en la fase F7 de la Etapa 2 (ver
[[01-vision-y-alcance]] pendientes).
**Consecuencia**: cualquier valor que dependa del dominio final
(`baseUrl`, sitemap, OG:url) se lee de configuración, nunca hardcodeado.

### ADR-08 — Autenticación del panel admin con JWT autoemitido (no HTTP Basic desde el frontend)

**Decisión**: el backend deja de exponer HTTP Basic hacia el panel
admin. Pasa a tener un endpoint `POST /api/auth/login` que valida
credenciales contra un `Usuario` persistido y emite un JWT (HS256,
autoemitido y autovalidado con `spring-boot-starter-oauth2-resource-server`
— sin Authorization Server externo, sin librería JWT de terceros). El
resto de endpoints protegidos pasan de `.httpBasic(...)` a
`.oauth2ResourceServer().jwt(...)`, verificando el `Bearer <token>` en
cada petición. El token lleva un claim `rol` (hoy solo `ADMIN`) y un
claim `jti`, pensado para una futura revocación por denylist que **no**
se construye todavía (ver [[03-modelo-de-dominio]] §7 y
[[05-backlog-issues]] fase F5).
**Motivo**: el diseño original (F2) usaba HTTP Basic *stateless* porque
había un único usuario hardcodeado sin necesidad real de sesión. Al
iniciar F5 el usuario explicitó que la aplicación va a evolucionar
hacia una herramienta de gestión multi-empleado con roles (y,
eventualmente, facturación/contabilidad), y pidió explícitamente un
sistema de autenticación robusto desde el principio — aunque hoy siga
habiendo un solo usuario. Guardar usuario+contraseña en el navegador
(la alternativa más simple, reusar HTTP Basic desde el frontend) no es
compatible con esa dirección: no hay expiración real, no hay noción de
"sesión", y no escala a roles/permisos por usuario. Decisión tomada
explícitamente por el usuario ante esta disyuntiva (ver
[[05-backlog-issues]] fase F5), no asumida.
**Consecuencia**: se reabre parte del diseño de seguridad cerrado en
F2 — nuevo agregado `Usuario`/`Rol` en `dominio/` (mismo patrón de
puertos/adaptadores que `SolicitudDeContacto`), nueva tabla `usuarios`
vía Flyway, y `ADMIN_USERNAME` pasa de ser un usuario cualquiera a
tener que ser un correo válido (se reusa el VO `Correo`). Sin refresh
tokens ni denylist de revocación en v1: el logout es responsabilidad
del cliente (borra el token) y el token sigue siendo técnicamente
válido hasta su expiración (configurable, default 8h) — trade-off
consciente para no sobreconstruir con un solo usuario real hoy.

> **Actualización F8 (Etapa 3, cuentas de cliente)**: la apuesta de
> este ADR se materializa — `Rol` gana `CLIENTE`, el claim `rol` del
> JWT pasa a usarse en autorización real (`hasRole("ADMIN")` en los
> endpoints del panel: un token CLIENTE recibe 403) y el login exige
> cuenta verificada por correo. Sigue sin denylist: consecuencia
> nueva, aceptada y documentada, es que restablecer la contraseña no
> revoca los JWT ya emitidos (hasta 8 h de vida restante). Detalle del
> modelo en [[03-modelo-de-dominio]] Parte 2.

### ADR-09 — Proxy servidor-a-servidor en el frontend en vez de CORS

**Decisión**: el servidor SSR del frontend (`frontend/src/server.ts`)
reenvía todo lo que llega a `/api/**` hacia el backend real (variable
de entorno `BACKEND_URL`), vía `http-proxy-middleware`. El navegador
nunca llama al backend directamente — solo al frontend, que hace de
intermediario. El backend no tiene ninguna configuración CORS.
**Motivo**: en la fase F7 ([[09-despliegue]], ISS-079/080) el usuario
eligió desplegar en Render (capa gratis) + Neon, lo que pone al
backend y al frontend en dos orígenes distintos. La alternativa directa
(CORS en el backend + `SolicitudesApi`/`AuthApi` armando URLs absolutas
al backend) exigía dos cosas nuevas: configuración CORS desde cero
(hoy inexistente) y alguna forma de inyectar la URL del backend en el
bundle del navegador en build time — con el riesgo de mezclar
`process.env` en código que se empaqueta para el browser. El proxy
evita ambas cosas: `SolicitudesApi`/`AuthApi` siguen usando rutas
relativas (`/api/...`) sin cambios, exactamente como ya lo hacían
contra el proxy de `ng serve` en desarrollo local, y `BACKEND_URL` se
lee en `server.ts`, que corre en Node real (sin trucos de bundling).
**Consecuencia**: toda petición a la API pasa por un salto de red
adicional (navegador → frontend → backend) — aceptable para el
volumen de tráfico esperado de un sitio de captación de leads. El
frontend queda como único punto de entrada público de la API.

### ADR-10 — Asistente IA detrás de un puerto de dominio, proveedor Groq (fase F9)

**Decisión**: el asistente de la fase F9 sigue la misma arquitectura
hexagonal del resto del backend: el dominio define el puerto
`GeneradorDeRespuestas` (y los VOs de la conversación); la
infraestructura implementa el adaptador de Groq (API compatible con
OpenAI, modelo `llama-3.3-70b-versatile`), con la URL base y la key
por variables de entorno (`GROQ_API_KEY`, nunca en el repo ni en el
navegador — el flujo es navegador → proxy `/api` del frontend (ADR-09)
→ backend → Groq). El **prompt de sistema se ancla al contenido real
del sitio**: vive como recurso del backend
(`asistente-contexto.md`, mantenido a mano desde [[08-contenido]]) con
la regla dura de **nunca inventar precios ni promesas** — en la prueba
real del 20 jul 2026 el modelo se inventó "paquetes desde $500.000
COP"; sin anclaje eso pasaría en producción. Cuando la pregunta sale
del contexto (o pide precio/cotización), el asistente escala al
humano: CTA de WhatsApp o formulario de contacto.
**Motivo**: cambiar de proveedor de IA (Groq hoy por su capa gratis;
mañana cualquier otro) debe ser escribir otro adaptador, con cero
cambios en dominio/aplicación — igual que con el correo o la
persistencia. La duplicación controlada del contenido en el backend
(en vez de leer `contenido/` del frontend) evita acoplar el backend al
código TypeScript; follow-up documentado: generar ese recurso en build.
**Consecuencia**: los límites de uso son parte del diseño (la capa
gratis de Groq es ~1.000 peticiones/día): límite global diario en la
capa de aplicación (el techo real), límite por usuario registrado, y
límite blando por sesión anónima — el límite por IP no sirve de
protección fina porque en producción todas las peticiones comparten la
IP del proxy (lección de F8). Sin memoria entre sesiones ni
function-calling en v1.

### ADR-11 — Dominio canónico crearcodecesar.com detrás de Cloudflare (10 ago 2026)

**Decisión**: el dominio canónico del sitio es
**`https://crearcodecesar.com`** (sin `www`); `www.crearcodecesar.com`
redirige 301 en Cloudflare, que actúa como DNS/proxy delante del
servicio de Render. `*.onrender.com` sigue funcionando durante la
transición (queda en `NG_ALLOWED_HOSTS` junto al dominio nuevo). La
fuente única del dominio en el código es `BASE_URL` en
`contenido/sitio.ts` (ADR-06) — alimenta sitemap, robots, `og:url`,
`og:image` y la nueva `<link rel="canonical">`; va como constante y no
como variable de entorno porque las metas se hornean en el prerender
(build), no en runtime. Los enlaces de los correos de cuenta usan
`FRONTEND_URL` en Render, que pasa de la URL de onrender al dominio
canónico.
**HSTS — decisión revisada el 11 ago 2026**: la emite **la propia
aplicación** (el servidor Express del SSR), no Cloudflare. La versión
original de este ADR la delegaba en Cloudflare; el motivo del cambio es
que **esa delegación daba por hecho que la zona propia estaría
proxeada, y ponerla en proxy tiene un costo real**: Render ya sirve el
sitio detrás de su propio Cloudflare (se ve en las respuestas:
`x-render-origin-server: cloudflare`), así que encadenar nuestro proxy
sobre el suyo añade un salto que no aporta y, sobre todo, **interfiere
con la validación ACME de los certificados de Render**, que necesita
alcanzar el origen; una renovación fallida tumba el sitio entero por
una cabecera. Emitirla en Express la deja bajo nuestro control y
versionada con el código, funcione o no el proxy de la zona.

Detalles de la implementación (`frontend/src/servidor/hsts.ts`, montado
en `server.ts` **antes** del proxy `/api`, de los estáticos y del router
de Angular, para que cubra también `/monday-app-association.json`):

- **Valor**: `max-age=<HSTS_MAX_AGE>; includeSubDomains`, con **86400
  (un día) por defecto**. Arranque conservador y deliberado: si algo
  sale mal con un subdominio o un certificado, el compromiso con los
  navegadores caduca en 24 h en vez de en un año. El `max-age` sale de
  variable de entorno justamente para subirlo desde el dashboard, sin
  desplegar código.
- **Sin `preload`**: entrar en la lista de precarga de los navegadores
  es en la práctica irreversible (salir tarda meses en propagarse a las
  versiones instaladas). Solo se añade cuando el `max-age` lleve meses
  en 31536000 y el dominio esté estable.
- **Solo sobre HTTPS**: se mira `x-forwarded-proto` (quedándose con el
  primer salto, que es el del cliente). En local se sirve por http, y
  emitirla ahí dejaría el navegador forzando https contra `localhost`,
  rompiendo el desarrollo.

Spring Security sigue sin competir: su HSTS por defecto solo aplica a
peticiones HTTPS y el backend nunca habla con el navegador — solo
recibe HTTP interno del proxy SSR (ADR-09).
**CORS**: el cambio de dominio NO requiere configurar CORS — por
ADR-09 el navegador solo conoce un origen (el frontend) y `/api` se
reenvía por dentro; agregar una allowlist de orígenes sería
configuración muerta. Si algún día el navegador llamara al backend
directo, ahí se crea `app.cors.allowed-origins` por variable de
entorno.
**Consecuencia**: `render.yaml` agrega los dominios a
`NG_ALLOWED_HOSTS` (sin esto, Angular SSR respondería 400 al dominio
nuevo — protección SSRF) y fija `FRONTEND_URL`; el archivo estático
`/monday-app-association.json` (requisito del marketplace de
monday.com) vive en `frontend/public/` y lo sirve Express antes del
router de Angular, sin autenticación.

## 6. Seguridad (resumen, detalle en épica E3)

- Panel admin protegido con Spring Security vía JWT (ver ADR-08):
  login emite el token, el resto de endpoints protegidos lo validan.
  Usuario único en v1, pero persistido (no hardcodeado) y con un campo
  de rol pensado para cuando existan más usuarios/responsabilidades.
- Secretos (credenciales de correo, credenciales de BD, credenciales de
  admin, secreto de firma JWT) solo por variables de entorno — nunca en
  el repositorio.
- Sin datos personales (nombre, correo, teléfono de leads) en logs ni en
  URLs — los identificadores de solicitud en rutas son el `SolicitudId`
  (UUID), no el correo ni el nombre.
