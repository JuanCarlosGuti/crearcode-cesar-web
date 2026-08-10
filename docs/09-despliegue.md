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

## 2. Arquitectura: un origen público, sin CORS (ADR-09)

Backend y frontend van a vivir en dos servicios/URLs distintas de
Render (Opción 4, §3) — en un primer análisis esto parecía obligar a
CORS ("dos subdominios"). Al implementar ISS-080 apareció una opción
mejor: el propio servidor SSR del frontend (`frontend/src/server.ts`)
reenvía todo `/api/**` al backend real (`http-proxy-middleware`, target
configurado vía la variable de entorno `BACKEND_URL`). El navegador
solo ve **un origen** (el del frontend) — nunca llama al backend
directo, así que **no hace falta CORS en absoluto**, y
`SolicitudesApi`/`AuthApi` no cambiaron una línea (siguen usando rutas
relativas `/api/...`, igual que ya hacían contra el proxy de
`ng serve` en desarrollo). Detalle completo y motivo en **ADR-09**
([[02-arquitectura]]).

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
base de datos en Neon (Postgres serverless, capa gratis).

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
tráfico lo justifique. Arquitectura: proxy servidor-a-servidor sin
CORS (§2, ADR-09).

Registrador de dominio: pendiente, ver arriba.

## 6. Qué falta en el código antes de desplegar (ISS-080, ISS-081)

Camino elegido: Opción 4 (Render + Neon), proxy sin CORS (ADR-09).

- [x] `Dockerfile` del backend (imagen JVM + JAR) — `backend/Dockerfile`.
- [x] `Dockerfile` del frontend (imagen Node + `dist/frontend/server`)
      — `frontend/Dockerfile`.
- [x] Proxy `/api/**` en `frontend/src/server.ts` hacia `BACKEND_URL`
      (ver ADR-09) — reemplaza la necesidad de CORS.
- [x] `server.port=${PORT:8080}` en `application.properties` — Render
      inyecta el puerto real en runtime.
- [x] Pipeline de CI (`docker-build` en `.github/workflows/ci.yml`)
      que verifica que ambas imágenes construyan al mergear a `main`
      — sin publicar a ningún registry: Render construye la imagen él
      mismo desde el `Dockerfile` del repo.
- [x] Verificado extremo a extremo: ambas imágenes corridas juntas en
      una red Docker (backend real + Postgres real) confirman que el
      proxy reenvía correctamente (`POST /api/solicitudes`,
      `POST /api/auth/login`); la suite e2e completa
      (`contacto-e2e.spec.ts`, `accesibilidad-e2e.spec.ts`) pasa
      completa contra el build de producción con el proxy activo, sin
      modificar ningún test existente.
- [x] `render.yaml` (Blueprint de Render, raíz del repo): declara los
      dos web services en la capa gratis, región Virginia, con
      `healthCheckPath`, `JWT_SECRET` autogenerado por Render,
      credenciales de Neon como `sync: false` (se ingresan en el
      dashboard, nunca en el repo), `BACKEND_URL` tomado
      automáticamente del servicio del backend y
      `NG_ALLOWED_HOSTS` (desde el 10 ago 2026 incluye el dominio
      propio: `crearcodecesar.com,www.crearcodecesar.com,*.onrender.com`).
- [x] `DB_URL` en el backend: variable nueva que acepta la URL JDBC
      completa — necesaria porque Neon exige `sslmode=require`, que no
      se puede expresar con solo host/puerto/nombre. Sin `DB_URL`, el
      comportamiento local es idéntico al de siempre (verificado con
      `mvnw verify` y arrancando la app real por ambos caminos).
- [x] `contenido/sitio.ts` (`BASE_URL`): actualizado el 10 ago 2026 al
      dominio canónico `https://crearcodecesar.com` (ADR-11) — deja de
      ser el placeholder `.example` que salía en sitemap/OG.

## 8. Dominio propio crearcodecesar.com (10 ago 2026, ADR-11)

Dominio comprado; Cloudflare actúa como DNS/proxy delante de Render.
Checklist de la transición:

1. **En Cloudflare**: registro apex `crearcodecesar.com` → CNAME
   (aplanado) o A hacia `crearcodecesar-frontend.onrender.com`;
   `www` → CNAME al mismo destino con regla de redirección 301 a la
   raíz. SSL "Full (strict)". HSTS la emite Cloudflare (ADR-11).
2. **En Render** (dashboard del frontend → Custom Domains): agregar
   `crearcodecesar.com` y `www.crearcodecesar.com` para que Render
   emita/acepte el certificado del dominio.
3. **En el repo** (hecho): `NG_ALLOWED_HOSTS` con los dominios nuevos,
   `FRONTEND_URL=https://crearcodecesar.com` (enlaces de correos),
   `BASE_URL` canónico, `<link rel="canonical">` en todas las páginas
   y `/monday-app-association.json` estático en `frontend/public/`.
4. **Después de propagar DNS**: probar https://crearcodecesar.com
   (páginas, /herramientas, sitemap.xml, robots.txt,
   /monday-app-association.json) y reenviar el dominio en Google
   Search Console cuando se quiera indexar.
5. El correo corporativo con dominio propio (ej. hola@crearcodecesar.com)
   sigue PENDIENTE — decisión aparte (Cloudflare Email Routing gratis
   es candidato).
- [ ] `NG_ALLOWED_HOSTS`: variable de entorno a configurar en Render
      con el dominio real el día del despliegue (sin cambio de código,
      Angular ya la lee nativamente, ver CLAUDE.md).
- [ ] Backups de PostgreSQL: Neon los incluye en su capa gratis, no
      hace falta configurar nada manual.

## 7. Correo de cuentas de cliente en producción (fase F8 — Gmail con App Password)

Los correos de verificación y recuperación (fase F8) se envían por SMTP.
En local los recibe Mailpit (`docker compose up -d`, bandeja en
http://localhost:8025) sin configurar nada. En producción se usa
**Gmail con App Password** sobre `crearcodecesar@gmail.com` (decisión
del usuario, 28 jul 2026; ~500 correos/día de tope, de sobra para
empezar). **Brevo queda como la migración futura** cuando haya dominio
propio y más volumen — gracias al puerto `EnviadorDeCorreosDeCuenta` y
a que toda la configuración es por variables de entorno, ese cambio
será solo de configuración SMTP, sin tocar código.

### Guía: crear la App Password (una sola vez, ~3 minutos)

1. Entrar a https://myaccount.google.com con `crearcodecesar@gmail.com`.
2. **Seguridad** → activar la **verificación en dos pasos** si no está
   activa (requisito de Google para las App Passwords).
3. Ir a https://myaccount.google.com/apppasswords (o buscar "Contraseñas
   de aplicaciones" en el buscador de la cuenta).
4. Nombre de la app: `render-crearcodecesar` → **Crear**.
5. Google muestra una contraseña de 16 letras **una única vez** —
   cópiala (sin los espacios). Esa es `MAIL_PASSWORD`; **nunca** es la
   contraseña real de la cuenta, y se puede revocar sola desde esa misma
   página si se filtra.

### Variables en Render (backend, ya declaradas en `render.yaml`)

| Variable | Valor | Cómo llega |
|---|---|---|
| `MAIL_HOST` | `smtp.gmail.com` | fija en `render.yaml` |
| `MAIL_PORT` | `587` | fija en `render.yaml` |
| `MAIL_SMTP_AUTH` / `MAIL_SMTP_STARTTLS` | `true` / `true` | fijas en `render.yaml` |
| `MAIL_USERNAME` | `crearcodecesar@gmail.com` | `sync: false` — se escribe en el dashboard |
| `MAIL_PASSWORD` | la App Password de 16 letras | `sync: false` — se escribe en el dashboard |
| `FRONTEND_URL` | URL pública del frontend | automática (`fromService`) — base de los enlaces de los correos |

Nota operativa: los rate limits de los endpoints de cuenta son
configurables por variables `RATE_LIMIT_*` (ver
`application.properties`) — útiles para relajarlos al correr la suite
e2e varias veces seguidas en local, nunca en producción.

## Fuentes consultadas (jul 2026)

- [Render Pricing](https://render.com/pricing)
- [Railway Pricing](https://railway.com/pricing)
- [Hetzner Cloud — cost optimized](https://www.hetzner.com/cloud/cost-optimized)
- [Fly.io Pricing](https://fly.io/pricing/)
- [Cloudflare Registrar — dominios .co](https://www.cloudflare.com/application-services/products/registrar/buy-co-domains/)
- [Neon — free tier](https://neon.com/faqs/managed-postgres-databases-free-tier)
- [MI.COM.CO — precios de dominios](https://mi.com.co/precios)
