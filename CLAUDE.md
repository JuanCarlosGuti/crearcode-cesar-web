# CLAUDE.md — Crear Code Cesar S.A.S. · Sitio web corporativo

Resumen operativo del proyecto. La fuente de verdad viva es la carpeta
[`docs/`](docs/) — si una decisión no está documentada allí, no se
asume: se pregunta al usuario y se actualiza el documento
correspondiente antes de seguir.

## Estado del proyecto

**Etapa actual: Etapa 3 — Plataforma v2. La fase F8 (cuentas de
cliente) está TERMINADA y APROBADA por el usuario (28 jul 2026;
ISS-083 a ISS-100, suites backend/frontend/e2e en verde + verificación
manual en navegador). *(Su correo quedó funcionando en producción el 11
ago 2026, por Resend — ver §Decisiones ya resueltas.)* La fase F8.5 —
rediseño visual y valor de la cuenta
(ISS-101 a ISS-107) — está TERMINADA y APROBADA (28 jul 2026;
Lighthouse 97-98/100/100/100, 21/21 e2e con axe, verificación manual).
La fase F9 — asistente IA con Groq (ISS-108 a ISS-118) — está
TERMINADA, APROBADA y PUBLICADA (28 jul 2026), verificada con Groq
real en local (respuestas ancladas, sin inventar precios,
escalamiento funcionando). El asistente quedó verificado EN PRODUCCIÓN
(28 jul 2026: pregunta de precio respondida sin cifras inventadas y con
escalamiento), y su `GROQ_API_KEY` vive hoy como secreto del
repositorio.

La fase F10 — **Centro de herramientas con IA** (ISS-119 a ISS-137) —
está TERMINADA, APROBADA por el usuario y PUBLICADA (10 ago 2026).
Incluye el cotizador y la página `/herramientas` viva (F10a), el
simulador de chatbot (F10b), el diagnóstico digital (F10c), el demo de
diseño con IA para cuentas registradas (F10d) y el rediseño de Home,
páginas de servicio y header según el prototipo aprobado (F10e).
Verificación de cierre (ISS-132): backend en verde (92 ITs + ArchUnit),
215 specs de frontend, 36 e2e con axe sin violaciones, Lighthouse
97-98/100/100/100 sobre el build de producción en Home, servicio,
`/herramientas` y Contacto, revisión manual en 375 y 1280 px, y prueba
real en producción del asistente (respuesta anclada de Groq y pregunta
de precio escalada sin inventar cifras). El mismo día se compró y
migró el dominio propio (ADR-11) y quedó implementado el proveedor de
imágenes con respaldo (ISS-137, Cloudflare Workers AI + Pollinations),
verificado con credenciales reales.

La fase F11 — **gestión comercial interna** (cotizaciones, ISS-138 a
ISS-155) — está TERMINADA y APROBADA por el usuario (11 ago 2026): el
pipeline lead → cotización → aceptada, con PDF generado por la app y
respuesta del cliente desde su cuenta. Al descomponerla apareció que la
"cuenta de cobro" no era viable para una S.A.S. (decisión 18), así que
la fase se reenfocó en cotizaciones; el certificado de Cámara de
Comercio que el usuario aportó lo confirma.

**Con esto termina la Etapa 3.** Lo siguiente no es una fase nueva sino
el pulido previo a las pruebas del MVP, con los pendientes listados
abajo.

La v1 está PUBLICADA en producción desde el 27 jul
2026, y desde el 19 ago 2026 corre en **servidor propio** (VPS de
Netcup en Virginia, desplegado con Kamal 2 desde GitHub Actions), no en
Render — ver ADR-13.**

Producción:
- **Todo vive en https://crearcodecesar.com**, un solo host. Caddy
  sirve el sitio prerenderizado; `kamal-proxy` entrega `/api` y
  `/actuator` al backend por prefijo de ruta. El navegador ve un solo
  origen, así que el proyecto sigue sin CORS (ADR-09). `www` redirige
  301 al dominio raíz desde el `Caddyfile`.
- La base de datos es el **PostgreSQL compartido del servidor** (rol y
  base `crearcodecesar`), no Neon.
- Cloudflare es solo el DNS del dominio — no hay proxy delante, así que
  tampoco hay CDN. El TLS lo emite Let's Encrypt vía kamal-proxy.

Los servicios de Render siguen encendidos sin dominio apuntándoles,
como vuelta atrás; se apagan cuando el usuario lo decida (ver
[docs/09-despliegue.md](docs/09-despliegue.md) §9 paso 6).

El usuario aprobó la documentación el 16 jul 2026 con la frase
"APRUEBO LA DOCUMENTACIÓN, ARRANCA LA FASE 1". Regla dura: no se avanza
de una fase a la siguiente sin tests en verde, ArchUnit en verde y el
OK explícito del usuario para esa fase concreta.

## Qué es este proyecto

Sitio web corporativo de **Crear Code Cesar S.A.S.**, empresa
colombiana de servicios de software (Valledupar, Cesar — operación
nacional). Tres líneas de negocio: desarrollo de software a la medida,
IA/automatización para pymes, y soluciones tecnológicas (cobro digital,
integraciones, modernización). El sitio es tanto una herramienta de
captación de leads como una vitrina de la forma de trabajar de la
empresa (documentar → probar → construir).

Ver el detalle completo en [docs/01-vision-y-alcance.md](docs/01-vision-y-alcance.md).

## Stack

- **Backend**: Java 25 (LTS) + Spring Boot 4.1.x + Maven. Arquitectura
  hexagonal en `com.crearcode.leads` con `dominio/` (sin Spring/JPA),
  `aplicacion/` (casos de uso, `@Transactional`), `infraestructura/`
  (REST, persistencia JPA, notificación). Dominio en español. Reglas de
  dependencia verificadas con ArchUnit desde el primer commit. Virtual
  threads para I/O concurrente.
- **Base de datos**: PostgreSQL + Flyway, vía Docker Compose local.
- **Frontend**: Angular 22, signals-first, **zoneless** (sin Zone.js),
  componentes standalone, Signal Forms para el formulario de contacto,
  Vitest como test runner, SSR/prerender habilitado. Contenido editorial
  desacoplado de los componentes (archivos de datos/Markdown en
  `contenido/`).
- **Seguridad**: panel admin con Spring Security (usuario único en v1),
  secretos solo por variables de entorno, sin datos personales en logs
  ni URLs.

Detalle completo, diagrama y ADRs en
[docs/02-arquitectura.md](docs/02-arquitectura.md). Modelo de dominio en
[docs/03-modelo-de-dominio.md](docs/03-modelo-de-dominio.md).

## Cómo trabajar conmigo (el usuario)

- **Responder siempre en español.**
- **Plan antes de cambios grandes**: cualquier issue no trivial se
  plantea primero (qué se va a hacer y por qué) antes de tocar código.
- **Diffs siempre visibles**: el usuario revisa cada cambio antes de
  darlo por bueno; no se agrupan múltiples issues en un solo cambio sin
  avisar.
- **Incrementos pequeños**: un issue del backlog ≈ una unidad de trabajo
  con tests primero (TDD), implementación, verificación en verde y
  commit descriptivo propio.
- **TDD real**: test que falla → implementación mínima → refactor. No se
  escribe implementación sin su test correspondiente ya escrito.
- **Nunca avanzar de fase (F0→F7) sin**: tests en verde, ArchUnit en
  verde, y el OK explícito del usuario.
- **Decisiones no documentadas**: si algo no está en `docs/`, se
  pregunta antes de asumir, y luego se actualiza el documento afectado
  — la documentación es la fuente de verdad viva, no un artefacto
  congelado en la Etapa 1.
- Convenciones de commits, nombres y estilo de código en
  [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md).

## Mapa de la documentación

| Documento | Contenido |
|---|---|
| [01-vision-y-alcance.md](docs/01-vision-y-alcance.md) | Objetivo, públicos, mensajes clave, alcance v1 vs. v2, criterios de éxito, pendientes |
| [02-arquitectura.md](docs/02-arquitectura.md) | Diagrama, estructura de carpetas, reglas hexagonales, ADRs |
| [03-modelo-de-dominio.md](docs/03-modelo-de-dominio.md) | Entidades, VOs, máquina de estados, puertos, invariantes |
| [04-historias-de-usuario.md](docs/04-historias-de-usuario.md) | Todas las HU por épica (E1-E5), formato Dado/Cuando/Entonces |
| [05-backlog-issues.md](docs/05-backlog-issues.md) | Issues técnicos ISS-NNN por fase (F0-F7), con tests nombrados |
| [06-plan-de-pruebas.md](docs/06-plan-de-pruebas.md) | Estrategia TDD, pirámide de pruebas, umbrales de cobertura, checklist de accesibilidad |
| [07-guia-de-estilo.md](docs/07-guia-de-estilo.md) | Convenciones de código y guía visual (paleta, tipografía, componentes) |
| [08-contenido.md](docs/08-contenido.md) | Todos los textos del sitio en borrador |
| [09-despliegue.md](docs/09-despliegue.md) | Opciones de hosting y dominio comparadas con costos, recomendación, checklist técnico pendiente (fase F7) |
| [10-vision-v2.md](docs/10-vision-v2.md) | Visión v2 / Etapa 3 (fases F8-F11): cuentas de cliente, asistente IA (Groq), demo de diseño, gestión interna — pendiente de aprobación explícita |

## Checklist de fases (Etapa 2 — actualizar a medida que avance)

- [x] **F0** — Esqueleto monorepo (Spring Boot JDK 25 + Angular 22 CLI) + CI + ArchUnit + healthcheck
- [x] **F1** — Dominio `leads` con tests (TDD, sin Spring)
- [x] **F2** — API + persistencia (casos de uso, JPA, REST, seguridad, honeypot, rate limiting)
- [x] **F3** — Frontend: estructura y páginas con contenido
- [x] **F4** — Formulario end-to-end con Signal Forms + notificaciones
- [x] **F5** — Panel admin (autenticación JWT, no HTTP Basic — ver ADR-08)
- [x] **F6** — SEO, rendimiento y accesibilidad (Lighthouse 98-99 Performance, 100 Accesibilidad/Buenas Prácticas/SEO)
- [ ] **F7** — Despliegue (costos de hosting + dominio, decisión final con el usuario)

Detalle de issues por fase en
[docs/05-backlog-issues.md](docs/05-backlog-issues.md).

**Etapa 3 (v2, fases F8-F11)** — planificada en
[docs/10-vision-v2.md](docs/10-vision-v2.md) a pedido del usuario
(20 jul 2026): F8 cuentas de cliente → F9 asistente IA (Groq) → F10
demo de diseño con IA → F11 gestión interna (cotizaciones/cuentas de
cobro, sin DIAN al inicio). La v1 se publicó el 27 jul 2026 y el
usuario aprobó el documento ese mismo día.
La `GROQ_API_KEY` vive solo en el `.env` local (gitignored) y como
variable de entorno del servidor el día que se use — nunca en el repo.

- [x] **F8** — Cuentas de cliente (ISS-083 a ISS-100): terminada,
  aprobada y en producción; su correo espera las variables `MAIL_*` en
  Render (pausado a pedido del usuario).
- [x] **F8.5** — Rediseño visual y valor de la cuenta (ISS-101 a
  ISS-107): terminada y aprobada (28 jul 2026).
- [x] **F9** — Asistente IA (Groq, ISS-108 a ISS-118): terminada,
  aprobada y publicada (28 jul 2026); su `GROQ_API_KEY` espera en el
  servidor propio (secretos del repositorio en GitHub).
- [x] **F10** — Centro de herramientas con IA (ISS-119 a ISS-137):
  terminada, aprobada y publicada (10 ago 2026). F10a cotizador +
  /herramientas viva → F10b simulador de chatbot → F10c diagnóstico
  digital → F10d demo de diseño → F10e rediseño Home/servicios/header
  según el prototipo aprobado (decisiones 10-17 de docs/10). Los cinco
  niveles de prueba por issue (Unit, Component, Integration, API y
  E2E, docs/06 §7) se cumplieron; cierre verificado en ISS-132.
  `CLOUDFLARE_ACCOUNT_ID` y `CLOUDFLARE_API_TOKEN` ya están cargados
  como secretos del repositorio, así que el demo usa Workers AI; si
  fallaran, responde el respaldo de Pollinations sin que el visitante
  lo note.
- [x] **F11** — Gestión comercial interna (ISS-138 a ISS-155):
  terminada y aprobada por el usuario (11 ago 2026). Pipeline
  lead → cotización → aceptada, cotización en PDF generada por la app y
  respuesta del cliente desde `/mi-cuenta`. Cuatro suites en verde: 129
  ITs de backend + ArchUnit, 244 specs de frontend, 38 e2e con axe sin
  violaciones y Lighthouse 96-97/100/100/100 sobre el build de
  producción, más revisión manual en 375 y 1280 px. **Sin documentos de
  cobro ni DIAN** (decisión 18) y **sin roles internos** todavía
  (decisión 19).
  Alcance: pipeline lead → cotización → aceptada, cotización en PDF
  generada por la app, y respuesta del cliente desde `/mi-cuenta`.
  **La "cuenta de cobro" salió del alcance** (decisión 18 de docs/10):
  Crear Code Cesar es una S.A.S. y como persona jurídica está obligada
  a factura electrónica DIAN — la cuenta de cobro es de personas
  naturales no responsables de IVA y no le sirve al cliente para
  deducir. Tampoco entran roles internos todavía (decisión 19: rol
  único hasta que haya un segundo miembro del equipo).

## Arranque local

Requisitos: JDK 25, Docker (con Docker Compose), Node 22+ con npm. No
se necesita Maven ni Angular CLI instalados globalmente: el backend
trae Maven Wrapper (`./mvnw`) y el frontend usa el CLI local del
proyecto vía `npx`/scripts de `package.json`.

1. **Base de datos y correo local** (desde la raíz del repo):
   ```
   docker compose up -d
   ```
   Deja PostgreSQL en `localhost:5433`, base y usuario `leads`
   (contraseña `leads`, solo para desarrollo local). Puerto 5433 en el
   host — no 5432 — para no chocar con otro PostgreSQL local que ya
   pudiera estar corriendo en esa máquina. Desde F8 también levanta
   **Mailpit** (SMTP en `localhost:1025`, que es el default del
   backend): todos los correos que la app envía en local (verificación
   de cuenta, recuperación, notificación de solicitudes) caen en su
   bandeja en http://localhost:8025 — nada sale a internet.

2. **Backend** (desde `backend/`):
   ```
   ./mvnw spring-boot:run
   ```
   Arranca en `http://localhost:8080`, aplica las migraciones de
   Flyway automáticamente y expone `GET /actuator/health` sin
   autenticación. Si el puerto 8080 ya está en uso en tu máquina:
   `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8090`.
   Para correr toda la suite de pruebas (unitarias + ArchUnit +
   integración con Testcontainers, requiere Docker activo):
   `./mvnw verify`.

3. **Frontend** (desde `frontend/`):
   ```
   npm install
   npm start
   ```
   Sirve en `http://localhost:4200` (usa `npm start -- --port <otro>`
   si ese puerto ya está ocupado). Pruebas con Vitest: `npm test`.
   Build de producción con SSR/prerender: `npm run build`. Las
   peticiones a `/api/*` del formulario de contacto se redirigen al
   backend vía el proxy de desarrollo (`frontend/proxy.conf.json`,
   apunta a `http://localhost:8080` por defecto — actualízalo si
   corriste el backend en otro puerto). Test e2e del flujo de contacto
   (requiere los tres servicios arriba corriendo): `npm run e2e`.

   Nota de esta máquina de desarrollo: el puerto 8080 ya está ocupado
   por Docker Desktop, así que aquí el backend hay que correrlo en el
   8090 (ver arriba) y editar localmente el `target` de
   `proxy.conf.json` a `http://localhost:8090` para poder probar el
   formulario en el navegador (cambio solo local, no se comitea: el
   archivo versionado sigue apuntando al 8080 por defecto).

   **Runbook de la suite e2e** (los 36 tests, con axe): `docker compose
   up -d` → `node e2e/stub-groq.mjs` (puerto 9099) → backend con
   `GROQ_API_URL=http://localhost:9099/openai/v1`,
   `POLLINATIONS_URL=http://localhost:9099` y
   `ASISTENTE_LIMITE_ANONIMO=3` → `npm start` → `npm run e2e` con
   `E2E_API_BASE_URL=http://localhost:8090`. Los límites hay que
   subirlos para poder correr la suite varias veces seguidas, y **cada
   regla tiene su propia variable**: la del formulario es
   `RATE_LIMIT_MAX_SOLICITUDES` (no `..._MAX_INTENTOS`, que es la de
   login/registro/asistente) — con el nombre equivocado el límite
   real sigue en 20/10 min y el e2e de contacto falla en la tercera
   corrida sin decir por qué.

   **Probar el build de producción localmente**: `npm run
   servir:estatico` (puerto 4300) construye la imagen del sitio y la
   levanta. No es una réplica de producción: **es la misma imagen**
   —Angular prerenderizado servido por Caddy— que corre en el servidor
   (ADR-13). Ahí apunta también `npm run lighthouse`.

   `npm run verificar:servido <url>` comprueba que la **primera
   respuesta** de cada ruta traiga la página correcta, con el `<link
   rel="canonical">` como marcador. Funciona contra la imagen local o
   contra el dominio, y con dos URLs las compara lado a lado. En CI
   corre sobre la imagen recién construida.

   Nació de un fallo real (12 ago 2026, cuando el sitio aún estaba en
   Render): un único comodín `/* → /index.csr.html` hacía que **todas**
   las páginas devolvieran el cascarón del SPA, porque Render solo
   resuelve el índice de una carpeta con barra final (`/contacto/` sí,
   `/contacto` no). Responden 200 y el navegador pinta la página
   correcta, así que solo se ve mirando el HTML de la primera respuesta
   — que es lo único que leen Google y las tarjetas de
   WhatsApp/LinkedIn. Dos reglas dejó: **un replicador más generoso que
   el original no verifica nada** (el servidor local de entonces
   resolvía él mismo `/contacto`, y por eso en local todo se veía
   bien), y por eso hoy se prueba la imagen de verdad y no una
   imitación.

Verificado manualmente end-to-end (16 jul 2026): los tres servicios
levantados a la vez, `GET /actuator/health` respondió
`{"status":"UP"}` con la base de datos real conectada, y el frontend
respondió 200 en su ruta raíz.

### Alternativa: todo containerizado (perfil `full` de Docker Compose)

Desde F7, `docker-compose.yml` también define `backend` y `frontend`,
construidos desde sus `Dockerfile` de producción (ISS-081) — pero
quedan en el perfil `full`, así que **no afectan** el flujo normal de
arriba (`docker compose up -d` sin perfil sigue levantando solo
Postgres). Útil para probar el stack completo con Postgres real (mismas
imágenes, proxy `/api` real) sin tener Java/Node instalados:

```
docker compose --profile full up --build
```

Levanta Postgres + backend (`http://localhost:8080`) + frontend
(`http://localhost:4300`, con el proxy `/api` ya apuntando al backend
por su nombre de servicio en la red de Compose — no hace falta
`BACKEND_URL` a mano). `docker compose --profile full down` los baja
a los tres.

Nota de esta máquina: el puerto 8080 del host ya está ocupado (ver
arriba), así que acá hace falta `BACKEND_PORT=8090` — se dejó en un
`.env` local en la raíz del repo (gitignored, no se comitea).
Verificado extremo a extremo (jul 2026): los tres contenedores arriba
a la vez, POST `/api/solicitudes` y `/api/auth/login` a través del
proxy del frontend responden igual que en el flujo nativo.

## API del backend (tras la fase F5)

| Endpoint | Auth | Qué hace |
|---|---|---|
| `GET /actuator/health` | Pública | Healthcheck, agrega el estado de la BD |
| `POST /api/solicitudes` | Pública | Registra un lead; honeypot (`sitioWeb`) y rate limiting (20/10 min por IP, configurable) |
| `POST /api/auth/login` | Pública | Login (admin y clientes); devuelve `{ token, expiraEn, rol, correo }` (JWT HS256, 8h por defecto); 403 si la cuenta no está verificada (solo tras contraseña correcta); rate limiting propio (5/15 min por IP) |
| `POST /api/auth/registro` | Pública | Crea una cuenta CLIENTE sin verificar (F8); 201, 409 si el correo ya existe, 400 si la contraseña <10 caracteres; envía el correo de verificación (best-effort) |
| `POST /api/auth/verificacion` | Pública | Consume el token del enlace del correo; 204, 400 único "enlace inválido o vencido" |
| `POST /api/auth/reenvio-verificacion` | Pública | Reenvía el enlace; 202 incondicional (nunca revela si el correo existe); throttle por correo 3/15 min en la capa de aplicación |
| `POST /api/auth/recuperacion` | Pública | Envía enlace de recuperación; 202 incondicional; mismo throttle por correo |
| `POST /api/auth/restablecimiento` | Pública | Cambia la contraseña con el token del correo; 204, deja la cuenta verificada; 400 único |
| `GET /api/solicitudes?estado=` | **Rol ADMIN** (`Bearer <token>`) | Lista solicitudes; un token CLIENTE recibe 403 (hueco cerrado en ISS-094) |
| `PATCH /api/solicitudes/{id}/estado` | **Rol ADMIN** (`Bearer <token>`) | Cambia el estado; 404 si no existe, 409 en transición inválida |

Los cinco endpoints de cuenta tienen rate limiting por IP propio
(variables `RATE_LIMIT_*`, ver `application.properties`) como respaldo
grueso: la protección real contra bombardeo de correos es el límite por
correo en la capa de aplicación, porque en producción todas las
peticiones llegan vía el proxy SSR y comparten IP aparente.

Usuario admin por defecto en local: `admin@crearcode-cesar.local` /
`cambiar-en-produccion` (variables `ADMIN_USERNAME`/`ADMIN_PASSWORD` en
cualquier otro entorno — `ADMIN_USERNAME` ahora **debe** ser un correo
válido, ver ADR-08). Se crea automáticamente al arrancar la app si la
tabla `usuarios` está vacía (`SembradorDeUsuarioAdmin`). La API sigue
siendo stateless (sin CSRF ni sesión de servidor): cada petición al
panel admin lleva su propio token.

## Frontend (tras la fase F3)

8 páginas públicas (home, 3 de servicio, casos listado/detalle, sobre
nosotros, blog listado/artículo, 2 legales), 14 rutas en total,
prerenderizadas con SSR (`npx ng build`). Contenido editorial 100%
desacoplado de componentes en `frontend/src/contenido/` (ver ADR-05 en
[docs/02-arquitectura.md](docs/02-arquitectura.md)). Verificación
manual en navegador (Playwright, mobile 375px y desktop 1280px) sobre
las 8 páginas: sin overflow horizontal, sin errores de consola,
acordeón FAQ y render de Markdown del blog confirmados funcionando.

## Formulario de contacto (tras la fase F4)

Página `/contacto` con Signal Forms (`@angular/forms/signals`):
validación reactiva que espeja exactamente las reglas de los VOs de
dominio (mismo regex de correo, misma normalización de teléfono
colombiano), honeypot invisible (`sitioWeb`), checkbox de consentimiento
no premarcado con enlace a la política, e integración real con
`POST /api/solicitudes`. Éxito muestra confirmación con alternativa de
WhatsApp; fallo muestra error sin perder los datos ya escritos. El CTA
de WhatsApp del header/footer usa el mensaje genérico en la mayoría de
páginas y el mensaje propio del servicio cuando el visitante está en
una página de servicio (`mensajeWhatsappParaRuta`). Verificado extremo
a extremo contra el backend real: el POST persiste la solicitud y
queda visible vía `GET /api/solicitudes` admin. Cubierto además por un
e2e mínimo con Playwright (`frontend/e2e/contacto-e2e.spec.ts`,
`npm run e2e`), con su propio job en CI.

## Panel admin (tras la fase F5)

Autenticación con JWT autoemitido (no HTTP Basic — ver ADR-08 en
[docs/02-arquitectura.md](docs/02-arquitectura.md), decisión pedida
explícitamente por el usuario pensando en crecimiento multi-empleado
con roles). `/admin/login` (pública), `/admin` (listado + filtro por
estado) y `/admin/solicitudes/:id` (detalle + cambio de estado con
confirmación, solo transiciones válidas) protegidas por `adminGuard`.
`SesionService` guarda el token en `sessionStorage` (se pierde al
cerrar la pestaña; sin revocación antes de esa expiración — trade-off
consciente de v1, ver ADR-08). El panel no lleva el header/footer del
sitio público (es una sección interna distinta, no contenido) y queda
fuera de SSR/prerender (`admin/**` con `RenderMode.Client`). Verificado
extremo a extremo en navegador real: login correcto/incorrecto, listado
con datos reales, cambio de estado reflejado de inmediato, logout.

## Cuentas de cliente (tras la fase F8)

Registro público de clientes extendiendo el contexto `usuarios` de F5:
`Usuario` ganó `verificado` y rol `CLIENTE` (rol único por usuario,
multi-rol se evalúa en F11), VO `ContrasenaPlana` (mínimo 10
caracteres, solo registro/restablecimiento), entidad `TokenDeUsuario`
(SecureRandom + SHA-256, un solo uso, 24h verificación / 1h
recuperación, se invalidan los previos al reenviar). Correos por SMTP
(`EnviadorDeCorreosDeCuentaAdapter`, enlaces construidos con
`FRONTEND_URL`); en local van a Mailpit. El login exige cuenta
verificada (403 solo tras contraseña correcta — no revela estado de
cuentas ajenas; las respuestas de reenvío/recuperación son siempre
genéricas por la misma razón). Restablecer NO revoca JWTs vivos
(trade-off aceptado, ADR-08).

Frontend: páginas `/registro`, `/ingreso` (redirige por rol),
`/recuperar-contrasena`, `/verificar-correo` y
`/restablecer-contrasena` (consumen `?token=`, RenderMode.Client) y
`/mi-cuenta` (clienteGuard, mínima: correo + cerrar sesión).
`SesionService` guarda `{token, rol, correo}` (clave `crearcode-sesion`,
sessionStorage); `adminGuard` exige rol ADMIN; el interceptor excluye
todo `/api/auth/*` y decide el destino del 401 según la página actual.
El header muestra Ingresar/Mi cuenta solo tras hidratar
(`afterNextRender`) para no romper la hidratación del prerender. En
sitemap solo entra `/registro`; robots.txt excluye `/mi-cuenta` y las
páginas de token. E2e `cuentas-e2e.spec.ts`: flujo completo con el
enlace real del correo leído de la API REST de Mailpit + axe (que
encontró y permitió corregir un contraste AA insuficiente en los
banners de error, también en el login del admin).

## Asistente IA (tras la fase F9)

Contexto `asistente` hexagonal (ADR-10): puerto `GeneradorDeRespuestas`
implementado por `GroqGeneradorDeRespuestasAdapter`
(chat/completions, modelo `openai/gpt-oss-120b`, `GROQ_API_KEY`
solo por entorno — flujo navegador → proxy `/api` → backend → Groq).
Prompt de sistema anclado a
`backend/src/main/resources/asistente-contexto.md` (mantenido a mano
desde docs/08): nunca inventa precios, escala al humano con el
marcador `[ESCALAR]` (el adaptador lo convierte en bandera).
`POST /api/asistente/mensajes` público con Bearer opcional; límites en
la capa de aplicación ANTES de llamar al proveedor (global diario 800,
registrado 50, anónimo 10 — variables `ASISTENTE_*`), errores con
código estable (`limite-anonimo`/`limite-registrado` 429,
`no-disponible` 503) y rate limit por IP de respaldo. Frontend: widget
flotante (`chat-asistente`) en el shell público, estado en
`ConversacionService` (signals, id de sesión anónima en
sessionStorage), sugerencias iniciales, escalamiento con WhatsApp
contextual, límite anónimo con CTA a `/registro`, nota de
transparencia. Tests sin gastar cuota: ITs contra un stub HTTP del JDK
y e2e contra `frontend/e2e/stub-groq.mjs` (CI lo arranca con
`GROQ_API_URL` y `ASISTENTE_LIMITE_ANONIMO=3`).

## Rediseño visual (tras la fase F8.5)

Evolución de la paleta oficial sin cambiarla (decisión del usuario, 28
jul 2026): tokens nuevos en `styles.scss` (gradiente de marca, acento
luminoso `#4CC38A` solo decorativo, sombras de elevación, transición
estándar, radio grande — valores en
[docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md) §Evolución
visual). Hero de la Home sobre el gradiente con decoración SVG y botón
primario invertido; tarjetas con elevación en hover; scroll-reveal vía
la directiva `aparecerAlVer`
(`frontend/src/app/componentes/aparecer-al-ver/`) — IntersectionObserver
de un solo disparo, no-op en SSR y bajo `prefers-reduced-motion`, el
estado oculto solo se aplica desde JS (sin JS todo es visible). Sección
"Tu cuenta te da más" en la Home y beneficios junto al formulario de
`/registro` (HU-34), con la regla de honestidad: lo que llega con
F9/F10 lleva badge "Muy pronto". El e2e de accesibilidad emula
`reducedMotion` para que axe escanee la página completa sin estados de
transición; axe atrapó dos contrastes AA reales en esta fase (banners
de error 4.27:1 y badge "Muy pronto" 4.19:1), ambos corregidos
oscureciendo el texto. Lighthouse tras el rediseño: Performance 97-98,
resto 100 — se mantiene lo ganado en F6.

## Proveedor de imágenes del demo (ISS-127, ISS-137)

El puerto `GeneradorDeImagenes` tiene tres montajes, elegidos en un
único punto (`ConfiguracionDeGeneradorDeImagenes`) con la variable
`DEMO_PROVEEDOR_IMAGENES`:

- **`pollinations`** (default del código): gratis y sin key.
- **`cloudflare`** (lo que corre en producción):
  Workers AI como primario **con respaldo automático a Pollinations**
  (`GeneradorDeImagenesConRespaldo`) — si el primario falla, responde
  el respaldo y el visitante no se entera. Necesita
  `CLOUDFLARE_ACCOUNT_ID` y `CLOUDFLARE_API_TOKEN` por entorno;
  verificado extremo a extremo el 10 ago 2026 (boceto real en 2,4 s).
- **`gemini`**: listo para el día que su capa gratis vuelva a incluir
  imágenes (en ago 2026 daba límite 0) o haya billing.

Ningún proveedor de la capa gratis tiene SLA: por eso el modo con
respaldo. Los adaptadores no llevan anotaciones condicionales — se
instancian desde la configuración, así que hay un solo lugar que leer
para saber qué corre.

**Prompts de imagen**: describir lo que sí se quiere, nunca lo que no.
Los modelos de difusión ignoran las negaciones — pedir "sin precios"
terminaba dibujando columnas con signos de peso, y pedir "sin texto
largo" producía mockups vacíos. El prompt vive en
`GenerarDemoDeDisenoUseCaseImpl.descripcionDeImagen` y va en inglés
(rinden bastante mejor), con los datos del negocio injertados tal como
los escribió el visitante.

## Rediseño F10e (Home, servicios y header — ISS-133 a ISS-135)

Segunda capa visual sobre F8.5, siguiendo el prototipo aprobado por el
usuario (10 ago 2026). No cambia paleta ni tokens: los reutiliza.

- **Home**: hero con gancho + tarjeta blanca del demo de diseño (CTA a
  `/herramientas#demo-diseno`), sección "Pruébalo con tu propio
  negocio" con las 4 herramientas, sección del asistente cuyas
  preguntas sugeridas **abren el widget flotante y envían la pregunta**
  (vía `AsistenteUiService` en `nucleo/` — la página no conoce al chat,
  solo publica la intención; contador de aperturas + pregunta de un
  solo uso), tabla "visitante vs. con cuenta" (`TABLA_CUENTA`, espeja
  los defaults de los límites del backend), y **placeholders honestos**
  de casos/equipo que reemplazaron a los testimonios ficticios de la v1
  (a su vez reemplazados por contenido real el 28 sep 2026, ver
  §Proyectos).
- **Servicios**: miga de pan, resumen corto, dos columnas con aside
  pegajoso que lleva al diagnóstico (`/herramientas#diagnostico`), y
  los títulos "Lo que resolvemos" / "Cómo trabajamos".
- **Header**: doble CTA "Agenda tu consulta" + "Crear cuenta" (o "Mi
  cuenta" con sesión, `/admin` para admins), detrás de la hidratación
  como siempre. El botón de WhatsApp salió del header; sigue en footer,
  hero, cierre y escalamiento del asistente.
- **`anchorScrolling: 'enabled'`** en el router: los CTA con ancla a
  `/herramientas` posicionan en la herramienta correcta.
- **Hallazgo AA**: el e2e de zoom de texto al 200% atrapó un overflow
  horizontal real — `1fr` en una rejilla deja que el contenido mínimo
  de una tarjeta empuje la columna. Convención nueva: `minmax(0, 1fr)`
  (ver [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md)
  §Rediseño F10e).
- Lighthouse tras el rediseño (build de producción, móvil): Performance
  97-98, Accesibilidad/Buenas Prácticas/SEO 100 en Home, servicio,
  `/herramientas` y Contacto.

## Proyectos, antes "Casos de éxito" (28 sep 2026)

La sección `/casos` dejó de llamarse "Casos de éxito" y pasa a
**Proyectos**: emprendimientos propios y código abierto, **no encargos
de clientes**. El nombre importa — bajo "casos de éxito" el lector
asume un cliente que pagó y quedó satisfecho, que es la misma mentira
de los testimonios ficticios que se quitaron en F10e, solo que más
difícil de ver.

Para una empresa que aún no ha vendido esto pesa más que un espacio
reservado: uparya.co se puede abrir y el código se puede leer; un
testimonio no se puede comprobar.

Cuatro entradas en `contenido/casos.ts`, una por línea de negocio:
**UparYa** (en producción), **Corpus Damana** (IA anclada a un corpus,
el mismo patrón del asistente del sitio), **Pasarela de Pagos Cripto**
(en desarrollo, y el texto lo dice) y **Cesar Travel**.

**Regla de entrada, escrita como test** (`contenido/casos.spec.ts`, no
en un documento que nadie relee): cada proyecto trae un enlace `https`
que funciona, ninguno se describe con lenguaje de cliente y no quedan
corchetes de plantilla. Por eso quedó fuera la app de monday: su
backend no tiene cara pública y no se pudo comprobar la ficha del
marketplace. Un enlace que el visitante abre y no encuentra hace más
daño que la ausencia del proyecto.

Se corrigió de paso la meta descripción de la página, que afirmaba
"casos de éxito de pymes colombianas que ya trabajan con Crear Code
Cesar" — falso, y era lo que mostraba Google. En la Home, los dos
placeholders de espacio reservado cedieron el sitio a los proyectos y
al perfil real del fundador.

## Rediseño tech "Código + IA" (28 sep 2026, rama `rediseno-tech`)

Tema oscuro con estética de terminal, a pedido del usuario. **Los
nombres de las variables CSS se conservaron y solo cambiaron sus
valores**, para no tocar 35 componentes uno a uno — con la consecuencia
de que `--color-primario` ya no significa lo que su nombre sugiere.
Regla de color: **verde `#3ddc97` = código, violeta `#9d8cff` = IA**, y
el violeta nunca se usa para acciones genéricas. Tres tipografías
(Space Grotesk, Figtree, JetBrains Mono) solo con los pesos que se
usan. Valores completos en
[docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md) §Rediseño tech.

Dos detalles que conviene no perder:

- **`--sombra-1` pasó de sombra difusa a un anillo de 1px.** Una sombra
  oscura sobre fondo oscuro no existe: las tarjetas que se separaban
  solo con sombra se habrían quedado sin borde visible y ningún test lo
  habría dicho.
- **El menú de Servicios es un `<details>/<summary>` nativo.** Teclado y
  táctil salen gratis; un desplegable con hover y CSS pasaría axe igual
  y sería inservible en un teléfono. Verificado interactuando, que es
  lo único que axe no hace.

Verificado sobre la imagen de producción: Lighthouse **97-98/100/100/100**
(igual que antes del rediseño), axe sin violaciones en 12 páginas, 242
specs en verde y sin desborde a 375 ni 1280 px. **Falta el OK del
usuario y los e2e completos con backend** antes de fusionar a master.

## Cotizaciones (fase F11)

Contexto `cotizaciones` con su agregado `Cotizacion` (dominio plano, sin
Spring ni JPA). Dos invariantes mandan sobre el resto: **una cotización
enviada ya no se edita** (lo que el cliente vio no cambia después) y
**los totales los calcula el dominio**, nunca llegan de fuera —
`ItemDeCotizacion` calcula su subtotal y `Dinero` lleva la aritmética
adentro, en pesos enteros.

- **Pipeline**: `lead → cotización → aceptada`. Al aceptar, el lead de
  origen pasa a `CONVERTIDA` solo si la transición aplica: el pipeline
  comercial no se rompe por el estado de un lead viejo.
- **Consecutivo** `COT-AAAA-NNNN` por año, con `UPDATE … RETURNING`
  atómico (probado con 20 envíos simultáneos). Se asigna **al enviar**:
  un borrador que nunca sale no consume número.
- **PDF** con OpenPDF detrás del puerto `GeneradorDeDocumento`. El
  documento se identifica como cotización — ni factura ni cuenta de
  cobro (decisión 18 de docs/10) — y omite NIT/dirección mientras el
  usuario no los confirme, en vez de imprimir datos inventados.
- **Correo** con el PDF adjunto (`MimeMessageHelper`, el primero del
  sitio con adjunto), best-effort: si el SMTP falla, la cotización queda
  enviada y el PDF se comparte a mano.
- **API**: `/api/cotizaciones/**` (rol ADMIN) y `/api/mis-cotizaciones/**`
  (cliente). El correo del cliente sale del token, nunca de la
  petición; una cotización ajena responde **404 y no 403**, para no
  revelar que existe.
- **Frontend**: panel en `/admin/cotizaciones` (listado, apertura desde
  un lead con `?solicitud=`, detalle editable con totales en vivo) y
  `/mi-cuenta/cotizaciones` para que el cliente descargue y responda.
- **Datos de la empresa** (defaults en `application.properties`, del
  certificado de Cámara de Comercio del 16 jul 2026): NIT
  **901941017-0**, Calle 4B # 20-36, Oficina 303, Barrio Callejas,
  Valledupar. **Pendiente**: si la empresa es responsable de IVA — ese
  dato está en el RUT de la DIAN, no en el certificado de Cámara —, la
  validez por defecto (hoy 15 días) y las condiciones de pago del pie.

## SEO, rendimiento y accesibilidad (tras la fase F6)

- **Metadatos por página** (`title`, `meta description`, Open Graph):
  cada página pública setea los suyos vía `Meta`/`Title` de Angular
  (`frontend/src/app/nucleo/metadatos-pagina.ts`), alimentados desde
  `contenido/`. Imagen OG por defecto en
  `frontend/public/imagenes/og-defecto.jpg`.
- **`sitemap.xml` y `robots.txt`**: generados dinámicamente desde el
  servidor SSR (`frontend/src/server.ts` + `frontend/src/servidor/`),
  no como archivos estáticos — así la URL base sale siempre de
  `contenido/sitio.ts` (ADR-06), sin duplicar el dominio. `/admin`
  queda excluido de ambos (HU-23).
- **Compresión**: el servidor Express comprime todas las respuestas
  (`compression`, gzip/brotli) — sin esto los bundles de Angular viajan
  sin comprimir y penalizan Performance en Lighthouse y en redes
  móviles reales.
- **Paleta**: tres colores se oscurecieron por contraste insuficiente
  como texto (mínimo AA 4.5:1) — acento/éxito, verde de WhatsApp y
  ámbar de alerta. Detalle y valores nuevos en
  [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md).
- **Auditoría Lighthouse** (`npm run lighthouse`, script propio en
  `frontend/scripts/lighthouse-audit.mjs` — Playwright + lighthouse
  programático vía CDP, sin depender de un Chrome del sistema; corre
  contra el build de producción real, no el dev server): Home, un
  servicio, `/herramientas` (añadida en F10e) y Contacto en modo móvil
  dan Performance 97-98, Accesibilidad 100, Buenas Prácticas 100,
  SEO 100.
- **Checklist de accesibilidad**: los 10 puntos de
  [docs/06-plan-de-pruebas.md](docs/06-plan-de-pruebas.md) §5
  verificados con axe-core vía Playwright
  (`frontend/e2e/accesibilidad-e2e.spec.ts`) sobre Home, un servicio,
  Contacto y el panel admin completo (login, listado y detalle
  autenticados) — cero violaciones. Encontró y permitió corregir dos
  bugs reales de layout/contraste que ningún test previo había
  atrapado (detalle en el mismo documento).
- **Convención de imágenes**: el sitio no tiene todavía ninguna imagen
  de contenido — queda documentada la convención (`NgOptimizedImage`,
  WebP, alt text) para cuando se agreguen imágenes reales, en vez de
  optimizar algo que no existe (ver
  [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md) §Imágenes).

## Despliegue (servidor propio con Kamal 2 — ADR-13)

Desde el 19 ago 2026 todo corre en un **VPS propio** (Netcup,
Virginia), desplegado con **Kamal 2 desde GitHub Actions**. Antes fue
Render + Neon; el historial de esa etapa está en
[docs/09-despliegue.md](docs/09-despliegue.md) §§1-8, y el
procedimiento del corte en §9.

- **Dos apps de Kamal comparten el host `crearcodecesar.com`**:
  `config/deploy.web.yml` (el sitio) se lleva todo, y
  `config/deploy.api.yml` (el backend) se lleva `/api` y `/actuator`
  vía `path_prefixes` con `strip_path_prefix: false`. Así el navegador
  sigue viendo un solo origen y **el proyecto sigue sin CORS**
  (ADR-09), pero con un único salto de proxy: la IP real del visitante
  llega al backend.
- **El sitio se despliega antes que la API, siempre.** kamal-proxy solo
  acepta la configuración de TLS en el servicio que sirve la raíz, así
  que el backend va con `ssl: false` y se cuelga de su certificado. Es
  **al revés que en UparYa**, donde la API va primero.
- **`forward_headers: false` explícito en los dos.** No es estilo: con
  `ssl: false` su valor por omisión es `true`, y entonces kamal-proxy
  conserva el `X-Forwarded-For` que mande el cliente.
- **`frontend/Dockerfile`** construye Angular y lo sirve con **Caddy**
  (`frontend/Caddyfile`), que traduce las reglas que antes aplicaba
  Render y hace el 301 de `www`. **`backend/Dockerfile`** no cambió.
- **Los secretos son secretos del repositorio en GitHub**, referenciados
  por nombre en `.kamal/secrets` — nunca valores en el repo.
  `KAMAL_REGISTRY_PASSWORD` es la excepción: sale del `GITHUB_TOKEN` de
  cada ejecución y no hay que crearlo.
- **El despliegue cuelga de las pruebas** (`needs:` de los cuatro jobs)
  y por eso vive en el mismo workflow que ellas: `needs:` no cruza
  workflows. Un filtro por rutas evita que un commit de documentación
  reconstruya una imagen con Maven dentro.
- **Base de datos**: PostgreSQL compartido del servidor, rol y base
  `crearcodecesar`, alcanzable solo desde las redes de Docker.
- **Verificación del sitio**: `npm run verificar:servido <url>`
  comprueba que la primera respuesta de cada ruta traiga la página
  correcta. Corre en CI sobre la imagen recién construida y sirve igual
  contra el dominio.
- **Pendiente (ISS-082)**: comprar el dominio, decidir registrador
  (ver [docs/09-despliegue.md](docs/09-despliegue.md) §4, sin
  decisión todavía), y el visto bueno explícito del usuario para
  publicar de verdad.

## Decisiones ya resueltas por el usuario

- Correo (11 ago 2026): **se envía por Resend, puerto 2587**, y **se
  responde a `contacto@crearcodecesar.com`**. El 2587 no es capricho:
  Render descarta el tráfico saliente a los puertos SMTP clásicos (25,
  465, 587) en el plan gratuito, y eso dejaba las peticiones colgadas
  sin error. Verificado en producción: llega a bandeja principal de
  Gmail. Detalle del incidente en docs/09 §7. El remitente de todos los correos es
  `Crear Code Cesar <contacto@crearcodecesar.com>`, fijado explícito en
  `RemitenteDeCorreo` porque el usuario SMTP de Resend es la palabra
  literal `resend` y el servicio **rechaza con 422** cualquier
  remitente fuera de un dominio verificado. `crearcodecesar.com` debe
  estar verificado en Resend antes del primer envío. El correo
  corporativo que muestra el sitio sigue siendo
  `admin@crearcodecesar.com` (ver docs/09 §7).
- **IVA de las cotizaciones: 19% por defecto** (11 ago 2026). Una
  S.A.S. es persona jurídica y por regla general responsable de IVA;
  cotizar sin él y descubrirlo después obliga a pedirle al cliente un
  19% extra sobre algo que ya aceptó. Se baja por cotización o con
  `COTIZACIONES_IMPUESTO` si el contador confirma que no aplica.
- LinkedIn del fundador: https://www.linkedin.com/in/juan-carlos-gutierrez-huerfano369582/
- Paleta del sitio: nació como **Opción C, "Minimal Corporativo"**
  (clara) y desde el **rediseño tech del 28 sep 2026 es oscura** —
  fondo `#071115`, verde menta `#3ddc97` para todo lo que es código y
  violeta `#9d8cff` para todo lo que es IA. Los nombres de las
  variables CSS no cambiaron, solo sus valores, así que
  **`--color-primario` ya no es el azul de marca sino la tinta clara de
  los títulos**: leer un componente viejo suponiendo lo contrario
  confunde. Valores y reglas en
  [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md) §Rediseño
  tech.
- Backend en **Spring Boot 4.1.x** (no 3.x): la última versión 3.x
  (3.5.16) quedó sin soporte OSS el 30 jun 2026, justo antes de iniciar
  la Etapa 2 (ver ADR-07 en [docs/02-arquitectura.md](docs/02-arquitectura.md)).

## Pendientes que requieren input del usuario

Con la Etapa 3 completa (11 ago 2026), lo que queda es puesta a punto
para las pruebas del MVP. **Nada de esto bloquea el código: todo son
acciones del usuario en paneles externos, o decisiones suyas.**

**~~Configuración en Render~~ — sección cerrada.** El sitio corre en
servidor propio desde el 19 ago 2026 (ADR-13) y `render.yaml` se borró
del repo el 28 sep. Las variables que aquí se pedían viven hoy como
secretos del repositorio en GitHub, cargadas antes del corte; el
procedimiento completo está en
[docs/09-despliegue.md](docs/09-despliegue.md) §9. Lo que sí quedó
pendiente del corte:

- **Apagar los servicios de Render**, que siguen encendidos sin dominio
  apuntándoles.
- **Revocar las credenciales viejas** de Resend, Groq y Cloudflare,
  rotadas antes del corte.
- **La prueba de ISS-136**: agotar el rate limit de un endpoint de
  `/api` desde un equipo y pedir el mismo desde otra red (datos
  móviles). Hasta hacerla, el límite por IP real está implementado pero
  no verificado.
- **Copias de seguridad del PostgreSQL y un monitor de
  disponibilidad**: hoy no existen para ningún proyecto del servidor.
  No duele mientras los datos sean de prueba; tienen que estar antes de
  salir al mercado.

**De la auditoría integral del 28 sep 2026** (ISS-161 a ISS-178 en
[docs/05-backlog-issues.md](docs/05-backlog-issues.md)): lo técnico ya
está hecho — asistente restaurado, cupos atómicos, cabeceras, 404,
SEO, aviso de privacidad. Lo que espera al usuario, en orden de
impacto: la **política de datos definitiva** (hoy dice "borrador" en
público), **analítica** (no se mide nada), la **prueba de ISS-136**
que destraba bajar el rate limit de 600, los **números del cotizador**
por tipo de proyecto, y las decisiones de producto (header, hero,
landings por sector, calendario).

**Decisiones del usuario**:

- **Condición de IVA**: hoy las cotizaciones salen con **19%** por
  defecto, porque una S.A.S. es persona jurídica y por regla general
  responsable. Confirmar con el contador (casilla 53 del RUT) y, si no
  aplica, bajarlo con `COTIZACIONES_IMPUESTO`.
- **Validez por defecto** (hoy 15 días) y condiciones comerciales del
  pie del PDF (anticipo, forma de pago).
- **Revisar el eslogan del hero** ("Tecnología que trabaja para tu
  negocio, no al revés.") — pendiente desde el 29 jul 2026. Sigue
  siendo el titular del rediseño, ahora con "no al revés" resaltado.
- **Aprobar el rediseño tech** de la rama `rediseno-tech` para
  fusionarlo a master.
- **Una foto real del fundador** para `/sobre-nosotros`: es lo único
  inventado que queda en esa página, y hoy simplemente no hay imagen.
  Cuadrada o 4:5, mínimo 800×800 px. Una horizontal de la misma sesión
  serviría para la tarjeta que se ve al compartir el sitio, hoy
  genérica.

**Higiene de secretos**: la rotación se hizo el 19 ago 2026 al cargar
los secretos del repositorio para el corte al servidor propio (Resend,
Groq y el token de Cloudflare; `CLOUDFLARE_ACCOUNT_ID` no se rota
porque es un identificador, y la contraseña de Neon dejó de importar al
abandonar Neon). **Falta revocar las viejas**, que siguen vivas.
