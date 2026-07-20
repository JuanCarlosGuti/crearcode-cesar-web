# 06 — Plan de pruebas

## 1. Estrategia general: TDD

En la Etapa 2, cada issue del backlog ([[05-backlog-issues]]) se
desarrolla con **TDD**: primero el test que describe el comportamiento
esperado (incluyendo el caso triste), luego la implementación mínima
para pasarlo, luego refactor si aplica. Ningún issue se da por
terminado sin sus tests nombrados en verde.

Regla dura: **no se avanza de una fase (F0-F7) a la siguiente sin que
todos los tests de esa fase y ArchUnit estén en verde**, además del OK
explícito del usuario (ver `CLAUDE.md`).

## 2. Pirámide de pruebas — backend

```
                    ▲
                   ╱ ╲        e2e (pocos)
                  ╱───╲       flujo completo de contacto
                 ╱     ╲
                ╱───────╲     integración (Testcontainers)
               ╱         ╲    repositorio JPA, controladores REST,
              ╱           ╲   adaptador de correo
             ╱─────────────╲
            ╱               ╲ casos de uso (puertos falsos)
           ╱                 ╲ aplicacion/ con fakes de puertos
          ╱───────────────────╲
         ╱                     ╲ unitarias de dominio
        ╱                       ╲ VOs, entidad, máquina de estados
       ╱─────────────────────────╲
      ArchUnit — reglas de dependencia (transversal a toda la pirámide)
```

### 2.1 Unitarias de dominio (`dominio/`)
- Sin Spring, sin base de datos, sin mocks de framework — solo Java
  puro y, si acaso, JUnit 5 + AssertJ.
- Cubren: `Correo`, `Telefono`, `DatosDeContacto`, `EstadoSolicitud`
  (máquina de estados completa, incluidas transiciones inválidas y
  estados terminales), `ConsentimientoDatos`, `SolicitudDeContacto`
  (factoría `registrar()` e invariantes).
- Deben ejecutar en milisegundos y ser la mayoría de los tests del
  backend.
- Ejemplos de test nombrados: `CorreoTest`, `TelefonoTest`,
  `EstadoSolicitudTest`, `SolicitudDeContactoTest`.

### 2.2 Casos de uso con puertos falsos (`aplicacion/`)
- Se testean `RegistrarSolicitudUseCase`, `CambiarEstadoSolicitudUseCase`
  y `ListarSolicitudesUseCase` inyectando implementaciones falsas
  (in-memory) de `SolicitudRepositorio` y `NotificadorPort` — no mocks
  de framework pesados, fakes simples escritos a mano o con Mockito
  puntualmente donde aporte claridad.
- Verifican orquestación: que se llama al repositorio, que se llama al
  notificador después de persistir (no antes), que un fallo del
  notificador no revierte la persistencia.
- Ejemplos: `RegistrarSolicitudUseCaseTest`,
  `CambiarEstadoSolicitudUseCaseTest`, `ListarSolicitudesUseCaseTest`.

### 2.3 Integración con Testcontainers (`infraestructura/`)
- Contra PostgreSQL real (contenedor efímero), no H2 ni bases en
  memoria — para detectar problemas reales de mapeo/migraciones.
- Cubren: `SolicitudRepositorioIT` (persistencia real),
  `SolicitudControllerIT` (MockMvc o WebTestClient contra el contexto
  Spring completo, incluye casos tristes: payload inválido, honeypot,
  transición de estado inválida, acceso sin autenticación),
  `NotificadorEmailAdapterIT` (servidor SMTP de prueba tipo GreenMail),
  `SeguridadAdminIT`, `RateLimitingFilterIT`.

### 2.4 e2e mínimo del flujo de contacto
- Un único test e2e de extremo a extremo que valida el flujo crítico de
  negocio: llenar el formulario en el frontend → enviar → verificar que
  la solicitud quedó registrada (vía API de verificación o consulta
  directa a BD de prueba) → verificar que se intentó la notificación.
- Se mantiene deliberadamente mínimo (no se multiplica en variantes,
  esas ya están cubiertas en niveles inferiores de la pirámide).
- `contacto-e2e.spec.ts` (Playwright o equivalente definido en F4).

### 2.5 ArchUnit — transversal
- No es un nivel de la pirámide sino una verificación transversal que
  corre en cada build (ver ISS-005 en [[05-backlog-issues]]).
- Reglas mínimas: `dominio/` no importa `org.springframework.*` ni
  `jakarta.persistence.*`; `dominio/` no depende de `infraestructura/`;
  `aplicacion/` no depende de `infraestructura/` (solo de las
  interfaces de puerto en `dominio/`).
- Test: `ArchitectureRulesTest`.

## 3. Pruebas — frontend (Angular 22)

### 3.1 Vitest (unitarias/componentes)
- Reemplazo oficial de Karma para este stack; corre sobre los mismos
  componentes standalone sin necesidad de `TestBed` pesado cuando el
  componente es simple.
- Componentes clave a cubrir: layout (header/footer con CTA),
  formulario de contacto completo (incluye Signal Forms), tarjetas de
  servicio, listado y filtro del panel admin, guard de rutas admin.
- Foco especial en **Signal Forms del formulario de contacto**: cada
  regla de validación (correo, teléfono, campos obligatorios,
  consentimiento) tiene su propio caso de test, incluidos los casos
  tristes (valor inválido, campo vacío, envío sin consentimiento).
- Ejemplos: `formulario-contacto.component.spec.ts`,
  `layout.component.spec.ts`, `listado-solicitudes.component.spec.ts`,
  `admin.guard.spec.ts`.

### 3.2 e2e del formulario
- Cubierto por el e2e mínimo descrito en §2.4 (un solo flujo crítico,
  ejecutado end-to-end contra backend real de prueba).
- No se añaden más e2e de frontend en v1 salvo que se detecte una
  regresión recurrente que solo un e2e pueda prevenir.

## 4. Umbrales de cobertura

- **Dominio (`dominio/`)**: **90% de cobertura de línea** como mínimo,
  medido en CI. Es la capa con más valor de negocio y menor costo de
  testear, así que el umbral es alto.
- **Aplicación (`aplicacion/`)**: 80% de cobertura de línea.
- **Infraestructura (`infraestructura/`)**: sin umbral numérico estricto
  (mucho es configuración/mapeo); se exige que cada adaptador tenga al
  menos un test de integración que ejercite su camino feliz y su
  principal camino triste.
- **Frontend**: sin umbral numérico estricto en v1; se exige que todo
  componente con lógica (no solo presentación) tenga tests, con
  prioridad total en el formulario de contacto (es el componente que
  materializa dinero/oportunidad de negocio).
- Los umbrales se revisan al cierre de la fase F2 (backend) y F4
  (frontend) con datos reales de cobertura; si un umbral resulta
  inalcanzable o inútil en la práctica, se ajusta aquí mismo con
  justificación, no se ignora en silencio.

## 5. Checklist de accesibilidad

A aplicar sobre Home, una página de servicio, Contacto y el panel admin
antes de cerrar la fase F6 ([[05-backlog-issues]] ISS-078):

- [x] Toda la navegación es posible solo con teclado (Tab/Shift+Tab/
      Enter/Espacio), sin trampas de foco.
- [x] El foco activo es siempre visible (outline perceptible, no
      eliminado por CSS sin reemplazo).
- [x] Toda imagen con significado tiene `alt` descriptivo; las
      puramente decorativas usan `alt=""`. (El sitio no tiene aún
      ninguna imagen de contenido — ver [[07-guia-de-estilo]] §Imágenes.)
- [x] Contraste de texto sobre fondo cumple WCAG AA (4.5:1 texto
      normal, 3:1 texto grande) en toda la paleta elegida
      ([[07-guia-de-estilo]]).
- [x] Todo campo de formulario tiene `label` asociado programáticamente
      (no solo placeholder).
- [x] Los mensajes de error de validación se anuncian a tecnología de
      asistencia (`aria-live` o asociación `aria-describedby`) y están
      vinculados a su campo.
- [x] El honeypot no es alcanzable ni anunciado por navegación de
      teclado/lector de pantalla para un usuario real.
- [x] Jerarquía de encabezados (`h1`→`h2`→`h3`) es lógica y no se salta
      niveles por razones puramente visuales.
- [x] El sitio es usable con zoom de texto al 200% sin pérdida de
      contenido ni funcionalidad.
- [x] Auditoría Lighthouse Accesibilidad ≥ 90 en las páginas listadas.

**Verificado (ISS-078)**: automatizado con axe-core vía Playwright
(`frontend/e2e/accesibilidad-e2e.spec.ts`) sobre Home, la página de
Desarrollo a la medida, Contacto, y el panel admin (login, listado y
detalle autenticados) — cero violaciones. Halló y permitió corregir dos
bugs reales antes de cerrar F6: el CTA "Agenda tu consulta" del header
se leía casi ilegible (contraste 1.28:1, colisión de especificidad CSS
entre `.cabecera__nav a` y `.boton-primario`) y el CTA doble del footer
desbordaba horizontalmente al probar zoom de texto 200% (breakpoint de
`.cta-doble` basado en ancho de viewport, no de columna). Lighthouse
Accesibilidad final: 100 en las 3 páginas públicas auditadas
([[05-backlog-issues]] ISS-077).

## 6. Qué NO se prueba exhaustivamente en v1 (a propósito)

- Variantes visuales de responsive en todos los breakpoints posibles:
  se verifican los breakpoints clave (móvil pequeño, tablet, desktop),
  no una matriz exhaustiva de dispositivos.
- Rendimiento bajo carga/concurrencia alta: fuera de alcance de v1 (un
  sitio de leads de pyme no requiere pruebas de carga en este punto);
  se reconsidera si el volumen de tráfico lo justifica.
- Pruebas de penetración formales: la seguridad se cubre con las
  prácticas de la épica E2/E3 (validación, rate limiting, Spring
  Security) pero un pentest formal queda fuera de v1.
