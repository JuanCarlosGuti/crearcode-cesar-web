# CLAUDE.md — Crear Code Cesar S.A.S. · Sitio web corporativo

Resumen operativo del proyecto. La fuente de verdad viva es la carpeta
[`docs/`](docs/) — si una decisión no está documentada allí, no se
asume: se pregunta al usuario y se actualiza el documento
correspondiente antes de seguir.

## Estado del proyecto

**Etapa actual: Etapa 2 — Desarrollo. Fases F0 y F1 completas
(ISS-001 a ISS-020), pendiente de OK explícito del usuario para
pasar a F2.**

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

## Checklist de fases (Etapa 2 — actualizar a medida que avance)

- [x] **F0** — Esqueleto monorepo (Spring Boot JDK 25 + Angular 22 CLI) + CI + ArchUnit + healthcheck
- [x] **F1** — Dominio `leads` con tests (TDD, sin Spring)
- [ ] **F2** — API + persistencia (casos de uso, JPA, REST, seguridad, honeypot, rate limiting)
- [ ] **F3** — Frontend: estructura y páginas con contenido
- [ ] **F4** — Formulario end-to-end con Signal Forms + notificaciones
- [ ] **F5** — Panel admin
- [ ] **F6** — SEO, rendimiento y accesibilidad (Lighthouse ≥90)
- [ ] **F7** — Despliegue (costos de hosting + dominio, decisión final con el usuario)

Detalle de issues por fase en
[docs/05-backlog-issues.md](docs/05-backlog-issues.md).

## Arranque local

Requisitos: JDK 25, Docker (con Docker Compose), Node 22+ con npm. No
se necesita Maven ni Angular CLI instalados globalmente: el backend
trae Maven Wrapper (`./mvnw`) y el frontend usa el CLI local del
proyecto vía `npx`/scripts de `package.json`.

1. **Base de datos** (desde la raíz del repo):
   ```
   docker compose up -d
   ```
   Deja PostgreSQL en `localhost:5433`, base y usuario `leads`
   (contraseña `leads`, solo para desarrollo local). Puerto 5433 en el
   host — no 5432 — para no chocar con otro PostgreSQL local que ya
   pudiera estar corriendo en esa máquina.

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
   Build de producción con SSR/prerender: `npm run build`.

Verificado manualmente end-to-end (16 jul 2026): los tres servicios
levantados a la vez, `GET /actuator/health` respondió
`{"status":"UP"}` con la base de datos real conectada, y el frontend
respondió 200 en su ruta raíz.

## Decisiones ya resueltas por el usuario

- Correo corporativo temporal: `crearcodecesar@gmail.com` (se
  reemplazará por un correo con dominio propio cuando se compre el
  dominio — ver pendiente más abajo).
- LinkedIn del fundador: https://www.linkedin.com/in/juan-carlos-gutierrez-huerfano369582/
- Paleta oficial del sitio: **Opción C — "Minimal Corporativo"** (ver
  [docs/07-guia-de-estilo.md](docs/07-guia-de-estilo.md)).
- Backend en **Spring Boot 4.1.x** (no 3.x): la última versión 3.x
  (3.5.16) quedó sin soporte OSS el 30 jun 2026, justo antes de iniciar
  la Etapa 2 (ver ADR-07 en [docs/02-arquitectura.md](docs/02-arquitectura.md)).

## Pendientes que requieren input del usuario

- Dominio web definitivo y correo corporativo final con ese dominio
  (decisión al cierre de la fase F7).
