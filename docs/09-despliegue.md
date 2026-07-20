# 09 — Despliegue

Cubre ISS-079 a ISS-082 de la fase F7 ([[05-backlog-issues]]) y HU-29
([[04-historias-de-usuario]]). Este documento es investigación +
recomendación — la decisión final (qué hosting, qué registrador, y
si publicar ya) es del usuario (ISS-082).

## 1. Qué hay que desplegar

Tres piezas, ninguna con Dockerfile todavía (ISS-081 las crea):

- **Backend**: JAR de Spring Boot (JVM) + PostgreSQL con Flyway.
- **Frontend**: servidor Node/Express con SSR (`dist/frontend/server/server.mjs`,
  ver [[07-guia-de-estilo]] y `frontend/src/server.ts`) — no es un sitio
  100% estático: sirve `/sitemap.xml` y `/robots.txt` dinámicos (ISS-073/074)
  además de las páginas prerenderizadas.
- **Dominio**: pendiente de compra, ~$60.000-80.000 COP/año estimado en
  la propia HU-29.

## 2. Decisión de arquitectura pendiente: un dominio o dos

Hoy, en local, el navegador nunca llama directamente al backend: el
proxy de desarrollo de Angular (`proxy.conf.json`) redirige `/api/*`
al backend, así que todo vive bajo el mismo origen y no hace falta
CORS. **El backend no tiene configuración CORS todavía** (no existía
la necesidad hasta ahora).

En producción hay dos caminos:

- **A. Un solo dominio** (ej. `crearcodecesar.co`): un reverse proxy
  (Caddy o nginx) enruta `/api/*` al backend y todo lo demás al
  frontend, bajo el mismo origen. **No hace falta CORS.** Es el camino
  más simple y es el que naturalmente encaja con la Opción de
  hosting "todo en un VPS" (§3).
- **B. Dos subdominios** (ej. `crearcodecesar.co` para el frontend,
  `api.crearcodecesar.co` para el backend): más simple de desplegar en
  plataformas PaaS (cada servicio con su propia URL, sin reverse proxy
  propio), pero **exige agregar configuración CORS** en el backend
  (`Access-Control-Allow-Origin` apuntando al dominio del frontend, vía
  variable de entorno — no hardcodeado, según ADR-06).

Esta decisión se toma junto con la de hosting (§3), porque el camino A
encaja mejor con la Opción 1 y el camino B con las Opciones 2 y 3.

## 3. Opciones de hosting comparadas

Precios consultados en jul 2026 (fuentes al final). Cifras en USD
convertidas a COP a ~$4.000 COP/USD — referencial, el valor real
depende de la tasa el día del pago (casi todas estas plataformas
cobran en USD con tarjeta internacional).

### Opción 1 — Un VPS único (Hetzner CX22) + Docker Compose

Backend + frontend + PostgreSQL, los tres en un mismo servidor vía
Docker Compose (mismo patrón que ya usa `docker-compose.yml` en local,
solo que en producción también corren el backend y el frontend, no
solo Postgres). Reverse proxy propio (Caddy, con HTTPS automático vía
Let's Encrypt) resolviendo el camino A de arquitectura (§2).

- **Costo**: Hetzner CX22 (2 vCPU, 4 GB RAM, 40 GB disco, 20 TB de
  tráfico incluido) — €3,79/mes ≈ **$4,6 USD/mes ≈ $18.500 COP/mes
  ≈ $222.000 COP/año**. Todo incluido (no hay costo aparte de base de
  datos: Postgres corre en el mismo servidor).
- **Ventajas**: el más barato con margen; control total; nada
  "duerme" por inactividad (a diferencia de los free tiers de
  Render/Railway); ya conocés Docker Compose de trabajar en local.
- **Desventajas**: sos vos quien administra el servidor — actualizar
  el SO, configurar backups de la base de datos (no vienen
  automáticos, hay que armar un cron con `pg_dump`), configurar el
  reverse proxy y renovación de certificados (Caddy lo automatiza,
  pero hay que dejarlo corriendo). Sin autoescalado (para el tráfico
  esperado de un sitio de captación de leads, no debería hacer falta).

### Opción 2 — Fly.io (backend + frontend) + Neon (Postgres gestionado, free tier)

Dos "apps" en Fly.io (una por servicio, cada una con su Dockerfile),
base de datos en Neon (Postgres serverless, capa gratis). Encaja con
el camino B de arquitectura (dos subdominios, CORS).

- **Costo**: Fly.io cobra por uso, sin plan fijo. Un frontend Node
  (256 MB RAM) ronda **$2 USD/mes**; un backend Java necesita más
  memoria para la JVM (mínimo realista ~512 MB-1 GB), rondando
  **$4-8 USD/mes**. Neon free tier (0,5 GB de almacenamiento, 100
  horas-CU/mes, *scale-to-zero*) alcanza sin costo para el tráfico
  esperado en el arranque. **Total estimado: $6-10 USD/mes ≈
  $24.000-40.000 COP/mes ≈ $290.000-480.000 COP/año.**
- **Ventajas**: nada que administrar a nivel de sistema operativo;
  TLS automático; Postgres gestionado (backups incluidos) sin costo
  mientras el uso sea bajo; más barato que las alternativas PaaS
  "clásicas" (Render, Railway).
- **Desventajas**: hay que aprender la CLI/`fly.toml` de Fly.io;
  la JVM del backend puede quedar ajustada de memoria en el tier más
  barato (puede necesitar tunear `-Xmx` o subir un escalón de precio);
  Neon free tier tiene *scale-to-zero* (la base "despierta" tras
  inactividad, con latencia extra en la primera consulta — no crítico
  para un panel admin de uso interno, sí a vigilar si molesta).

### Opción 3 — Render con Postgres de pago (referencia, PaaS "todo incluido")

Mencionada porque es la alternativa más simple de las tres, aunque más
cara — sirve como techo de comparación, no como recomendación
principal dado el criterio de "hosting económico" de HU-29.

- **Costo**: Web service Starter $7 USD/mes × 2 (backend + frontend) +
  Postgres desde $6-15 USD/mes ≈ **$20-29 USD/mes ≈
  $80.000-116.000 COP/mes ≈ $960.000-1.390.000 COP/año.**
- **Ventajas**: el más simple de configurar (deploy por git push, sin
  Dockerfile obligatorio), buena documentación, Postgres con backups
  automáticos y sin límite de 30 días.
- **Desventajas**: 4-6× más caro que las otras dos opciones para el
  mismo resultado.

### Opción 4 — Render (capa gratis) + Neon (Postgres, capa gratis) — **elegida**

Backend y frontend como *web services* gratis de Render; base de datos
en Neon en vez de en el Postgres gratis de Render, porque **el
Postgres gratis de Render se borra a los 30 días** (hallado al
presentar esta comparación al usuario) — un riesgo real de pérdida de
datos para una base que va a tener leads reales, no solo un
inconveniente de latencia. Neon en su capa gratis no se borra, solo
entra en *scale-to-zero* (conserva los datos, se "duerme" y despierta
con latencia extra en la primera consulta tras inactividad).

- **Costo**: **$0/mes de cómputo.** Único costo fijo: el dominio
  (§4, ~$60.000-80.000 COP/año).
- **Ventajas**: sin costo de infraestructura mientras el tráfico sea
  bajo; nada que administrar a nivel de sistema operativo; deploy
  simple (git push o imagen Docker); Postgres con datos persistentes.
- **Desventajas**: los *web services* gratis de Render "duermen" tras
  15 min sin tráfico (30-60s de arranque en frío en la siguiente
  visita) — aceptado como trade-off consciente dado el costo cero; a
  revisar si en la práctica resulta molesto para visitantes reales.
  Implica el **camino B de arquitectura** (§2): backend y frontend en
  URLs/dominios distintos → hace falta configurar CORS (ISS-080).

### Resumen

| Opción | Costo mensual | Costo anual | Esfuerzo de operación |
|---|---|---|---|
| 1. VPS único (Hetzner) | ~$18.500 COP | ~$222.000 COP | Alto (self-managed) |
| 2. Fly.io + Neon | ~$24.000-40.000 COP | ~$290.000-480.000 COP | Medio |
| 3. Render (Postgres de pago) | ~$80.000-116.000 COP | ~$960.000-1.390.000 COP | Bajo |
| **4. Render gratis + Neon — elegida** | **$0** | **$0** (+ dominio) | Bajo, con sleep aceptado |

## 4. Opciones de dominio comparadas

- **Registrador colombiano (ej. MI.COM.CO)**: `.co` desde **$59.990
  COP/año**, factura en pesos, sin conversión de moneda ni tarjeta
  internacional. Encaja con la estimación de la propia HU-29
  ($60.000-80.000 COP/año).
- **Registrador internacional at-cost (Cloudflare Registrar)**: sin
  markup sobre el precio de mayorista, pero cobra en USD (tarjeta
  internacional, posible comisión de conversión del banco) y el precio
  exacto de `.co` no quedó confirmado en la búsqueda — hay que
  consultarlo directo en el momento de comprar.

**Recomendación**: registrador colombiano, por simplicidad de pago (COP,
sin tarjeta internacional) y porque ya cumple el estimado de HU-29.
**Pendiente de decidir** — no bloquea ISS-080/081, se define al
momento de comprar el dominio (ISS-082).

## 5. Decisión

**Hosting: Opción 4 — Render (capa gratis) + Neon (Postgres, capa
gratis)**, decidido por el usuario tras revisar esta comparación. Se
prefirió sobre la Opción 1 (VPS único) porque evita la administración
de servidor a cambio de aceptar el *sleep* de los servicios gratis de
Render — trade-off consciente, costo cero de cómputo hasta que el
tráfico lo justifique. Implica el camino B de arquitectura (§2):
backend y frontend en orígenes distintos, hace falta CORS.

Registrador de dominio: pendiente, ver arriba.

## 6. Qué falta en el código antes de desplegar (ISS-080, ISS-081)

Camino elegido: Opción 4 (Render + Neon), camino B de arquitectura.

- [ ] `Dockerfile` del backend (imagen JVM + JAR).
- [ ] `Dockerfile` del frontend (imagen Node + `dist/frontend/server`).
- [ ] Configuración CORS en el backend: origen del frontend permitido
      vía variable de entorno (nunca hardcodeado, ADR-06).
- [ ] `BASE_URL` del frontend (`contenido/sitio.ts`, hoy una constante
      con el placeholder `.example`) pasa a leerse de una variable de
      entorno en build time.
- [ ] `NG_ALLOWED_HOSTS` (ver CLAUDE.md, descubierto en F6) con el
      dominio real de Render, no solo `localhost`.
- [ ] Pipeline de CI que construya ambas imágenes Docker (backend y
      frontend) al mergear a `main` — Render puede desplegar desde un
      registry o construir directo desde el `Dockerfile` del repo.
- [ ] Backups de PostgreSQL: Neon los incluye en su capa gratis, no
      hace falta configurar nada manual.

## Fuentes consultadas (jul 2026)

- [Render Pricing](https://render.com/pricing)
- [Railway Pricing](https://railway.com/pricing)
- [Hetzner Cloud — cost optimized](https://www.hetzner.com/cloud/cost-optimized)
- [Fly.io Pricing](https://fly.io/pricing/)
- [Cloudflare Registrar — dominios .co](https://www.cloudflare.com/application-services/products/registrar/buy-co-domains/)
- [Neon — free tier](https://neon.com/faqs/managed-postgres-databases-free-tier)
- [MI.COM.CO — precios de dominios](https://mi.com.co/precios)
