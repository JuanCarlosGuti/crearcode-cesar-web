# 05 — Backlog de issues técnicos

Descomposición de las historias de usuario ([[04-historias-de-usuario]])
en issues técnicos numerados `ISS-NNN`, organizados en las fases F0-F7
que se ejecutarán en la Etapa 2. Cada issue lista: descripción, HU
asociada, definición de hecho (DoD), estimación (S/M/L), dependencias y
los tests que lo prueban (nombrados). El orden dentro de cada fase es el
orden de ejecución sugerido.

Convención de estimación: **S** = medio día o menos, **M** = 1-2 días,
**L** = 3+ días o con incertidumbre relevante.

---

## Fase F0 — Esqueleto de monorepo, CI, ArchUnit, healthcheck

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-001 | Generar esqueleto backend Spring Boot 4.1.x + Java 25 + Maven | HU-27 | Proyecto arranca con `mvn spring-boot:run`; se verifica y documenta compatibilidad de Lombok, ArchUnit y plugins Maven con JDK 25 (ver ADR-07 en [[02-arquitectura]]) | M | — | `ContextLoads` (smoke test de arranque) |
| ISS-002 | Docker Compose con PostgreSQL local | HU-27 | `docker compose up` deja PostgreSQL disponible con BD/usuario esperados | S | — | Verificación manual + healthcheck de contenedor |
| ISS-003 | Flyway configurado + migración baseline | HU-27 | Backend aplica migraciones al arrancar contra el PostgreSQL de ISS-002 | S | ISS-001, ISS-002 | `FlywayMigrationIT` (Testcontainers) |
| ISS-004 | Estructura de paquetes `com.crearcode.leads` (dominio/aplicacion/infraestructura) | — (soporte de HU-28) | Paquetes creados, vacíos salvo package-info; visibles en el árbol del proyecto | S | ISS-001 | — |
| ISS-005 | Test ArchUnit de reglas de dependencia hexagonal | HU-28 | Falla el build si `dominio/` importa Spring/JPA o depende de `infraestructura/`; falla si `aplicacion/` depende de `infraestructura/` | M | ISS-004 | `ArchitectureRulesTest` |
| ISS-006 | Endpoint healthcheck | HU-27 | `GET /actuator/health` (o equivalente) responde 200 con BD conectada | S | ISS-001, ISS-003 | `HealthCheckIT` |
| ISS-007 | Esqueleto frontend Angular 22 CLI (standalone, zoneless, SSR habilitado) | HU-27 | `ng serve` y `ng build` (con SSR) funcionan sobre un proyecto base sin `NgModule` ni Zone.js | M | — | Smoke test de build |
| ISS-008 | Configurar Vitest en frontend | HU-28 | `ng test` (o script equivalente) ejecuta Vitest sobre un test trivial | S | ISS-007 | `app.component.spec.ts` (placeholder) |
| ISS-009 | Pipeline CI (backend + frontend + ArchUnit) | HU-28 | Un push/PR dispara build+test de backend, build+test de frontend y ArchUnit; falla si cualquiera falla | M | ISS-005, ISS-008 | El propio pipeline es la verificación |
| ISS-010 | Documentar arranque local en `CLAUDE.md`/README | HU-27 | Un desarrollador nuevo levanta el stack completo siguiendo solo la documentación | S | ISS-002, ISS-007 | Verificación manual (checklist) |

---

## Fase F1 — Dominio `leads` con tests (TDD, sin Spring)

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-011 | VO `Correo` con validación de formato | HU-13 | Rechaza correos vacíos o con formato inválido; acepta formatos válidos | S | ISS-004 | `CorreoTest` |
| ISS-012 | VO `Telefono` con validación colombiana | HU-13 | Normaliza espacios/guiones y prefijo `+57`; rechaza formatos no colombianos | S | ISS-004 | `TelefonoTest` |
| ISS-013 | VO `DatosDeContacto` | HU-12, HU-13 | Compone `Correo`+`Telefono`+nombre/empresa; rechaza nombre vacío | S | ISS-011, ISS-012 | `DatosDeContactoTest` |
| ISS-014 | Enum `ServicioDeInteres` | HU-05, HU-12 | Valores `DESARROLLO_A_LA_MEDIDA`, `IA_Y_AUTOMATIZACION`, `SOLUCIONES_TECNOLOGICAS`, `OTRO` | S | ISS-004 | Cubierto por `SolicitudDeContactoTest` |
| ISS-015 | Enum `EstadoSolicitud` + máquina de estados | HU-21 | Método que valida transiciones según el grafo de [[03-modelo-de-dominio]] §3 | M | ISS-004 | `EstadoSolicitudTest` (incluye transiciones inválidas y terminales) |
| ISS-016 | VO `ConsentimientoDatos` | HU-14 | No se puede construir con `aceptado=false` desde el flujo de registro | S | ISS-004 | `ConsentimientoDatosTest` |
| ISS-017 | Excepciones de dominio | HU-13, HU-21 | `TransicionDeEstadoInvalidaException`, `DatosDeContactoInvalidosException`, `ConsentimientoRequeridoException` | S | ISS-004 | Cubiertas por sus respectivos tests de VO/entidad |
| ISS-018 | Entidad `SolicitudDeContacto` (factoría `registrar` + `cambiarEstado`) | HU-12, HU-14, HU-21 | `registrar()` aplica invariantes 1-3, 6-7 de [[03-modelo-de-dominio]]; `cambiarEstado()` aplica invariantes 4-5 | M | ISS-013, ISS-015, ISS-016, ISS-017 | `SolicitudDeContactoTest` (incl. casos tristes: sin consentimiento, transición inválida) |
| ISS-019 | Puertos de entrada (interfaces) | HU-12, HU-20, HU-21 | `RegistrarSolicitudUseCase`, `CambiarEstadoSolicitudUseCase`, `ListarSolicitudesUseCase` definidos en `dominio/` | S | ISS-018 | — (verificado por ArchUnit + usos en F2) |
| ISS-020 | Puertos de salida (interfaces) | HU-18, HU-20 | `SolicitudRepositorio`, `NotificadorPort` definidos en `dominio/` | S | ISS-018 | — (verificado por ArchUnit) |

---

## Fase F2 — API + persistencia

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-021 | Implementar `RegistrarSolicitudUseCase` en `aplicacion/` | HU-12, HU-18 | Orquesta creación + `repositorio.guardar()` + `notificador.notificarNuevaSolicitud()`; `@Transactional` | M | ISS-019, ISS-020 | `RegistrarSolicitudUseCaseTest` (con `SolicitudRepositorio`/`NotificadorPort` falsos) |
| ISS-022 | Implementar `CambiarEstadoSolicitudUseCase` | HU-21 | Recupera solicitud, aplica `cambiarEstado()`, persiste | S | ISS-020 | `CambiarEstadoSolicitudUseCaseTest` (con fake) |
| ISS-023 | Implementar `ListarSolicitudesUseCase` | HU-20, HU-22 | Lista todas o filtradas por `EstadoSolicitud` | S | ISS-020 | `ListarSolicitudesUseCaseTest` (con fake) |
| ISS-024 | Entidad JPA + mapper `SolicitudDeContacto` ↔ `SolicitudJpaEntity` | HU-12 | Mapper explícito, sin exponer entidad JPA fuera de `infraestructura/persistencia` | M | ISS-018 | `SolicitudMapperTest` |
| ISS-025 | Implementación `SolicitudRepositorio` (Spring Data JPA) | HU-12, HU-20 | `guardar`, `buscarPorId`, `listar`, `listarPorEstado` funcionan contra PostgreSQL real | M | ISS-024, ISS-003 | `SolicitudRepositorioIT` (Testcontainers PostgreSQL) |
| ISS-026 | Migración Flyway tabla `solicitudes_contacto` | HU-12 | Tabla con columnas para datos de contacto, servicio, mensaje, estado, consentimiento y timestamps | S | ISS-003 | Cubierta por ISS-025 (IT) |
| ISS-027 | Controlador REST `POST /api/solicitudes` | HU-12 | Valida DTO de entrada (Bean Validation) y delega a `RegistrarSolicitudUseCase` | M | ISS-021 | `SolicitudControllerIT` (incl. caso triste: payload inválido → 400) |
| ISS-028 | Controlador REST `GET /api/solicitudes` (listado + filtro) | HU-20, HU-22 | Requiere autenticación; soporta `?estado=` | S | ISS-023, ISS-035 | `SolicitudControllerIT` |
| ISS-029 | Controlador REST `PATCH /api/solicitudes/{id}/estado` | HU-21 | Requiere autenticación; responde 409/400 en transición inválida | M | ISS-022, ISS-035 | `SolicitudControllerIT` (incl. transición inválida) |
| ISS-030 | DTOs de request/response + Bean Validation en el borde HTTP | HU-13 | Validación de formato duplicada en el borde (mensaje de error HTTP claro) sin reemplazar la validación de dominio | S | ISS-027 | `SolicitudControllerIT` |
| ISS-031 | Manejo global de errores (`@ControllerAdvice`) | HU-12, HU-21 | Respuestas de error consistentes, sin stacktraces ni datos internos expuestos | S | ISS-027 | `GlobalExceptionHandlerTest` |
| ISS-032 | Adaptador de notificación por correo (`NotificadorPort`) | HU-18 | Envía correo con datos clave de la solicitud; fallo de envío no revierte la persistencia ya hecha | M | ISS-020 | `NotificadorEmailAdapterIT` (servidor SMTP de prueba) |
| ISS-033 | Honeypot en el borde de recepción | HU-15 | Solicitud con campo honeypot no vacío se descarta antes de `RegistrarSolicitudUseCase`, responde 200 aparente | S | ISS-027 | `SolicitudControllerIT` (caso honeypot) |
| ISS-034 | Rate limiting (interceptor/filtro) | HU-16 | Más de N solicitudes por IP en ventana de tiempo → 429 | M | ISS-027 | `RateLimitingFilterIT` |
| ISS-035 | Spring Security: usuario único admin | HU-19 | Login protege `/api/solicitudes*`; credenciales solo por variable de entorno | M | ISS-001 | `SeguridadAdminIT` (login correcto/incorrecto, acceso sin sesión) |
| ISS-036 | Revisión de logging sin datos personales | HU-19 (transversal seguridad) | Ningún log ni URL expone nombre/correo/teléfono de un lead; IDs de solicitud son `SolicitudId` | S | ISS-021 a ISS-032 | Revisión manual + `LoggingSinDatosPersonalesTest` (verifica formato de logs de los casos de uso) |

---

## Fase F3 — Frontend: estructura y páginas con contenido

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-037 | Esquema de datos de contenido tipado (servicio, caso, artículo) | HU-05, HU-06, HU-09 | Interfaces TypeScript + archivos de datos en `contenido/`, sin texto embebido en componentes (ADR-05) | M | ISS-007 | `contenido.schema.spec.ts` |
| ISS-038 | Página Home | HU-01, HU-02, HU-03 | Incluye propuesta de valor, 3 tarjetas de servicio y sección de prueba social, todo desde `contenido/` | M | ISS-037 | `home.page.spec.ts` |
| ISS-039 | Layout header/footer con CTA doble persistente | HU-04 | CTA "agendar" + WhatsApp visibles en header/footer en todas las páginas públicas | S | ISS-007 | `layout.component.spec.ts` |
| ISS-040 | Páginas de servicio (x3) | HU-05 | Estructura problema/incluye/proceso/entregables/FAQ, con FAQ expandible | M | ISS-037 | `pagina-servicio.component.spec.ts` |
| ISS-041 | Página Casos (listado + detalle) | HU-06, HU-07 | Listado con 2-3 casos placeholder; detalle con reto/solución/resultado | M | ISS-037 | `casos.page.spec.ts`, `caso-detalle.page.spec.ts` |
| ISS-042 | Página Sobre nosotros | HU-08 | Incluye historia, perfil del fundador, forma de trabajar y valores | S | ISS-037 | `sobre-nosotros.page.spec.ts` |
| ISS-043 | Blog (listado + render de artículo Markdown) | HU-09, HU-10 | Listado ordenado por fecha; render de Markdown con metadatos SEO propios por artículo | M | ISS-037 | `blog-listado.page.spec.ts`, `blog-articulo.page.spec.ts` |
| ISS-044 | Páginas legales (política de datos, términos) | HU-11 | Texto borrador con datos reales de la empresa, accesible desde footer y desde el formulario | S | ISS-037 | `legales.page.spec.ts` |
| ISS-045 | Configurar SSR/prerender para rutas de contenido público | HU-01, HU-23, HU-25 | Build genera HTML prerrenderizado para todas las rutas de contenido | M | ISS-038 a ISS-044 | Verificación de build + `PrerenderSmokeTest` |
| ISS-046 | Revisión responsive mobile-first de componentes base | HU-01 (transversal) | Sin scroll horizontal ni elementos cortados en viewport móvil de referencia | M | ISS-038 a ISS-044 | Verificación manual + capturas en breakpoints clave |

---

## Fase F4 — Formulario end-to-end con Signal Forms + notificaciones

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-047 | Formulario de contacto con Signal Forms | HU-12, HU-13 | Validación reactiva que espeja las reglas de `DatosDeContacto`; mensajes de error específicos por campo | L | ISS-037 | `formulario-contacto.component.spec.ts` (incl. casos tristes de validación) |
| ISS-048 | Campo honeypot en el formulario | HU-15 | Oculto visualmente, `aria-hidden`, no interfiere con navegación por teclado | S | ISS-047 | `formulario-contacto.component.spec.ts` (caso honeypot) |
| ISS-049 | Checkbox de consentimiento con enlace a política | HU-14 | No premarcado; bloquea envío si no se marca | S | ISS-047, ISS-044 | `formulario-contacto.component.spec.ts` (caso sin consentimiento) |
| ISS-050 | Integración formulario → `POST /api/solicitudes` | HU-12 | Maneja éxito (mensaje de confirmación) y error (no pierde datos ya escritos) | M | ISS-047, ISS-027 | `formulario-contacto.component.spec.ts` (mock de servicio HTTP) |
| ISS-051 | Botón WhatsApp con mensaje precargado dinámico | HU-17 | Mensaje incluye contexto de la página/servicio actual | S | ISS-039 | `whatsapp-cta.component.spec.ts` |
| ISS-052 | Test e2e del flujo de contacto completo | HU-12, HU-18 | Llenar formulario → ver confirmación → verificar que la solicitud quedó registrada (vía API o BD de prueba) | L | ISS-050, ISS-021, ISS-032 | `contacto-e2e.spec.ts` (Playwright o equivalente) |

---

## Fase F5 — Autenticación robusta + Panel admin

**Nota de esta fase (actualizado tras iniciar F5)**: el backlog original
asumía que el frontend reutilizaría el HTTP Basic *stateless* de
ISS-035 (F2). Al arrancar F5 el usuario pidió explícitamente un sistema
de autenticación robusto pensando en crecimiento multi-empleado/roles
(ver ADR-08 en [[02-arquitectura]] y el contexto `usuarios` en
[[03-modelo-de-dominio]]), lo que reabre esa pieza de F2: el mecanismo
de ISS-035 queda superado por `AuthController`/JWT (ISS-058 a ISS-061
abajo), aunque su fila se deja tal cual en la Fase F2 como registro
histórico de lo que efectivamente se construyó en ese momento.

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-053 | Dependencia JWT + propiedades de configuración | HU-19 | `spring-boot-starter-oauth2-resource-server` agregado; `app.jwt.secreto`/`app.jwt.expiracion-minutos` configurables por variable de entorno (secreto ≥32 bytes) | S | ISS-001 | Verificación de build |
| ISS-054 | Dominio `usuarios`: `Usuario`, `Rol`, `UsuarioId` + puertos | HU-19 | Entidad y VOs sin Spring/JPA; puertos `UsuarioRepositorio`, `CifradorDeContrasenas`, `GeneradorDeToken`, `AutenticarUsuarioUseCase`, `CrearUsuarioUseCase` definidos en `dominio/` | M | ISS-004 | `UsuarioTest` (verificado además por ArchUnit) |
| ISS-055 | Caso de uso `AutenticarUsuarioUseCase` | HU-19 | Mismo mensaje genérico si el correo no existe o la contraseña no coincide (`CredencialesInvalidasException` en `aplicacion/`, no en `dominio/`) | M | ISS-054 | `AutenticarUsuarioUseCaseTest` (con fakes, incl. ambos casos tristes) |
| ISS-056 | Caso de uso `CrearUsuarioUseCase` | HU-19 | Usado por el *seed* del admin único (ISS-062); rechaza correo duplicado | S | ISS-054 | `CrearUsuarioUseCaseTest` (con fake) |
| ISS-057 | Migración Flyway `usuarios` + adaptador JPA | HU-19 | Tabla `usuarios` (correo único); `UsuarioRepositorioJpaAdapter` con búsqueda case-insensitive | M | ISS-054 | `UsuarioRepositorioIT` (Testcontainers) |
| ISS-058 | Adaptadores de cifrado y token | HU-19 | `BCryptCifradorDeContrasenas` (hash real); `JwtGeneradorDeToken` (HS256, claims `rol` y `jti`) | M | ISS-053, ISS-054 | `BCryptCifradorDeContrasenasTest`, `JwtGeneradorDeTokenTest` |
| ISS-059 | `SecurityConfig`: HTTP Basic → JWT Bearer | HU-19 | `POST /api/auth/login` público; resto de `/api/solicitudes*` exige `Bearer <token>` válido; `AuthenticationEntryPoint`/`AccessDeniedHandler` propios devuelven el mismo `ErrorResponse` que el resto de la API | L | ISS-058 | Reescritura de `SeguridadAdminIT` (Bearer en vez de Basic) |
| ISS-060 | Rate limiting en el login | HU-19 (endurecido) | `RateLimitingFilter` generalizado a múltiples reglas (ruta+método+umbral); `POST /api/auth/login` con umbral propio, más estricto que el del formulario de contacto | M | ISS-059 | `RateLimitingFilterIT` (regla de login) |
| ISS-061 | Controlador `POST /api/auth/login` | HU-19 | Devuelve token + expiración en éxito; 401 genérico en fallo, sin distinguir causa | M | ISS-055, ISS-059 | `AuthControllerIT` (login correcto, incorrecto, rate-limit) |
| ISS-062 | *Seed* del admin único al arrancar | HU-19 | Si `usuarios` está vacía, crea el admin desde `ADMIN_USERNAME` (pasa a exigir formato de correo)/`ADMIN_PASSWORD`; tolera arranque concurrente (idempotente) | S | ISS-056, ISS-057 | `SembradorDeUsuarioAdminIT` |
| ISS-063 | `AuthApi` (cliente HTTP de login) en frontend | HU-19 | `POST /api/auth/login` tipado | S | ISS-061 | `auth-api.spec.ts` |
| ISS-064 | `SesionService` (estado de sesión) | HU-19 | Signal con el token, persistido en `sessionStorage`; no accede a `sessionStorage` durante SSR (`isPlatformBrowser`) | M | ISS-063 | `sesion.spec.ts` |
| ISS-065 | Interceptor de autenticación | HU-19 | Adjunta `Authorization: Bearer` cuando hay token; cualquier 401 (salvo la propia petición de login) limpia la sesión y navega a `/admin/login` | M | ISS-064 | `token.interceptor.spec.ts` |
| ISS-066 | Guard de ruta protegida (`adminGuard`) | HU-19 | Rutas `/admin/*` (salvo `/admin/login`) redirigen a login si no hay sesión | S | ISS-064 | `admin.guard.spec.ts` |
| ISS-067 | Página de login admin | HU-19 | Formulario con Signal Forms (correo + contraseña); error genérico visible sin redirigir | M | ISS-063, ISS-065, ISS-066 | `login.spec.ts` |
| ISS-068 | Listado de solicitudes en panel admin + filtro por estado | HU-20, HU-22 | Consume `GET /api/solicitudes`; estado vacío general y estado vacío específico por filtro; ordenado más reciente primero | M | ISS-066 | `listado-solicitudes.spec.ts` (incl. caso filtro sin resultados) |
| ISS-069 | Detalle de solicitud + cambio de estado | HU-21 | Solo ofrece transiciones válidas según estado actual (máquina de estados de [[03-modelo-de-dominio]] Parte 1 §3); confirma antes de aplicar | M | ISS-068 | `detalle-solicitud.spec.ts` (incl. estado terminal sin opciones) |
| ISS-070 | Logout | HU-19 | Botón visible en el panel; limpia la sesión y navega a login | S | ISS-065 | `logout.spec.ts` (o cubierto en `sesion.spec.ts`) |
| ISS-071 | Rutas admin fuera de SSR/prerender | HU-19 (transversal) | `admin/**` con `RenderMode.Client`; el build no las prerrenderiza | S | ISS-067 | Verificación de build |

---

## Fase F6 — SEO, rendimiento y accesibilidad

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-072 | Metadatos por página (title, description) | HU-23 | Cada ruta de contenido setea metadatos propios vía Angular `Meta`/`Title`, alimentados desde `contenido/` | M | ISS-045 | `meta-por-pagina.spec.ts` |
| ISS-073 | `sitemap.xml` generado desde rutas de contenido | HU-23 | Incluye todas las páginas públicas, excluye `/admin` | S | ISS-045 | Verificación de build + `SitemapTest` |
| ISS-074 | `robots.txt` | HU-23 | Permite rastreo público, deshabilita explícitamente `/admin` | S | — | Verificación manual |
| ISS-075 | Open Graph por página + imagen por defecto | HU-24 | Cada página define OG:title/description/image; fallback a imagen por defecto | M | ISS-072 | `open-graph.spec.ts` |
| ISS-076 | Optimización de imágenes | HU-25 | Formatos modernos (ej. WebP/AVIF), tamaños adecuados, carga diferida donde aplica | M | ISS-038 a ISS-044 | Auditoría Lighthouse (ISS-077) |
| ISS-077 | Auditoría Lighthouse ≥90 (Performance/SEO/Accesibilidad) + ajustes | HU-25, HU-26 | Home, un servicio y Contacto alcanzan ≥90 en modo móvil; ajustes documentados | L | ISS-045, ISS-072 a ISS-076 | Reporte Lighthouse adjunto como evidencia |
| ISS-078 | Checklist de accesibilidad aplicado | HU-26 | Foco visible, alt text, contraste AA, errores de formulario anunciados y asociados a su campo | M | ISS-047, ISS-077 | Checklist de [[06-plan-de-pruebas]] + revisión con lector de pantalla |

---

## Fase F7 — Despliegue

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-079 | Documentar opciones de hosting económico + costos (incl. dominio) | HU-29 | Al menos 2 opciones comparadas (frontend y backend), con costo estimado mensual/anual y el dominio (~$60.000-80.000 COP/año) | M | — | No aplica (documento) |
| ISS-080 | Configuración agnóstica al dominio: proxy `/api` en vez de CORS (ADR-09) | HU-29 | El navegador solo llama al frontend; `server.ts` reenvía `/api/**` al backend real vía `BACKEND_URL`. `server.port` del backend lee `PORT` | S | ISS-075 | e2e completo (`contacto-e2e`, `accesibilidad-e2e`) contra el build de producción con el proxy activo |
| ISS-081 | Pipeline de build de producción | HU-29 | Genera build SSR de frontend y artefacto/imagen de backend listos para desplegar | M | ISS-009, ISS-045 | Verificación de build en CI |
| ISS-082 | Checkpoint de decisión de publicación con el usuario | HU-29 | El usuario aprueba explícitamente publicar, con costos ya documentados en ISS-079 | S | ISS-079, ISS-081 | No aplica (decisión humana, no técnica) |

---

## Fase F8 — Cuentas de cliente (Etapa 3)

Primera fase de la v2 ([[10-vision-v2]], aprobada el 27 jul 2026).
Historias: HU-30 a HU-33 (épica E6 en [[04-historias-de-usuario]]).
Decisiones cerradas al arrancar (registradas en
[[03-modelo-de-dominio]] Parte 2 y en el plan de fase): rol único por
usuario (no `Set<Rol>`, se revisa en F11); correo de producción con
Gmail + App Password (Brevo documentado como migración futura);
registro duplicado → 409 explícito; error de token único ("enlace
inválido o vencido"); restablecer contraseña no revoca JWTs vivos
(ADR-08); throttle de correos **por correo** en la capa de aplicación
(el límite por IP es respaldo — en producción la IP visible es la del
proxy del frontend).

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-083 | Documentación de F8 (HUs E6, modelo de dominio, backlog, copy, nota ADR-08) | HU-30..33 | Docs 02/03/04/05/08 actualizados antes del código | S | — | No aplica (documento) |
| ISS-084 | Dominio: `Rol.CLIENTE`, `Usuario.verificado` + factorías/`verificar()`/`conContrasena()`, VO `ContrasenaPlana` | HU-30 | Invariantes con tests puros; sembrador/login no usan `ContrasenaPlana` | S | ISS-083 | `UsuarioTest`, `ContrasenaPlanaTest` |
| ISS-085 | Dominio: `TokenDeUsuario` (+`generar` con SecureRandom/SHA-256), puertos `TokenDeUsuarioRepositorio` y `EnviadorDeCorreosDeCuenta`, `SesionAutenticada`+rol+correo, `UsuarioRepositorio.buscarPorId` | HU-31, HU-32 | Vigencia/un-solo-uso como invariantes; ArchUnit en verde | M | ISS-084 | `TokenDeUsuarioTest` |
| ISS-086 | Aplicación: `RegistrarClienteUseCase` | HU-30 | Crea no verificado + token 24h + correo best-effort; 409 si existe | M | ISS-085 | `RegistrarClienteUseCaseTest` (con fakes) |
| ISS-087 | Aplicación: `VerificarCorreoUseCase` + `ReenviarVerificacionUseCase` | HU-31 | Un solo uso, invalida previos, throttle 3/15min por correo, silencioso si no existe | M | ISS-086 | `VerificarCorreoUseCaseTest`, `ReenviarVerificacionUseCaseTest` |
| ISS-088 | Aplicación: `SolicitarRecuperacionUseCase` + `RestablecerContrasenaUseCase` | HU-32 | Respuesta genérica siempre; restablecer marca verificado | M | ISS-087 | `SolicitarRecuperacionUseCaseTest`, `RestablecerContrasenaUseCaseTest` |
| ISS-089 | Aplicación: gate de cuenta no verificada en el login | HU-31 | `CuentaNoVerificadaException` solo tras validar contraseña | S | ISS-084 | `AutenticarUsuarioUseCaseTest` ampliado |
| ISS-090 | Persistencia: migración `V4__cuentas_de_cliente.sql` + entidad/mapper/adapter de tokens + columna `verificado` | HU-30..32 | Backfill admin verificado; FK, UNIQUE hash, NOT NULLs | M | ISS-085 | `TokenDeUsuarioRepositorioIT`, `UsuarioRepositorioIT` ampliado |
| ISS-091 | Correo: `EnviadorDeCorreosDeCuentaAdapter` + `FRONTEND_URL` + props SMTP auth/starttls (default off) | HU-31, HU-32 | Enlaces armados en el adaptador; GreenMail sigue en verde | M | ISS-085 | `EnviadorDeCorreosDeCuentaAdapterIT` (GreenMail) |
| ISS-092 | REST: `POST /api/auth/registro`, `/verificacion`, `/reenvio-verificacion` + handlers 409/403/400 | HU-30, HU-31 | Bean Validation en el borde; respuestas consistentes | M | ISS-086, ISS-087, ISS-090 | `AuthControllerIT` ampliado |
| ISS-093 | REST: `POST /api/auth/recuperacion`, `/restablecimiento` | HU-32 | Respuesta genérica; token inválido → 400 único | S | ISS-088, ISS-090 | `AuthControllerIT` ampliado |
| ISS-094 | Seguridad: `hasRole("ADMIN")` en `/api/solicitudes/**` (cierra hueco), permitAll explícitos de auth, reglas de rate limit nuevas | HU-33 | Token CLIENTE → 403 en admin; orden de matchers correcto | M | ISS-092 | `SeguridadAdminIT` ampliado (caso CLIENTE→403), `RateLimitingFilterIT` |
| ISS-095 | Frontend núcleo: `AuthApi` ampliada, `SesionService` con rol+correo (clave `crearcode-sesion`), `clienteGuard`, `adminGuard` con rol, interceptor con destino por `router.url` | HU-33 | Specs de guard/interceptor/sesión en verde | M | ISS-092 | `sesion.spec`, `admin.guard.spec`, `cliente.guard.spec`, `token.interceptor.spec` |
| ISS-096 | Frontend: páginas `/registro` e `/ingreso` (Signal Forms, confirmación con `valueOf`, copy en `contenido/cuenta.ts`) | HU-30, HU-33 | Validación espejo del dominio (correo, 10 chars); ADMIN → `/admin` | M | ISS-095 | `registro.spec`, `ingreso.spec` |
| ISS-097 | Frontend: `/verificar-correo`, `/recuperar-contrasena`, `/restablecer-contrasena` | HU-31, HU-32 | Verificación auto al abrir; éxito genérico en recuperación | M | ISS-095 | `verificar-correo.spec`, `recuperar.spec`, `restablecer.spec` |
| ISS-098 | Frontend: `/mi-cuenta` + header con sesión (señal post-hidratación) + rutas server/sitemap/robots | HU-33 | Sin mismatch de hidratación; robots Disallow rutas con token | M | ISS-095 | `mi-cuenta.spec`, `header.spec`, `sitemap/robots.spec` |
| ISS-099 | Mailpit en compose (perfil default) y CI + e2e `cuentas-e2e.spec.ts` (flujo completo con enlace real) | HU-30..33 | e2e lee el enlace vía API de Mailpit; overrides `RATE_LIMIT_*` documentados; axe en páginas nuevas | M | ISS-096..098 | `cuentas-e2e.spec.ts` |
| ISS-100 | Verificación manual en navegador + guía App Password de Gmail + variables en Render + cierre de fase | HU-30..33 | Flujo real probado en producción con correo real; CLAUDE.md al día; OK del usuario para F9 | M | ISS-099 | Checklist manual |

Follow-ups registrados (no en F8): limpieza periódica de
`tokens_de_usuario` vencidos; evaluar red interna de Render para que el
rate limit por IP vea la IP real del cliente.

---

## Fase F8.5 — Rediseño visual y valor de la cuenta (Etapa 3)

Fase corta intercalada a pedido del usuario (28 jul 2026, decisiones
en [[10-vision-v2]] §F8.5 y §5). Historias: HU-34 y HU-35 (épica E7 en
[[04-historias-de-usuario]]). Regla de la fase: **cada cambio visual
se verifica contra lo ganado en F6** — AA, cero violaciones axe y
Lighthouse Performance ≥ 95 / resto 100; `prefers-reduced-motion`
desactiva toda animación.

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-101 | Documentación de F8.5 (HUs E7, visión, backlog, guía de estilo §Evolución visual, copy de la sección de beneficios) | HU-34, HU-35 | Docs 04/05/07/08/10 y CLAUDE.md actualizados antes del código | S | — | No aplica (documento) |
| ISS-102 | Sistema visual: tokens nuevos en `styles.scss` (gradiente de marca, elevaciones/sombras, transiciones estándar) + utilidades de animación con `prefers-reduced-motion` | HU-35 | Valores documentados en [[07-guia-de-estilo]]; contrastes AA verificados; sin regresión axe | M | ISS-101 | e2e `accesibilidad-e2e` sigue en verde |
| ISS-103 | Directiva `aparecerAlVer` (scroll-reveal con IntersectionObserver, una sola vez, zoneless-safe, no-op en SSR y con reduced-motion) | HU-35 | Sin layout shift; contenido visible sin JS (progressive enhancement) | M | ISS-102 | `aparecer-al-ver.spec` |
| ISS-104 | Hero de la Home renovado (gradiente, jerarquía de CTAs, elemento gráfico SVG sutil) + hover/focus en tarjetas de servicios y CTAs | HU-35 | Mobile 375px sin overflow; foco visible se conserva | M | ISS-102 | `home.spec` ajustado; e2e a11y |
| ISS-105 | Sección "Tu cuenta te da más" en la Home (tarjetas de beneficios + CTA a `/registro`, copy de [[08-contenido]], "muy pronto" en IA/demo) | HU-34 | Contenido en `contenido/` (ADR-05); enlaces correctos | M | ISS-102 | `home.spec` ampliado |
| ISS-106 | Página `/registro` con panel de beneficios junto al formulario + scroll-reveal en el resto de páginas públicas | HU-34, HU-35 | El formulario no pierde nada de HU-30; responsive | M | ISS-103, ISS-105 | `registro.spec` ampliado |
| ISS-107 | Verificación integral y cierre: Lighthouse ≥ umbrales, axe cero violaciones, e2e verdes, verificación manual en navegador (375/1280), CLAUDE.md al día | HU-34, HU-35 | Puntajes F6 mantenidos; OK del usuario para F9 | M | ISS-104..106 | `npm run lighthouse`, suites e2e, checklist manual |

---

## Fase F9 — Asistente IA (Etapa 3)

Historias: HU-36 a HU-38 (épica E8 en [[04-historias-de-usuario]]).
Arquitectura y decisiones en ADR-10 ([[02-arquitectura]]): puerto
`GeneradorDeRespuestas`, adaptador Groq, prompt anclado a
`asistente-contexto.md`, límites en la capa de aplicación (el de IP es
respaldo). La `GROQ_API_KEY` vive en el `.env` local (gitignored) y en
el dashboard de Render — nunca en el repo. Los tests de integración y
el e2e usan un **stub HTTP del proveedor** (la URL base del adaptador
es configurable): deterministas, sin gastar cuota ni exponer la key.

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-108 | Documentación de F9 (HUs E8, ADR-10, modelo, backlog, copy del chat + prompt de sistema en docs/08) | HU-36..38 | Docs 02/03/04/05/08 y CLAUDE.md antes del código | S | — | No aplica (documento) |
| ISS-109 | Dominio `asistente`: VOs `MensajeDeChat` (rol usuario/asistente, texto con longitud máx), `ConversacionDeAsistente` (historial acotado), puerto `GeneradorDeRespuestas`, excepciones propias | HU-36 | Invariantes con tests puros; ArchUnit en verde | M | ISS-108 | `MensajeDeChatTest`, `ConversacionDeAsistenteTest` |
| ISS-110 | Aplicación: `ResponderAlVisitanteUseCase` — arma el prompt anclado, aplica límites (global diario, por usuario, por sesión anónima) y traduce fallos del proveedor a la respuesta de indisponibilidad | HU-36, HU-38 | Nunca propaga errores técnicos; límites configurables por properties | L | ISS-109 | `ResponderAlVisitanteUseCaseTest` (fakes) |
| ISS-111 | Infraestructura: `GroqGeneradorDeRespuestasAdapter` (chat completions, timeout corto, `GROQ_API_KEY`/`GROQ_API_URL`/`GROQ_MODELO` por properties) + recurso `asistente-contexto.md` | HU-36 | IT contra stub HTTP local (sin red externa); key jamás logueada | M | ISS-109 | `GroqGeneradorDeRespuestasAdapterIT` (stub) |
| ISS-112 | REST: `POST /api/asistente/mensajes` (público, Bearer opcional para límite mayor) + regla de rate limit por IP de respaldo + handlers | HU-36, HU-38 | permitAll explícito; 429 con mensaje amable; validación de tamaño | M | ISS-110, ISS-111 | `AsistenteControllerIT` |
| ISS-113 | Frontend: `AsistenteApi` + `ConversacionService` (signals: mensajes, estado enviando, límite restante, id de sesión anónima en sessionStorage) | HU-36, HU-38 | Specs en verde; rutas relativas `/api` (ADR-09) | M | ISS-112 | `asistente-api.spec`, `conversacion.spec` |
| ISS-114 | Widget de chat: burbuja flotante + panel (mensajes, entrada, sugerencias iniciales, `aria-live` para respuestas, foco accesible, cierre con Esc) | HU-36 | Accesible (axe); no bloquea SSR/prerender; móvil 375px sin overflow | L | ISS-113 | `chat-widget.spec` |
| ISS-115 | Escalamiento a humano: detección de la señal de escalamiento + CTA de WhatsApp contextual (`mensajeWhatsappParaRuta`) y enlace a /contacto dentro del chat | HU-37 | El mensaje de WhatsApp es el de la página actual | M | ISS-114 | `chat-widget.spec` ampliado |
| ISS-116 | Límites en la UX: aviso de límite alcanzado con CTA a /registro (anónimos) y mensaje de indisponibilidad global | HU-38 | Texto según docs/08; sin errores técnicos visibles | S | ISS-114 | `chat-widget.spec` ampliado |
| ISS-117 | e2e `asistente-e2e.spec.ts` contra stub de Groq (conversación, escalamiento, límite) + axe del widget abierto + stub para CI | HU-36..38 | Job e2e de CI en verde con el stub como service/proceso | M | ISS-114..116 | `asistente-e2e.spec.ts` |
| ISS-118 | Verificación manual (navegador real, prueba con Groq real en local) + `render.yaml` con `GROQ_*` + CLAUDE.md + cierre de fase | HU-36..38 | Prueba real end-to-end con la key local; OK del usuario para F10 | M | ISS-117 | Checklist manual |

---

## Fase F10 — Centro de herramientas con IA (Etapa 3)

Historias: HU-39 a HU-43 (épica E9). Ampliada el 29 jul 2026
([[10-vision-v2]] §F10 y decisiones 7-9): cuatro sub-fases de menor a
mayor esfuerzo, cada una entregable y verificable por sí sola. Regla
transversal: límites diarios al estilo F9, estados amables, honestidad
en textos, e2e con stubs (sin gastar cuota) y AA/Lighthouse intactos.

**Referencia visual (10 ago 2026)**: el prototipo aprobado por el
usuario (decisiones 10-13 de [[10-vision-v2]]). Niveles de prueba por
issue según [[06-plan-de-pruebas]] §7 (Unit / Component / Integration /
API / E2E — CI ejecuta los cinco).

### F10a — Centro de herramientas vivo con el cotizador

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-119 | Documentación de F10 (épica E9, visión ampliada, backlog, copy de cotizador y centro en docs/08) | HU-39..43 | Docs 04/05/08/10 y CLAUDE.md antes del código | S | — | No aplica |
| ISS-120 | `contenido/cotizador.ts`: pasos, opciones y rangos configurables (propuesta del prototipo, a aprobar antes de publicar) | HU-39 | Rangos orientativos aprobables por el usuario; ADR-05 | S | ISS-119 | Unit (spec de datos) |
| ISS-121 | Componente del cotizador: wizard de 3 pasos con signals, barra de progreso, resultado por rango con resumen, CTA contacto/WhatsApp prellenado y "empezar de nuevo" | HU-39 | Sin backend; accesible; móvil 375px | M | ISS-120 | Unit + Component (`cotizador.spec`) |
| ISS-122 | Página VIVA `/herramientas` (decisión 11): cotizador integrado + demo destacado + secciones del diagnóstico/simulador en estado "Muy pronto" + banda de cuenta 5×; header/Home/sitemap | HU-43 | "Muy pronto" honesto; axe | M | ISS-121 | Component + E2E (`herramientas`, sitemap) |

### F10b — Simulador "un chatbot para tu negocio"

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-123 | Backend: plantilla de prompt segura del simulador (nombre/rubro injertados como datos, anti-inyección) reutilizando `GeneradorDeRespuestas` y límites F9; endpoint propio | HU-40 | El texto del visitante no puede alterar las reglas; IT con stub | M | ISS-119 | `SimuladorUseCaseTest`, IT |
| ISS-124 | Frontend: página del simulador (form negocio → chat demo) + e2e con stub | HU-40 | Estados de carga/error/límite; axe | M | ISS-122, ISS-123 | `simulador.spec`, e2e |

### F10c — Diagnóstico digital

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-125 | Backend: caso de uso del diagnóstico (respuestas del quiz → informe anclado con Groq, límites propios) + endpoint | HU-41 | Sin precios; 3 oportunidades concretas; IT con stub | M | ISS-119 | `DiagnosticoUseCaseTest`, IT |
| ISS-126 | Frontend: quiz de ~6 preguntas → informe en pantalla + CTA; e2e con stub | HU-41 | Informe legible en móvil; axe | M | ISS-122, ISS-125 | `diagnostico.spec`, e2e |

### F10d — Demo de diseño con IA (registrados)

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-127 | Puerto `GeneradorDeImagenes` + adaptador Gemini Flash Image (`GEMINI_API_KEY` por entorno, capa gratis ~500/día) | HU-42 | IT contra stub HTTP; key jamás logueada | M | ISS-119 | `GeminiAdapterIT` (stub) |
| ISS-128 | Backend: caso de uso del demo (imagen + funcionalidades con Groq; SOLO registrados; límites y variación única) | HU-42 | 401/403 correcto para anónimos; IT | L | ISS-127 | `DemoDeDisenoUseCaseTest`, IT |
| ISS-129 | Frontend: página del demo (form → generando → resultado como imagen + variación + CTA) y estado bloqueado para anónimos | HU-42 | Imagen nunca HTML; estados completos; axe | L | ISS-122, ISS-128 | `demo-diseno.spec` |
| ISS-130 | e2e del demo con stubs (Groq + Gemini) + CI | HU-42 | Flujo completo registrado y bloqueo anónimo | M | ISS-129 | `demo-diseno-e2e` |

### F10e — Rediseño de Home y servicios según el prototipo (decisión 12)

Los componentes de F10b/c/d se integran inline en `/herramientas` a
medida que cada sub-fase los active (reemplazan su tarjeta "Muy
pronto").

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-133 ✅ | Home rediseñada: hero con tarjeta del demo (estado según sub-fases vivas), sección centro de herramientas (4 tarjetas), sección asistente con preguntas sugeridas que abren el widget, tabla "visitante vs. con cuenta", placeholders honestos de casos/equipo, CTA de agenda | HU-34, HU-43 | Tokens F8.5 intactos; AA 4.5:1 (los textos translúcidos del prototipo se oscurecen); móvil 375 sin overflow | L | ISS-122 | Component + E2E + axe |
| ISS-134 ✅ | Páginas de servicio rediseñadas: breadcrumb, aside con CTA al diagnóstico, "lo que resolvemos", "cómo trabajamos", FAQ existente, CTA final | HU-43 | Contenido desde `contenido/` (ADR-05) | M | ISS-133 | Component + E2E + axe |
| ISS-135 ✅ | Header según prototipo: enlace Herramientas, doble CTA (agenda + cuenta), menú móvil verificado | HU-43 | Hidratación intacta (patrón `afterNextRender`) | S | ISS-133 | Component + E2E |

**Hallazgos de F10e** (los que valen para el resto del sitio quedan en
[[07-guia-de-estilo]] §Rediseño F10e):

- El e2e de zoom de texto al 200% atrapó un overflow horizontal real en
  la rejilla de herramientas de la Home: `1fr` deja que el contenido
  mínimo de una tarjeta empuje la columna. Se corrigió con
  `minmax(0, 1fr)` — convención nueva para todas las rejillas.
- El `AsistenteUiService` (nuevo, `nucleo/`) desacopla "abrir el
  asistente con esta pregunta" del componente del widget: la Home no
  conoce al chat, solo publica la intención. Contador de aperturas +
  pregunta de un solo uso para que repetir la misma sugerencia vuelva
  a disparar el efecto.
- El presupuesto `anyComponentStyle` de Angular (4 kB) ya no describía
  una página real: la Home rediseñada tiene siete secciones con estilos
  propios (7.1 kB). Subió a 8 kB de aviso / 12 kB de error.

### Seguimiento descubierto en producción (10 ago 2026)

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-136 | Rate limit por IP REAL detrás del proxy: `xfwd` en el proxy SSR + lectura confiable de `X-Forwarded-For` en el backend (evaluando el riesgo de spoofing porque el backend es públicamente accesible). Mientras tanto el techo del asistente sube a 600/15min vía Render (la protección fina son los cupos por sesión/usuario) | — | Límite por visitante real sin bloquear el tráfico legítimo | M | F10 | Integration + API |

Confirmado otra vez en producción el 10 ago 2026 tras publicar F10e:
`POST /api/asistente/mensajes` respondió 429 con cuerpo plano
`Too Many Requests` (firma del filtro por IP, no de la aplicación) sin
que el visitante hubiera gastado su cupo. Mientras el sync del
Blueprint no se acepte en Render, cualquier prueba real del asistente
en producción puede toparse con este techo.

### Proveedor de imágenes con respaldo (decisión 17 de [[10-vision-v2]])

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-137 ✅ | Adaptador de Cloudflare Workers AI + `GeneradorDeImagenesConRespaldo` (primario Cloudflare, respaldo automático Pollinations) y `ConfiguracionDeGeneradorDeImagenes` como único punto de decisión | HU-42 | Credenciales solo por entorno, jamás logueadas; el visitante nunca ve el fallo del primario | M | ISS-127 | `CloudflareGeneradorDeImagenesAdapterIT` (stub), `GeneradorDeImagenesConRespaldoTest` |

**Verificado con credenciales reales (10 ago 2026)**: token válido
(`/user/tokens/verify` → active), y el flujo completo del demo —
registro por API, verificación por Mailpit, login y
`POST /api/asistente/demo-diseno` — devolvió un boceto real en **2,4
segundos** (JPEG de ~275 KB) a través del adaptador nuevo. Ya está en
`render.yaml` con `DEMO_PROVEEDOR_IMAGENES=cloudflare`; falta que el
usuario ingrese las dos credenciales en el dashboard y acepte el sync.

**Hallazgo de calidad (mismo día)**: el prompt de imagen original
pedía "una sola pantalla principal" y "sin texto largo", y con
Cloudflare producía mockups casi vacíos (un teléfono con la pantalla en
blanco). El prompt nuevo va en inglés — los modelos de imagen rinden
bastante mejor — y pide explícitamente barra superior, barra lateral,
tarjetas y una lista; el resultado pasó a ser un dashboard creíble.
Segundo detalle: pedir "no prices, no currency symbols" **inducía** las
cifras en vez de evitarlas (los modelos de difusión ignoran las
negaciones), así que ahora se le dice qué poner en cada fila (nombre,
estado y hora). Lección transferible a cualquier prompt de imagen del
sitio: **describir lo que sí se quiere, nunca lo que no**.

### Frontend como Static Site (ADR-12, 11 ago 2026)

Fase corta de infraestructura, aprobada por el usuario tras comprobar
que el SSR no aportaba nada al SEO (todas las rutas públicas ya eran
prerender). Objetivo: que el sitio deje de dormirse y se sirva por CDN.

| ID | Descripción | Definición de hecho | Est. | Tests |
|---|---|---|---|---|
| ISS-157 | `sitemap.xml` y `robots.txt` generados en build desde `contenido/sitio.ts`, no en cada petición | La URL base sigue saliendo de un solo sitio (ADR-06); los archivos quedan en `dist` | S | Unit (los de `sitemap`/`robots` siguen valiendo) + verificación del archivo generado |
| ISS-158 | `render.yaml`: el frontend pasa a `type: web` estático con `rewrite` de `/api` al backend, fallback a `index.csr.html` y cabecera HSTS | ADR-09 intacto: un solo origen, sin CORS | S | Verificación manual del sitio desplegado |
| ISS-159 | Retirar `server.ts`, el `Dockerfile` del frontend y las dependencias de servidor (`express`, `compression`, `http-proxy-middleware`, `@angular/ssr` si deja de usarse) | El build no emite `server/`; CI sigue en verde | M | Las cuatro suites |
| ISS-160 | Ajustar la verificación local que dependía del servidor Node (Lighthouse y el runbook de e2e) y actualizar CLAUDE.md/docs | Lighthouse sigue midiendo el artefacto real de producción | S | Lighthouse ≥95/100/100/100 |

### Endurecimiento posterior al dominio propio

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-156 ✅ | HSTS emitido por el servidor SSR (no por Cloudflare — ADR-11 revisado): `max-age` desde `HSTS_MAX_AGE` con default de 1 día, `includeSubDomains`, sin `preload`, solo sobre https | — | Sale en `/` y en los estáticos; no rompe el desarrollo local por http | S | ADR-11 | Unit + Integration (Express montado igual que `server.ts`) |

Motivo del cambio de decisión: delegar HSTS en Cloudflare daba por
hecho que la zona propia estaría proxeada, y **Render ya sirve el sitio
detrás de su propio Cloudflare** (`x-render-origin-server: cloudflare`).
Encadenar otro proxy encima añade un salto que no aporta y puede
interferir con la validación ACME de los certificados de Render — una
renovación fallida tumba el sitio por una cabecera. Emitirla en Express
la deja versionada con el código y bajo nuestro control.

### Cierre F10

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-131 | Home/beneficios/registro: quitar "Muy pronto" a lo vivo; robots/sitemap; textos finales | HU-43, HU-34 | Honestidad al día en todo el sitio | S | por sub-fase | Specs ajustados |
| ISS-132 ✅ | Verificación integral (Lighthouse ≥95/100/100/100, axe, e2e completas, manual 375/1280, los 5 niveles en verde) + `render.yaml` + CLAUDE.md + OK del usuario | HU-39..43 | Prueba real con Groq/Cloudflare antes de publicar; cubre también F10e | M | todo F10 | Checklist manual |

**Fase F10 CERRADA el 10 ago 2026** con el OK explícito del usuario,
cumplida la regla dura del proyecto: suites en verde, ArchUnit en
verde y aprobación para esa fase concreta.

**Verificación de ISS-132 (10 ago 2026, local)** — falta solo la prueba
en producción tras el despliegue y el OK explícito del usuario:

| Comprobación | Resultado |
|---|---|
| Backend (`mvnw verify`: unit + ArchUnit + 88 ITs) | BUILD SUCCESS |
| Frontend (`npm test`) | 215 specs en verde |
| E2E Playwright + axe (`npm run e2e`) | 36 en verde, cero violaciones |
| Build de producción con SSR/prerender | 19 rutas, sin warnings |
| Lighthouse móvil sobre el build real (`/`, servicio, `/herramientas`, `/contacto`) | Performance 97-98 · Accesibilidad 100 · Buenas Prácticas 100 · SEO 100 |
| Manual 375 px / 1280 px (Home, servicio, herramientas, menú móvil) | Sin overflow horizontal, sin errores de consola |
| Sugerencias de la Home → abren el asistente y responden | Verificado en el build de producción |

**Verificación en producción (https://crearcodecesar.com, 10 ago
2026)** con navegador real, sin errores de consola ni overflow en 375 y
1280 px: tarjeta del demo con su ancla, 4 tarjetas de herramientas, 5
filas de la tabla de cuenta, 2 espacios reservados, 3 preguntas
sugeridas, doble CTA del header (escritorio y menú móvil), cero badges
"Muy pronto", miga de pan y aside del servicio, y el ancla
`/herramientas#diagnostico` posicionando en la herramienta correcta.

**Prueba real del asistente en producción (10 ago 2026, tras aceptar el
sync del Blueprint)**: superada. Desde la Home, una pregunta sugerida
abre el widget y Groq responde anclado al contexto ("tres líneas de
servicio…"); la pregunta "¿cuánto cuesta una app para mi restaurante?"
**no inventó ninguna cifra** y escaló a humano con WhatsApp y el
formulario de contacto. Cero errores de consola.

Durante el redeploy que dispara el sync, las llamadas siguieron dando
429 unos minutos: el contador del `RateLimitingFilter` vive en memoria
y la ventana vieja (techo 30) siguió vigente hasta que el proceso se
reinició con `RATE_LIMIT_ASISTENTE_MAX_INTENTOS=600`. Detalle a
recordar al diagnosticar: **ese 429 no lo escribe el filtro con cuerpo**
(hace `setStatus` sin cuerpo), así que un `Too Many Requests` con texto
plano puede venir de otra capa — la forma rápida de aislarlo es llamar
al backend directo (`crearcodecesar-backend.onrender.com`), que se
salta Cloudflare y el proxy SSR.

## Fase F11 — Gestión comercial interna (cotizaciones)

Descompuesta el 10 ago 2026 al arrancar la fase. Alcance en
[[10-vision-v2]] §F11 y decisiones 18-20 (cotizaciones sí, documentos
de cobro y DIAN no, rol único todavía, PDF con OpenPDF). Historias en
[[04-historias-de-usuario]] épica E10 (HU-44 a HU-48) y modelo en
[[03-modelo-de-dominio]] Parte 4. Niveles de prueba obligatorios por
issue según [[06-plan-de-pruebas]] §7.

### F11a — Dominio y casos de uso (sin infraestructura)

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-138 | Documentación de F11: alcance reenfocado, épica E10, modelo de dominio, este backlog | HU-44..48 | Docs antes del código; hallazgo fiscal registrado con fuentes | S | — | No aplica |
| ISS-139 | VOs del contexto: `Dinero` (aritmética y no negativo), `ItemDeCotizacion` (subtotal calculado), `NumeroDeCotizacion`, `DatosDelCliente`, `Porcentaje` | HU-44 | Sin Spring ni JPA (ArchUnit); totales solo calculados | M | ISS-138 | Unit |
| ISS-140 | `EstadoCotizacion` con su máquina de estados y `Cotizacion` con sus invariantes (inmutable tras enviar, mínimo un ítem, validez) | HU-44, HU-45 | Cada transición válida e inválida cubierta, como `EstadoSolicitudTest` | M | ISS-139 | Unit |
| ISS-141 | Puertos + casos de uso de borrador: abrir (desde lead o en blanco) y editar ítems | HU-44 | Fakes a mano, `Clock` fijo; el dominio nunca llama al reloj | M | ISS-140 | Unit (fakes) |
| ISS-142 | Caso de uso enviar: asigna consecutivo, congela la cotización, dispara el correo best-effort | HU-45 | Un fallo de correo no revierte el envío | M | ISS-141 | Unit (fakes) |
| ISS-143 | Casos de uso de consulta y respuesta: listar (equipo y cliente), obtener, aceptar/rechazar con validez y propiedad verificadas; al aceptar, el lead pasa a CONVERTIDA sin romperse si la transición no aplica | HU-46, HU-47 | Invariantes 4, 6 y 7 cubiertos con test | L | ISS-142 | Unit (fakes) |

### F11b — Infraestructura

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-144 | Migración `V6__cotizaciones.sql` (cotizaciones + ítems + consecutivos por año) y el cuarteto entidad/mapper/repositorio-adaptador | HU-44 | Patrón de `Solicitud*`; entidad JPA plana | L | ISS-140 | Integration (Testcontainers) |
| ISS-145 | `GeneradorDeNumeroDeCotizacion`: consecutivo atómico por año en la base de datos | HU-45 | Sin saltos ni repetidos bajo concurrencia — test con hilos simultáneos | M | ISS-144 | Integration |
| ISS-146 | Puerto `GeneradorDeDocumento` + adaptador OpenPDF con la plantilla de la cotización (identidad de la empresa, tabla de ítems, totales, validez, sello "cotización") | HU-48 | Dependencia nueva en `pom.xml`; el PDF abre y contiene los datos esperados | L | ISS-139 | Integration |
| ISS-147 | `EnviadorDeCotizaciones`: correo con el PDF adjunto (pasa de `SimpleMailMessage` a `MimeMessageHelper`) | HU-45 | IT con GreenMail verificando el adjunto | M | ISS-146 | Integration |
| ISS-148 | REST del equipo: crear, editar, enviar, listar, obtener y descargar (rol ADMIN) | HU-44, HU-45, HU-47 | `hasRole("ADMIN")` explícito; 409 en transición inválida | L | ISS-144 | API (IT REST) |
| ISS-149 | REST del cliente: listar las propias, obtener, aceptar/rechazar, descargar (autenticado) | HU-46 | Un cliente NO puede leer ni responder la cotización de otro — IT del acceso cruzado | L | ISS-148 | API (IT REST) |

### F11c — Frontend

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-150 | Panel: listado con filtro por estado y detalle con acciones (editar borrador, enviar, descargar, cancelar) | HU-44, HU-45, HU-47 | Estados de carga/error; sin SSR (`admin/**` sigue en Client) | L | ISS-148 | Component |
| ISS-151 | Formulario de cotización con ítems dinámicos y totales en vivo (calculados también en servidor — el frontend solo muestra) | HU-44 | Signal Forms; accesible con teclado | L | ISS-150 | Component |
| ISS-152 | `/mi-cuenta`: listado de cotizaciones del cliente, detalle, descargar PDF y aceptar/rechazar con confirmación | HU-46 | La cuenta pasa a servir para el negocio real, no solo cupos de IA | L | ISS-149 | Component |
| ISS-153 | Textos en `contenido/` (panel, cuenta, correo y PDF) y metadatos de las páginas nuevas | HU-44..48 | Contenido desacoplado (ADR-05); robots/sitemap al día | S | ISS-152 | Component |

### F11d — Cierre

| ID | Descripción | HU | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-154 ✅ | E2E del ciclo completo: el equipo crea desde un lead, envía, el cliente entra a su cuenta, descarga el PDF y acepta; el lead queda CONVERTIDO | HU-44..48 | Correo leído de Mailpit como en `cuentas-e2e`; axe sin violaciones | L | ISS-152 | E2E |
| ISS-155 ✅ | Cierre de fase: suites en verde, ArchUnit, Lighthouse, revisión manual 375/1280, docs y CLAUDE.md al día, OK del usuario | HU-44..48 | Regla dura del proyecto | M | todo F11 | Checklist manual |

**Fase F11 CERRADA el 11 ago 2026** con el OK explícito del usuario,
cumplida la regla dura: suites en verde, ArchUnit en verde y aprobación
para esa fase concreta.

**Estado al 11 ago 2026**: ISS-138 a ISS-154 implementados. Verificación
de las cuatro suites:

| Comprobación | Resultado |
|---|---|
| Backend (`mvnw verify`: unitarios + ArchUnit + ITs) | 129 ITs, BUILD SUCCESS |
| Frontend (`npm test`) | 244 specs |
| E2E Playwright + axe | 38, cero violaciones |
| Lighthouse móvil sobre el build de producción | Performance 96-97 · Accesibilidad 100 · Buenas Prácticas 100 · SEO 100 |

**Hallazgos de F11** (los que valen más allá de la fase):

- `List.of(...).contains(null)` lanza `NullPointerException`: validar
  una lista inmutable buscando nulos así explota. Se usa
  `stream().anyMatch(Objects::isNull)`.
- El badge `CONVERTIDA` del panel arrastraba un contraste de 4.11:1
  desde F5, por debajo del mínimo AA. Ningún test lo había visto porque
  ninguno dejaba una solicitud en ese estado; el e2e del ciclo
  comercial sí, y axe lo atrapó. Corregido oscureciendo el texto.
- Los ITs que necesitan varios clientes distintos emiten el token con
  el generador real en vez de pasar por `POST /api/auth/login`: ese
  endpoint tiene rate limit de 5/15 min por IP y la clase entera se
  quedaba sin cupo.
- `MimeMessageHelper` anida multiparts (mixed → alternative): para leer
  el cuerpo de un correo con adjunto en un test hay que recorrerlos en
  profundidad, no solo el primer nivel.

**Revisión manual en navegador (11 ago 2026)**: panel de cotizaciones
(listado, apertura y detalle editable) y `/mi-cuenta/cotizaciones` en
1280 y 375 px, sin overflow horizontal ni errores de consola. Se
corrigió el margen de las páginas nuevas del panel, que salían pegadas
al borde por faltarles el `.contenedor` del resto del sitio.

**Datos de la empresa (recibidos el 11 ago 2026)**, tomados del
certificado de existencia y representación legal de la Cámara de
Comercio de Valledupar (matrícula 220369, expedido el 16 jul 2026) y
ya cargados como defaults en `application.properties`:

- **NIT 901941017-0** y razón social CREAR CODE CESAR S.A.S.
- **Dirección fiscal**: Calle 4B # 20-36, Oficina 303, Barrio Callejas,
  Valledupar, Cesar.
- Contexto útil: microempresa, Grupo NIIF III, constituida el 10 mar
  2025; representante legal Juan Carlos Gutiérrez Huérfano.

El certificado **confirma la decisión 18**: es una persona jurídica
(S.A.S.), luego obligada a factura electrónica — la cotización no puede
hacer las veces de documento de cobro.

**Aclaración fiscal (11 ago 2026)**: el usuario indicó que la empresa
"está en régimen simplificado y aún no ha facturado". Dos cosas a
tener en cuenta antes de la primera cotización real:

1. **El "régimen simplificado" ya no existe** como figura: lo
   eliminaron la Ley 1943 de 2018 y la Ley 2010 de 2019. Hoy la
   clasificación es **responsable / no responsable de IVA**.
2. **"No responsable de IVA" aplica solo a personas naturales.** Una
   S.A.S. es persona jurídica, así que por regla general **es
   responsable de IVA** — el umbral de 3.500 UVT no la exime.

Consecuencia práctica: cotizar sin IVA y descubrir después que había
que facturarlo obliga a pedirle al cliente un 19% adicional sobre algo
que ya aceptó, o a asumirlo del margen. Conviene confirmarlo con el
contador **antes** de enviar la primera cotización, mirando las
responsabilidades de la casilla 53 del RUT (código 48 = responsable de
IVA). *Esto no es asesoría fiscal.* Fuentes:
[responsable de IVA 2026](https://blog.alegra.com/colombia/responsable-de-iva/),
[régimen simplificado](https://www.gerencie.com/regimen-simplificado.html).

**Preparación para facturar** (cuando salga al mercado; no lo cubre
esta app, que solo emite cotizaciones):

1. Habilitarse como facturador electrónico en el portal de la DIAN.
2. Solicitar la **resolución de numeración** (prefijo y rango, vigencia
   de 1 a 5 años).
3. Configurar el modo de operación y superar el **set de pruebas** para
   quedar en estado HABILITADO; asociar los prefijos ~1 hora después de
   pedir la resolución.
4. Elegir cómo se emite: el sistema gratuito de la DIAN o un proveedor
   con API (Alegra desde ~$18.000 COP/mes) si se quiere automatizar
   desde esta app más adelante.

Guías: [pasos de habilitación](https://ayuda.alegra.com/col/pasos-habilitacion-facturador-electronico-dian),
[facturación gratuita DIAN](https://micrositios.dian.gov.co/sistema-de-facturacion-electronica/guia-de-facturacion-gratuita-dian/).

**Sigue pendiente del usuario**:

- **Confirmar con el contador la condición de IVA** y, con eso, el
  porcentaje por defecto de las cotizaciones. El impuesto ya es
  configurable **por cotización** (se elige al abrirla), así que el
  sistema soporta los dos escenarios sin tocar código.
- Validez por defecto (hoy 15 días) y condiciones comerciales estándar
  (anticipo, forma de pago) para el pie del documento.
- Datos de contacto del documento: desde el 11 ago 2026 el correo
  corporativo es **`admin@crearcodecesar.com`** (dominio propio), y es
  el que aparece en el sitio, el PDF y el asistente. El certificado
  registra `juancgutierrezh@gmail.com` y los teléfonos 3015791952 /
  3145408191 como datos de notificación judicial; el sitio usa el
  WhatsApp comercial 323 988 5883. Conviene decidir si en la cotización
  va ese WhatsApp o alguno de los teléfonos registrados.

**Nota de dependencia**: el correo de producción sigue pospuesto hasta
las pruebas del MVP, así que HU-45 se verifica en local contra Mailpit;
en producción el envío quedará operativo el día que se carguen
`MAIL_USERNAME`/`MAIL_PASSWORD` en Render. Mientras tanto el PDF se
descarga y se comparte a mano, que es justamente el camino de respaldo
que exige la propia HU.

---

## Auditoría integral del 28 sep 2026 — ISS-161 a ISS-178

Auditoría externa (documento "Auditoría integral — Crear Code Cesar",
25 secciones: QA, UX, CRO, copy, SEO, seguridad, privacidad,
herramientas de IA, funnel y roadmap). Su hallazgo de fondo no era
técnico: *"un escaparate de ingeniería que hoy no convierte, y su gancho
principal está caído"*. Lo técnico se atacó de inmediato; lo comercial
son decisiones del usuario y quedan abajo sin ✅.

### Hecho el mismo día

| ID | Descripción | Hallazgo | Definición de hecho | Est. | Depende de | Tests |
|---|---|---|---|---|---|---|
| ISS-161 ✅ | Asistente caído: Groq retiró `llama-3.3-70b-versatile` (404 `model_not_found`). Modelo por defecto → `openai/gpt-oss-120b`; el 503 deja un `WARN` con la causa, nunca la conversación | C1, C2 | Las tres herramientas responden 200 en producción; el IT exige el log | S | — | IT del controlador (log capturado) + IT del adaptador |
| ISS-162 ✅ | Fricción: `mailto:`/`wa.me` en el pie, tarjetas de herramientas enlazadas a su ancla, botón Atrás en el cotizador, menú móvil con X y WhatsApp | QA4, QA5, QA11, QA15 | 4 specs nuevos; WhatsApp solo dentro del envoltorio que el CSS oculta en escritorio (F10e se conserva) | M | — | Component |
| ISS-163 ✅ | Cabeceras `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`; HSTS a un año; página 404 con salidas servida por Caddy con estado 404 | QA6, §10 | La verificación de rutas exige el título "no encontrada" | S | ADR-13 | `verificar-rutas-servidas` en CI |
| ISS-164 ✅ | Tope de 2000 caracteres al mensaje de contacto en dominio, DTO (`@Size`) y formulario (`maxLength` de Signal Forms) | QA13 | 400 con mensaje claro; el textarea lo proyecta como `maxlength` | S | — | Unit (dominio) + IT (REST) + Component |
| ISS-165 ✅ | Reserva atómica del cupo de IA: `ContadorDiario.reservar()` antes de llamar al proveedor y `liberar()` si sobra o falla, en los cuatro casos de uso | §10 (alta) | Test de concurrencia: 18 peticiones simultáneas con cupo 6 → 6 llamadas (antes 18) | M | — | Unit (concurrencia) + suites de los 4 casos de uso |
| ISS-166 ✅ | SEO: JSON-LD `ProfessionalService` con NAP, NIT y fundador (desde constantes, ADR-06); `og:locale`, `og:site_name`, `twitter:*`; NIT y dirección en el pie; título de la Home de 80 a 55 caracteres | §7 | Comprobado en el HTML prerenderizado de la imagen | M | — | Unit (`datos-estructurados`, `metadatos-pagina`) + Component (footer) |
| ISS-167 ✅ | Aviso "no escribas datos personales de tus clientes" en las cuatro herramientas de IA (`AVISO_IA`) y WhatsApp en los mensajes de caída | §11, §12 | Un solo texto en `contenido/legales.ts`; 4 specs | S | — | Component |
| ISS-168 ✅ | README y AGENTS.md sin Render, Neon ni Gmail; AGENTS.md pasa a puntero a CLAUDE.md | §18 | `monday-app-association.json` NO se borra: lo exige monday para board-checkup | S | — | — |

### Pendiente — necesita una decisión o una acción del usuario

| ID | Descripción | Hallazgo | Qué falta del usuario | Est. |
|---|---|---|---|---|
| ISS-169 ✅(código) | Política de datos definitiva: quitar "borrador", nombrar encargados (Groq, Cloudflare, Pollinations, Resend, Netcup, Google Fonts), transferencia internacional, retención de conversaciones, derechos ARCO | C3, §11 | Revisión legal del texto que se redacte; decidir si aplica el RNBD de la SIC | M |
| ISS-170 ✅(código) | Analítica: GA4 + Clarity con los eventos de §13 (`whatsapp_click`, `tool_complete`, `lead_captured`…) y banner de consentimiento | C4 | Crear las propiedades (IDs de GA4/Clarity) y aprobar el banner | M |
| ISS-171 ◐ | Bajar `RATE_LIMIT_ASISTENTE_MAX_INTENTOS` de 600 a ~30 e identificar al anónimo por IP con hash y sal diaria | C5 | Hacer primero la prueba de ISS-136 desde dos redes: sin IP real por visitante, bajar el límite tumba las herramientas para todos | S |
| ISS-172 ◐ | Cotizador: que el tipo de proyecto y la urgencia cambien el rango; rangos separados para web y para sistema; producto de entrada barato | QA3, §5 | Los números — el sitio no publica precios inventados | M |
| ISS-173 | Teléfono del formulario: aceptar fijos e internacionales además del celular colombiano | QA12 | Decidir si se quieren leads fuera de Colombia y fijos | S |
| ISS-174 | Corpus Damana: moverlo al servidor propio (mismo patrón Kamal) o advertir la espera de Render en la tarjeta | C6, QA7 | Decidir cuál | S/M |
| ISS-175 ✅ | Enviar el correo del lead nuevo fuera de la transacción (tras commit, asíncrono) para que un Resend lento no retenga la respuesta hasta 10 s | QA14 | — (solo prioridad) | S |
| ISS-176 | Monitor externo de disponibilidad (home + endpoint sintético de IA) con alerta | C2, §19 | Elegir servicio (UptimeRobot / Better Stack) y crear la cuenta | S |
| ISS-177 | Copias de seguridad diarias del PostgreSQL compartido fuera del VPS | C7 | Decidir destino (R2 ya existe para UparYa) — antes de salir al mercado | M |
| ISS-178 ◐ | CSP en modo Report-Only y luego en firme | §10 | Recorrer el sitio sin avisos en consola y pasarla a firme | M |

### Segunda tanda — rama `fix/auditoria-sep-2026` (28 sep 2026)

El usuario pidió implementar el listado priorizado completo de la
auditoría (P0 a P3). Antes de tocar nada se verificó cada hallazgo
contra el código: de 48 sub-hallazgos, **10 ya estaban hechos** en la
primera tanda, **1 no aplicaba** y el resto se corrigió aquí. Un
commit por identificador de la auditoría.

| ID auditoría | Qué se hizo | ISS |
|---|---|---|
| P0-1a | El 503 de cupo agotado y el de proveedor caído dejan de compartir código: `limite-global` (INFO, vuelve mañana) y `proveedor-caido` (WARN, avería). **Cambia una decisión documentada** — la invariante 3 decía que para el visitante eran lo mismo | ISS-179 |
| P0-1c | `GeneradorDeRespuestasConRespaldo`: si el modelo primario falla, responde `GROQ_MODELO_RESPALDO`. Es lo que faltó el día de la caída — el demo, que sí tenía respaldo, siguió funcionando con el mismo proveedor roto | ISS-180 |
| P0-1d | El CTA de WhatsApp existe de verdad en los tres mensajes de caída; ISS-167 cambió el texto pero no añadió enlace | ISS-181 |
| P0-2a | El cupo anónimo ya no se resetea borrando `sessionStorage`: techo por red con `SHA-256(sal del día + IP)`, nunca la IP. Se cuentan las dos cosas para no castigar a una oficina con NAT | ISS-182 |
| P0-2d | `CupoDeIa`: las ~20 líneas de cupos duplicadas en los 4 casos de uso (y sus 8 mapas) pasan a un componente con su prueba de concurrencia | ISS-183 |
| P0-2e | El rate limiter suelta las ventanas vencidas; acumulaba una entrada por IP y regla desde el último despliegue | ISS-184 |
| P0-3 | Política de datos completa (9 secciones, seis encargados nombrados, transferencia internacional, retención, ARCO) y sin el aviso de borrador. Marcada `REVISAR CON ABOGADO` | ISS-169 |
| P0-4 | La app no arranca en producción con `ADMIN_PASSWORD`/`JWT_SECRET` por defecto (`PERMITIR_CREDENCIALES_DE_DESARROLLO=false` en `deploy.api.yml`). Bandera explícita porque el proyecto no usa perfiles de Spring | ISS-185 |
| P1-1 | Analítica detrás de consentimiento: no carga nada antes de aceptar, no mide en desarrollo, ningún dato personal en los eventos. Banner que pregunta sin bloquear | ISS-170 |
| P1-2a | Matriz de rango por tipo × alcance (12 cifras, todas con `TODO dueño`) y nota por urgencia; antes las 36 combinaciones daban 3 resultados | ISS-172 |
| P1-3 | El WhatsApp del cierre del diagnóstico lleva las tres oportunidades | ISS-186 |
| P1-4 | Botón flotante de WhatsApp bajo 60rem, apilado sobre la burbuja del asistente | ISS-187 |
| P1-6b/c/d/e | `Article` y `BreadcrumbList`, `og:type=article`, `lastmod` solo donde hay fecha real, título de la Home con "Valledupar" en 49 caracteres | ISS-188 |
| P1-7 | NAP visible en el cuerpo de `/contacto` | ISS-188 |
| P1-8 | Inyección: la conversación debe alternar (corta el bloque de turnos falsos), `DatoDelVisitante` escapa lo que el visitante escribe en los tres prompts, y al proveedor de imágenes solo le llega el título generado | ISS-189 |
| P2-1 | CSP en `Report-Only` | ISS-178 |
| P2-2b/c/d | El formulario espeja los máximos del dominio, muestra el motivo real del 400 y enfoca el primer campo inválido | ISS-190 |
| P2-2e | El correo del lead sale tras el commit y fuera de la petición (`AFTER_COMMIT` + `@Async` sobre hilos virtuales) | ISS-175 |
| P2-2f | "Te respondemos el mismo día hábil". **Es una promesa**: se revierte en una línea | ISS-191 |
| P2-5a | Enlace "Saltar al contenido"; axe no lo delataba porque `bypass` se satisface con `<main>` | ISS-192 |
| P2-7 | La sesión se cierra al vencer el token, en vez de esperar al 401 | ISS-193 |
| P3-a | Señuelo de BCrypt cuando el correo no existe: la diferencia de tiempo delataba qué correos tienen cuenta | ISS-194 |
| P3-b | `USER` sin privilegios en las dos imágenes | ISS-195 |
| P3-c | ESLint con las reglas de Angular, corriendo en CI antes de los tests | ISS-196 |
| P3-e | `docker-compose.yml` (el perfil `full` estaba roto: publicaba el 4000 y Caddy escucha en 8080), `ci.yml`, `sitio.ts`, `docs/02`, `README`, `asistente-contexto.md` | ISS-197 |

**Verificado, no corregido** (el hallazgo no aplica):

- **P3-d, código muerto.** Ni el adaptador de Gemini —rama viva del
  switch con `DEMO_PROVEEDOR_IMAGENES=gemini`, documentada— ni
  `monday-app-association.json`, que exige el marketplace de monday.
  Lo único real era la línea de `docs/02` que decía que lo servía
  Express; corregida en P3-e.

### Política de datos v2 — texto legal aportado por el usuario (28 sep 2026)

El usuario aportó un documento legal propio, más riguroso que el
borrador que se había redactado: cita el marco vigente (Ley 1581,
Decreto 1074 de 2015, Circular Única SIC Título V, **Circular Externa
002 de 2024 sobre IA**, Ley 2300 de 2023), fija plazos y retenciones
con números, y nombra encargados que faltaban — **Meta Platforms**
(WhatsApp) y Microsoft. Está publicado en `contenido/legales.ts`
(ISS-169) y la versión que se guarda como prueba con cada solicitud
sube a **v2** (`VERSION_POLITICA_DATOS`).

De los seis `[CONFIRMAR]` del documento:

| Punto | Resolución |
|---|---|
| Conversaciones de IA no se guardan | **Confirmado en el código**: ninguna entidad JPA las persiste y el manejador registra la causa técnica sin el texto del visitante, con un IT que lo exige |
| Copias de seguridad | **No se publican**: todavía no existen. Una política que promete lo que no se hace es peor que una incompleta |
| Pollinations | Se declara como encargado, con el matiz de que desde P1-8c solo recibe el título que generó el modelo |
| Entrenamiento con los datos | Se publica lo que nosotros no autorizamos, no lo que hacen ellos. Falta confirmar los términos de API de Groq y Cloudflare |
| Ley 2300 (horarios de contacto comercial) | La política se remite a la ley sin transcribir el horario; hay que cumplirlo desde el primer envío comercial |
| Plazo de 15 días hábiles para reportar incidentes | Se publica como compromiso; falta confirmar el plazo exacto |

**Checklist del documento que sigue pendiente** (cada uno toca código
u operación, y ninguno estaba en el alcance de la auditoría técnica):

- [ ] **Casilla comercial separada** de la obligatoria en el formulario
  (la política, sección 13, ya la promete). Es campo nuevo en el
  dominio, migración y DTO. **Bloquea el primer envío comercial**, no
  el despliegue: hoy no se envía ninguno.
- [ ] Botón "Eliminar mi cuenta" en `/mi-cuenta`. Hoy se pide por
  correo, que es lo que dice la política.
- [ ] Tarea programada que borre solicitudes de más de 24 meses y
  cuentas con 24 meses sin uso (la política fija esos plazos).
- [ ] Copias de seguridad de PostgreSQL — y entonces añadir su línea a
  la sección 12.
- [ ] Evaluación de impacto de las herramientas de IA (Circular 002 de
  2024): documento corto de riesgos, datos, proveedores y medidas, a
  revisar una vez al año. La política ya lo promete.
- [ ] Procedimiento interno de una página para responder consultas y
  reclamos dentro de los plazos y registrar incidentes.
- [ ] **RNBD**: obligatorio solo por encima de 100.000 UVT de activos
  (Decreto 090 de 2018) — confirmar con el contador.
- [ ] Seguimiento al Proyecto de Ley 282 de 2026 Cámara.

**Lo que sigue esperando al usuario**: los ids de GA4 y Clarity
(ISS-170), la revisión legal de la política (ISS-169), las doce cifras
del cotizador (ISS-172), la prueba de ISS-136 antes de bajar el límite
de 600 (ISS-171), pasar la CSP de Report-Only a firme (ISS-178), y
P2-6 —autoalojar las fuentes— que quedó sin hacer: son siete archivos
`.woff2` que hay que descargar y versionar, y es una decisión sobre
qué tipografías se quedan.

**Decisiones de producto que la auditoría propone y que no son código
hasta que el usuario decida**: quitar "Crear cuenta" del header y la
tabla de cupos de la Home; sacar el demo de diseño del hero; nombrar
los servicios por resultado en vez de por categoría; landings por
sector con precio "desde"; captura de WhatsApp al final del diagnóstico
y del cotizador; calendario real para la consulta; "el mismo día hábil"
en vez de "pronto"; foto del fundador; Google Business Profile; activar
el proxy de Cloudflare; apagar Render y revocar las llaves rotadas.

**Estado al cierre de la segunda tanda (28 sep 2026)**: backend con
`mvn verify` completo en verde (138 tests, ArchUnit y los ITs con
Testcontainers) y **296 specs de frontend**, ESLint limpio. La rama
`fix/auditoria-sep-2026` NO está desplegada: falta el OK del usuario y
los e2e completos con backend.

---

## Resumen de cobertura

Todas las HU de [[04-historias-de-usuario]] (29 de la Etapa 2, HU-30 a
HU-33 de la fase F8 y HU-34/HU-35 de la fase F8.5) quedan cubiertas por
al menos un issue de este backlog; ninguna HU queda sin issue asociado.
El orden de fases F0→F7 coincide con el propuesto para la Etapa 2 en el
brief original y se retoma en `CLAUDE.md`.
