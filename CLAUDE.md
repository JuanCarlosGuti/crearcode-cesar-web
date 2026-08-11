# CLAUDE.md — Crear Code Cesar S.A.S. · Sitio web corporativo

Resumen operativo del proyecto. La fuente de verdad viva es la carpeta
[`docs/`](docs/) — si una decisión no está documentada allí, no se
asume: se pregunta al usuario y se actualiza el documento
correspondiente antes de seguir.

## Estado del proyecto

**Etapa actual: Etapa 3 — Plataforma v2. La fase F8 (cuentas de
cliente) está TERMINADA y APROBADA por el usuario (28 jul 2026;
ISS-083 a ISS-100, suites backend/frontend/e2e en verde + verificación
manual en navegador). Para que su correo funcione en producción faltan
solo las variables `MAIL_USERNAME`/`MAIL_PASSWORD` en el dashboard de
Render (App Password de Gmail, guía en
[docs/09-despliegue.md](docs/09-despliegue.md) §7 — pausado a pedido
del usuario). La fase F8.5 — rediseño visual y valor de la cuenta
(ISS-101 a ISS-107) — está TERMINADA y APROBADA (28 jul 2026;
Lighthouse 97-98/100/100/100, 21/21 e2e con axe, verificación manual).
La fase F9 — asistente IA con Groq (ISS-108 a ISS-118) — está
TERMINADA, APROBADA y PUBLICADA (28 jul 2026), verificada con Groq
real en local (respuestas ancladas, sin inventar precios,
escalamiento funcionando). La `GROQ_API_KEY` ya está en Render y el
asistente quedó verificado EN PRODUCCIÓN (28 jul 2026: pregunta de
precio respondida sin cifras inventadas y con escalamiento). Decisión
del usuario (28 jul 2026): la configuración del correo de producción
(`MAIL_USERNAME`/`MAIL_PASSWORD` en Render) queda **pospuesta hasta
las pruebas del MVP** — antes de eso el usuario quiere pulir
funcionalidad y estética.

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
2026: Render (capa gratis) + Neon.**

URLs de producción:
- **Dominio canónico: https://crearcodecesar.com** (comprado el 10 ago
  2026, Cloudflare como DNS/proxy, ver ADR-11; www redirige 301)
- Frontend (origen del servicio): https://crearcodecesar-frontend.onrender.com
- Backend: https://crearcodecesar-backend.onrender.com (API detrás
  del proxy `/api` del frontend, ver ADR-09)

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
variable de entorno en Render el día que se use — nunca en el repo.

- [x] **F8** — Cuentas de cliente (ISS-083 a ISS-100): terminada,
  aprobada y en producción; su correo espera las variables `MAIL_*` en
  Render (pausado a pedido del usuario).
- [x] **F8.5** — Rediseño visual y valor de la cuenta (ISS-101 a
  ISS-107): terminada y aprobada (28 jul 2026).
- [x] **F9** — Asistente IA (Groq, ISS-108 a ISS-118): terminada,
  aprobada y publicada (28 jul 2026); su `GROQ_API_KEY` espera en el
  dashboard de Render.
- [x] **F10** — Centro de herramientas con IA (ISS-119 a ISS-137):
  terminada, aprobada y publicada (10 ago 2026). F10a cotizador +
  /herramientas viva → F10b simulador de chatbot → F10c diagnóstico
  digital → F10d demo de diseño → F10e rediseño Home/servicios/header
  según el prototipo aprobado (decisiones 10-17 de docs/10). Los cinco
  niveles de prueba por issue (Unit, Component, Integration, API y
  E2E, docs/06 §7) se cumplieron; cierre verificado en ISS-132.
  Pendiente solo del usuario: ingresar `CLOUDFLARE_ACCOUNT_ID` y
  `CLOUDFLARE_API_TOKEN` en Render para que el demo use Cloudflare
  (hasta entonces responde el respaldo de Pollinations).
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

   Para probar el build de producción SSR localmente
   (`node dist/frontend/server/server.mjs`, no `npm start`): Angular 22
   valida el header `Host` contra una allowlist (protección SSRF, ver
   [angular.dev/best-practices/security](https://angular.dev/best-practices/security#preventing-server-side-request-forgery-ssrf)) —
   sin configurarla, cualquier petición responde 400. Se resuelve con
   la variable de entorno `NG_ALLOWED_HOSTS` (lista separada por comas,
   **sin puerto**, ej. `NG_ALLOWED_HOSTS=localhost`). En producción esa
   variable deberá incluir el dominio real (pendiente de F7).

Verificado manualmente end-to-end (16 jul 2026): los tres servicios
levantados a la vez, `GET /actuator/health` respondió
`{"status":"UP"}` con la base de datos real conectada, y el frontend
respondió 200 en su ruta raíz.

### Alternativa: todo containerizado (perfil `full` de Docker Compose)

Desde F7, `docker-compose.yml` también define `backend` y `frontend`,
construidos desde sus `Dockerfile` de producción (ISS-081) — pero
quedan en el perfil `full`, así que **no afectan** el flujo normal de
arriba (`docker compose up -d` sin perfil sigue levantando solo
Postgres). Útil para probar el stack tal como corre en Render (mismas
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
(chat/completions, modelo `llama-3.3-70b-versatile`, `GROQ_API_KEY`
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
- **`cloudflare`** (lo que declara `render.yaml` para producción):
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
  de casos/equipo que reemplazan a los testimonios ficticios de la v1.
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

## Despliegue (tras ISS-079 a ISS-081 de la fase F7 — ISS-082 pendiente)

Hosting elegido por el usuario tras comparar costos en
[docs/09-despliegue.md](docs/09-despliegue.md): **Render (backend +
frontend, capa gratis) + Neon (PostgreSQL, capa gratis)** — $0/mes de
cómputo, único costo fijo el dominio (aún no comprado).

- **`backend/Dockerfile`** y **`frontend/Dockerfile`** (multi-stage,
  nuevos en F7): Render los construye directo desde el repo, sin
  necesidad de un registry propio. CI (`docker-build` en
  `.github/workflows/ci.yml`) verifica que ambos construyan en cada
  push/PR.
- **Sin CORS** (ver ADR-09 en
  [docs/02-arquitectura.md](docs/02-arquitectura.md)): el servidor SSR
  del frontend (`frontend/src/server.ts`) reenvía todo `/api/**` al
  backend real vía `http-proxy-middleware` — el navegador solo ve un
  origen. `SolicitudesApi`/`AuthApi` no cambiaron (siguen con rutas
  relativas `/api/...`).
- **`render.yaml`** (Blueprint de Render, raíz del repo): declara los
  dos web services (capa gratis, región Virginia) con sus variables —
  secretos como `sync: false` (se ingresan en el dashboard de Render,
  nunca en el repo) y `JWT_SECRET` autogenerado por Render.
- **Variables de entorno nuevas para producción**:
  - `DB_URL` (backend): URL JDBC completa hacia Neon (con
    `sslmode=require`, que Neon exige). Sin setearla, se arma desde
    `DB_HOST`/`DB_PORT`/`DB_NAME` como siempre en local.
  - `BACKEND_URL` (frontend): URL pública del backend en Render. Sin
    setearla, el proxy local apunta a `http://localhost:8080`.
  - `PORT` (ambos): ya estándar — Render la inyecta sola; el backend
    la lee vía `server.port=${PORT:8080}`, el frontend ya la leía
    desde antes de F7.
  - `NG_ALLOWED_HOSTS` (frontend, descubierta en F6): debe incluir el
    dominio real de Render el día del despliegue — sin esto, Angular
    devuelve 400 a cualquier petición (protección SSRF nativa).
  - `HSTS_MAX_AGE` (frontend, ADR-11 revisado el 11 ago 2026): segundos
    del `Strict-Transport-Security` que emite el servidor SSR. Default
    conservador de `86400` (un día); se sube desde el dashboard sin
    desplegar código, escalando 86400 → 604800 → 2592000 → 31536000.
    La cabecera se emite solo si `x-forwarded-proto` es https (para no
    romper el desarrollo local) y **nunca lleva `preload`**.
- **Verificado extremo a extremo** (no solo `docker build`): ambas
  imágenes corridas juntas en una red Docker con Postgres real
  confirmaron que el proxy reenvía correctamente login y registro de
  solicitudes; la suite e2e completa
  (`contacto-e2e.spec.ts`, `accesibilidad-e2e.spec.ts`) corrida contra
  el build de producción con el proxy activo pasa sin modificar ningún
  test existente.
- **Pendiente (ISS-082)**: comprar el dominio, decidir registrador
  (ver [docs/09-despliegue.md](docs/09-despliegue.md) §4, sin
  decisión todavía), y el visto bueno explícito del usuario para
  publicar de verdad.

## Decisiones ya resueltas por el usuario

- Correo corporativo: **`admin@crearcodecesar.com`** (11 ago 2026), con
  el dominio propio. Reemplaza al `crearcodecesar@gmail.com` temporal
  en todo el sitio, el PDF de cotizaciones, el contexto del asistente y
  las notificaciones internas. Falta saber **con qué proveedor vive ese
  buzón** (Google Workspace, Zoho…) para fijar el SMTP de producción.
- LinkedIn del fundador: https://www.linkedin.com/in/juan-carlos-gutierrez-huerfano369582/
- Paleta oficial del sitio: **Opción C — "Minimal Corporativo"** (ver
  [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md)).
- Backend en **Spring Boot 4.1.x** (no 3.x): la última versión 3.x
  (3.5.16) quedó sin soporte OSS el 30 jun 2026, justo antes de iniciar
  la Etapa 2 (ver ADR-07 en [docs/02-arquitectura.md](docs/02-arquitectura.md)).

## Pendientes que requieren input del usuario

Al cierre de la fase F11 (11 ago 2026) quedan abiertos:

- **Condición de IVA de la empresa** (responsable o no, y el
  porcentaje): define si las cotizaciones llevan IVA. El dato está en
  el RUT de la DIAN, no en el certificado de Cámara de Comercio; hasta
  confirmarlo el impuesto queda en 0.
- **Validez por defecto de las cotizaciones** (hoy 15 días) y las
  condiciones comerciales del pie del PDF (anticipo, forma de pago).

- **Credenciales de Cloudflare en Render** (`CLOUDFLARE_ACCOUNT_ID` y
  `CLOUDFLARE_API_TOKEN`): sin ellas el demo de diseño responde con el
  respaldo de Pollinations en vez de Workers AI.
- **Correo corporativo final con el dominio propio** y las variables
  `MAIL_USERNAME`/`MAIL_PASSWORD` en Render — pospuesto por decisión
  del usuario hasta las pruebas del MVP (28 jul 2026).
- **Rotar los secretos que circularon por el chat** antes de las
  pruebas de campo: token de Cloudflare, App Password de Gmail y la
  contraseña de Neon. Todos viven solo en el `.env` local (gitignored)
  y en el dashboard de Render, así que rotarlos es pegar el valor
  nuevo en dos sitios.
- **Revisar el eslogan del hero** ("Tecnología que trabaja para tu
  negocio, no al revés.") — el usuario quiere mirarlo dentro del
  pulido previo a las pruebas del MVP (29 jul 2026).
- **Arranque de la fase F11** (gestión interna): se descompone en
  issues cuando el usuario dé la orden.
