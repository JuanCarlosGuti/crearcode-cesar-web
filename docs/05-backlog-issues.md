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

**Fase F11**: sigue sin descomponer — se descompone al arrancar, según
[[10-vision-v2]].

---

## Resumen de cobertura

Todas las HU de [[04-historias-de-usuario]] (29 de la Etapa 2, HU-30 a
HU-33 de la fase F8 y HU-34/HU-35 de la fase F8.5) quedan cubiertas por
al menos un issue de este backlog; ninguna HU queda sin issue asociado.
El orden de fases F0→F7 coincide con el propuesto para la Etapa 2 en el
brief original y se retoma en `CLAUDE.md`.
