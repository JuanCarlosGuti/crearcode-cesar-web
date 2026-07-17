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
| ISS-080 | Parametrizar configuración agnóstica al dominio | HU-29 | `baseUrl`, CORS y `OG:url` se leen de variables de entorno, sin valores hardcodeados | S | ISS-075 | `ConfiguracionAgnosticaDominioTest` |
| ISS-081 | Pipeline de build de producción | HU-29 | Genera build SSR de frontend y artefacto/imagen de backend listos para desplegar | M | ISS-009, ISS-045 | Verificación de build en CI |
| ISS-082 | Checkpoint de decisión de publicación con el usuario | HU-29 | El usuario aprueba explícitamente publicar, con costos ya documentados en ISS-079 | S | ISS-079, ISS-081 | No aplica (decisión humana, no técnica) |

---

## Resumen de cobertura

Las 29 HU de [[04-historias-de-usuario]] quedan cubiertas por al menos un
issue de este backlog; ninguna HU queda sin issue asociado. El orden de
fases F0→F7 coincide con el propuesto para la Etapa 2 en el brief
original y se retoma en `CLAUDE.md`.
