# 09 — Despliegue

Cubre ISS-079 a ISS-082 de la fase F7 ([[05-backlog-issues]]) y HU-29
([[04-historias-de-usuario]]). Este documento es investigación +
recomendación — la decisión final (qué hosting, qué registrador, y
si publicar ya) es del usuario (ISS-082).

## 1. Qué hay que desplegar

Tres piezas, ninguna con Dockerfile todavía (ISS-081 las crea):

- **Backend**: JAR de Spring Boot (JVM) + PostgreSQL con Flyway.
- **Frontend**: sitio **100% estático** desde ADR-12 (11 ago 2026).
  `npm run build` prerenderiza las páginas públicas y genera
  `sitemap.xml` y `robots.txt`; las rutas de sesión se sirven como SPA
  desde `index.csr.html`. Antes era un servidor Node/Express con SSR,
  que se dormía a los 15 minutos sin aportar nada al SEO.
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

## 7 bis. Corte a Static Site sin downtime (ADR-12)

**Render no convierte un web service en Static Site**: son tipos de
servicio distintos. Si se cambia `runtime: docker` por
`runtime: static` manteniendo el nombre, el servicio existente sigue
siendo el de antes e intenta construir el `Dockerfile` que ya no está
— eso pasó el 11 ago 2026 y el deploy falló (sin tumbar el sitio: Render
conserva sirviendo la versión anterior cuando un build falla).

Por eso el Blueprint declara el sitio con **nombre nuevo**
(`crearcodecesar-sitio`), que se crea al lado del viejo. Orden del
corte:

1. **Sincronizar el Blueprint.** Render crea `crearcodecesar-sitio`
   como Static Site y lo publica en su propia URL `*.onrender.com`. El
   servicio viejo sigue sirviendo el dominio, intacto.
2. **Verificar en esa URL**: que carguen las páginas públicas, que
   `/admin` y `/mi-cuenta` entren por el fallback de SPA, que
   `/sitemap.xml` y `/robots.txt` respondan, y que el formulario de
   contacto funcione (eso prueba el `rewrite` de `/api`).

   No basta con que respondan 200: hay que mirar el **HTML de la
   primera respuesta**, porque un error de reglas sirve el cascarón del
   SPA y el navegador igual pinta la página correcta después (pasó el
   12 ago 2026, ver ADR-12). Comprobación rápida — cada ruta debe traer
   su propio `<title>`, no el genérico:

   ```bash
   for r in / /contacto /servicios/desarrollo-a-la-medida /herramientas /blog; do
     echo "$r -> $(curl -s https://crearcodecesar-sitio.onrender.com$r \
       | grep -o '<title>[^<]*</title>')"
   done
   ```

   Lo mismo lo verifica en CI `frontend/scripts/verificar-rutas-estaticas.mjs`
   (`npm run verificar:rutas`) sobre el `render.yaml` real.
3. **Mover el dominio**: quitar `crearcodecesar.com` y `www` del
   servicio viejo y añadirlos al nuevo. Como el DNS ya apunta a
   Cloudflare y de ahí a Render, la propagación es cuestión de minutos.
4. **Comprobar el dominio** ya en el sitio nuevo, incluida la cabecera
   HSTS (`curl -sI https://crearcodecesar.com | grep -i strict`). Esta
   comprobación **solo vale en el dominio propio**: sobre
   `*.onrender.com` el borde de Render impone su propio HSTS
   (`max-age=315360000; includeSubdomains; preload`, porque tiene su
   dominio en la lista de precarga) y tapa el valor declarado en el
   Blueprint.
5. **Borrar el web service viejo** (`crearcodecesar-frontend`) y quitar
   su declaración del Blueprint. Hasta ese momento, volver atrás es
   devolverle el dominio.

Mientras el servicio viejo exista, cada push seguirá disparándole un
build que falla por el `Dockerfile` ausente. Es ruido, no un problema:
no afecta a lo que está publicado.

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

## 7. Correo transaccional en producción

Los correos de verificación y recuperación (F8), la notificación de
solicitudes nuevas (F2) y el envío de cotizaciones con el PDF adjunto
(F11) salen por SMTP. En local los recibe Mailpit
(`docker compose up -d`, bandeja en http://localhost:8025) sin
configurar nada.

**Remitente definitivo: `admin@crearcodecesar.com`** (decisión del
usuario, 11 ago 2026), ya con el dominio propio. Sustituye al
`crearcodecesar@gmail.com` temporal que se había elegido el 28 jul
2026 mientras no existía dominio.

**Envío por Resend** (decisión del usuario, 11 ago 2026), ya declarado
en `render.yaml`. El buzón donde se *reciben* los correos es aparte
(Zoho u otro): Resend solo envía.

| Variable | Valor | Por qué |
|---|---|---|
| `MAIL_HOST` | `smtp.resend.com` | fija en `render.yaml` |
| `MAIL_PORT` | **`2587`** (STARTTLS) | fija — ver el aviso de abajo |
| `MAIL_USERNAME` | **`resend`** (la palabra literal) | fija — no es una dirección; le dice a su gateway que autentique con API key |
| `MAIL_PASSWORD` | la API key (`re_…`) | `sync: false`, se ingresa en el dashboard |
| `MAIL_FROM` | `Crear Code Cesar <contacto@crearcodecesar.com>` | fija |
| `MAIL_REPLY_TO` | `contacto@crearcodecesar.com` | fija |

**El remitente va explícito en el código** (`RemitenteDeCorreo`, leído
por los tres adaptadores de correo). Antes no se fijaba y Spring usaba
el usuario SMTP como remitente: con Resend eso habría puesto `resend`
como dirección de origen y **el envío se habría rechazado con 422 "from
address not allowed"**. Resend solo acepta remitentes de dominios
verificados en su panel, así que `crearcodecesar.com` debe estar
verificado ahí (con sus registros en Cloudflare) antes del primer envío.

Cuidado con la ortografía: **`contacto@`** con o final. Y ese buzón
debe existir de verdad para recibir: es la dirección a la que responden
los clientes cuando contestan una cotización.

### Render bloquea los puertos SMTP clásicos (incidente del 11 ago 2026)

Con el correo ya bien configurado, el registro en producción se quedaba
colgado en "Creando cuenta…" y el correo nunca llegaba. La causa no era
el código ni las credenciales: **Render bloquea el tráfico saliente a
los puertos SMTP 25, 465 y 587 en las instancias del plan gratuito**
([changelog](https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports)).
Y no los rechaza, los **descarta**: por eso la conexión no fallaba
rápido, se quedaba esperando y arrastraba la petición del visitante.

Dos correcciones, ambas aplicadas:

1. **`MAIL_PORT=2587`**. Resend expone ese puerto alternativo con
   STARTTLS igual que el 587, y no está en la lista de bloqueados.
2. **Timeouts de SMTP** (`connectiontimeout`, `timeout`, `writetimeout`,
   10 s, variable `MAIL_TIMEOUT_MS`). Sin ellos, cualquier bloqueo
   futuro vuelve a colgar la petición: el envío es best-effort, así que
   más vale fallar rápido y registrarlo.

**Verificado en producción el 11 ago 2026**, tras cambiar el puerto: la
petición pasó de colgarse más de 60 s a responder en 5,5 s, y el correo
llegó a **bandeja principal de Gmail** (no a spam) con el remitente
`Crear Code Cesar <contacto@crearcodecesar.com>` y el enlace apuntando
al dominio propio. Es decir: DKIM y SPF están bien y el `From` explícito
es aceptado por Resend.

Señal útil para diagnosticar a futuro, por el tiempo de respuesta del
endpoint que envía: **~5 s** es una conversación SMTP completa (salió);
**menos de 1 s** significa que no se envió nada (el caso de uso fue
silencioso, por ejemplo cuenta ya verificada o límite por correo
alcanzado); **10 s** es el timeout, o sea que no se pudo conectar.

**Plan B si Render extendiera el bloqueo a los puertos alternativos**:
el envío está detrás de `TransporteDeCorreo`, con dos implementaciones
elegidas por `MAIL_TRANSPORTE`:

- `smtp` (default) — Mailpit en local, GreenMail en los tests.
- `resend` — la **API HTTP** de Resend (puerto 443, nunca bloqueado).
  Solo hay que poner `MAIL_TRANSPORTE=resend` y `RESEND_API_KEY` con la
  misma clave; no cambia nada más.

### Estado del correo (verificado el 11 ago 2026)

**Envío y recepción están separados a propósito y no se pisan**: Resend
envía desde el subdominio `send`, Zoho recibe en la raíz.

| Qué | Registro | Valor comprobado |
|---|---|---|
| DKIM de Resend | TXT `resend._domainkey` | publicado |
| SPF de envío | TXT `send` | `v=spf1 include:amazonses.com ~all` |
| Rebotes | MX `send` (prio 10) | `feedback-smtp.us-east-1.amazonses.com` |
| Recepción | MX raíz | `mx.zoho.com` (10), `mx2` (20), `mx3` (50) |
| SPF de recepción | TXT raíz | `v=spf1 include:zohomail.com ~all` |
| DMARC | TXT `_dmarc` | `p=none; rua=mailto:admin@crearcodecesar.com` |

Dominio **verificado en Resend** (región us-east-1) y la API key ya
está como `MAIL_PASSWORD` en Render, restringida a este dominio con
permiso de solo envío. `contacto@` existe como alias del buzón `admin@`
en Zoho (y `contact@` sin la o también, por si alguien la escribe mal):
las respuestas llegan a la misma bandeja.

**No agregar `amazonses.com` al SPF de la raíz**: no hace falta y
consumiría lookups. DMARC alinea igual porque el DKIM de Resend firma
con `d=crearcodecesar.com`, que coincide con el dominio del `From`; y
el SPF del envelope (`send.crearcodecesar.com`) alinea en modo relajado
con la raíz. Cuando lleve semanas enviando sin incidencias, el paso
natural es endurecer DMARC de `p=none` a `p=quarantine`.

**Advertencia importante antes de configurarlo**: el plan **Forever
Free** de Zoho está pensado para usar el correo desde su webmail y su
app, y **restringe el acceso por cliente externo** (bloquea IMAP; el
acceso SMTP/POP depende del plan y la región). Si al configurar el
envío aparece un error de autenticación o de "acceso no permitido", no
es un problema del código: hace falta **Mail Lite** (~USD 1 al
mes/usuario), que habilita SMTP e IMAP.
Referencias: [configuración SMTP/IMAP de Zoho](https://www.zoho.com/mail/help/imap-access.html),
[límites del plan gratuito](https://mail.mailbux.com/blog/email-comparisons/zoho-mail-free-plan-limitations-alternative).

Alternativa si no se quiere pagar: un servicio **transaccional** con
capa gratis (Brevo da ~300 correos/día, Resend ~3.000/mes), que además
maneja mejor la entregabilidad de correos automáticos. Como todo va por
variables de entorno y el envío está detrás de un puerto, cambiar de
proveedor es configuración, no código.

### Checklist operativo pendiente en Render (al 11 ago 2026)

Todo esto se hace en el dashboard, una sola vez. La vía más directa es
**por servicio**, sin pasar por el Blueprint: `dashboard.render.com` →
servicio `crearcodecesar-backend` → pestaña **Environment** → *Add
environment variable* → **Save changes** (Render redespliega solo).

| Variable | Servicio | Valor | Para qué |
|---|---|---|---|
| `CLOUDFLARE_ACCOUNT_ID` | backend | el de la cuenta (32 caracteres) | Demo de diseño con Workers AI |
| `CLOUDFLARE_API_TOKEN` | backend | token de la plantilla Workers AI | ídem |
| `MAIL_PASSWORD` | backend | la **API key de Resend** (`re_…`) | correos de cuenta y cotizaciones |

Las variables que ya vienen fijas en `render.yaml`
(`DEMO_PROVEEDOR_IMAGENES=cloudflare`, `MAIL_HOST=smtp.resend.com`,
`MAIL_USERNAME=resend`, `MAIL_FROM`, `MAIL_REPLY_TO`,
`HSTS_MAX_AGE`, `RATE_LIMIT_ASISTENTE_MAX_INTENTOS`,
`COTIZACIONES_IMPUESTO`) se aplican **al aceptar el sync del
Blueprint**: `dashboard.render.com` → **Blueprints** → el del repo →
botón de sincronizar/aplicar cambios. Render muestra el diff antes de
aplicar y pide los valores de las variables marcadas `sync: false` que
aún no existan.

**Rotación de secretos** (todos circularon por el chat de desarrollo;
rotar es generar el valor nuevo y pegarlo en los dos sitios donde vive:
el `.env` local y Render):

| Secreto | Dónde se rota |
|---|---|
| Token de Cloudflare | `dash.cloudflare.com/profile/api-tokens` → *Roll* |
| Contraseña del buzón / app password de Zoho | Perfil de Zoho → Seguridad |
| App Password del Gmail temporal | `myaccount.google.com/apppasswords` → revocar (ya no se usa) |
| Contraseña de Neon | consola de Neon → *Reset password* → actualizar `DB_URL` en Render |
| `GROQ_API_KEY` | `console.groq.com` → revocar y crear otra |

**Pasos en Zoho** (una vez, con el dominio ya verificado):

1. Crear el buzón `admin@crearcodecesar.com` en Zoho Mail.
2. Verificar el dominio y publicar los registros **SPF y DKIM** que
   Zoho indique — sin ellos, Gmail y Outlook mandan los correos a spam.
   Los registros se agregan en Cloudflare, que es quien lleva el DNS.
3. Activar la verificación en dos pasos y generar una **contraseña de
   aplicación** (Perfil → Seguridad → Contraseñas de aplicación). Esa
   es `MAIL_PASSWORD`; nunca la contraseña real.
4. Probar el envío real desde producción con el flujo de recuperación
   de contraseña, que es el más corto de verificar.

### Guía histórica: App Password de Google (del Gmail temporal)

Se conserva por si se vuelve a un buzón de Google; con Zoho no aplica.

1. Entrar a https://myaccount.google.com con la cuenta de Gmail.
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
| `MAIL_HOST` | `smtp.zoho.com` | fija en `render.yaml` |
| `MAIL_PORT` | `587` | fija en `render.yaml` |
| `MAIL_SMTP_AUTH` / `MAIL_SMTP_STARTTLS` | `true` / `true` | fijas en `render.yaml` |
| `MAIL_USERNAME` | `admin@crearcodecesar.com` | `sync: false` — se escribe en el dashboard |
| `MAIL_PASSWORD` | la contraseña de aplicación del buzón | `sync: false` — se escribe en el dashboard |
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
