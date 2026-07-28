# 03 — Modelo de dominio

Dos contextos acotados en esta v1: **`leads`** — captura, validación y
gestión del ciclo de vida de las solicitudes de contacto que llegan
desde el sitio público — y **`usuarios`** — identidad de quien accede
al panel admin (agregado en la fase F5, ver ADR-08 en
[[02-arquitectura]]). El resto del sitio es contenido servido (ver
[[08-contenido]]).

Convención: todo el modelo se nombra en español, sin dependencias de
Spring/JPA/framework alguno (ver regla de dependencias en
[[02-arquitectura]]).

# Parte 1 — Contexto `leads`

## 1. Entidad: `SolicitudDeContacto`

Identidad propia (`SolicitudId`), ciclo de vida con estado mutable
controlado. Es el agregado raíz del contexto `leads`.

| Atributo | Tipo | Notas |
|---|---|---|
| `id` | `SolicitudId` (VO sobre UUID) | Se asigna al crear |
| `datosDeContacto` | `DatosDeContacto` (VO) | Ver sección 2 |
| `servicioDeInteres` | `ServicioDeInteres` (enum) | `DESARROLLO_A_LA_MEDIDA`, `IA_Y_AUTOMATIZACION`, `SOLUCIONES_TECNOLOGICAS`, `OTRO` |
| `mensaje` | `String` | No vacío, longitud máxima razonable (ej. 2000 caracteres) |
| `estado` | `EstadoSolicitud` (enum) | Ver sección 3 |
| `consentimiento` | `ConsentimientoDatos` (VO) | Ver sección 4 — obligatorio |
| `fechaCreacion` | `Instant` | Recibido como parámetro, no generado por el dominio (ver §6) |
| `fechaUltimaActualizacion` | `Instant` | Se actualiza en cada cambio de estado |

Comportamiento del agregado (no getters/setters anémicos):
- `SolicitudDeContacto.registrar(datos, servicio, mensaje, consentimiento, ahora)` —
  factoría estática que aplica todas las invariantes de creación y
  devuelve la solicitud en estado `NUEVA`.
- `solicitud.cambiarEstado(nuevoEstado, ahora)` — valida la transición
  contra la máquina de estados (§3) y lanza
  `TransicionDeEstadoInvalidaException` si no es válida.

## 2. Value Object: `DatosDeContacto`

Inmutable, igualdad por valor. Encapsula los datos que llegan del
formulario público.

| Atributo | Tipo | Validación |
|---|---|---|
| `nombre` | `String` | No vacío ni solo espacios; longitud máx. (ej. 120) |
| `empresa` | `String` (opcional) | Puede ser vacío/nulo; si viene, longitud máx. (ej. 120) |
| `correo` | `Correo` (VO) | Formato válido de email (regex RFC simplificada), no vacío, máx. 254 caracteres |
| `telefono` | `Telefono` (VO) | Formato colombiano: celular de 10 dígitos iniciado en 3, con o sin prefijo `+57`/`57`; se normaliza quitando espacios y guiones antes de validar |

`Correo` y `Telefono` son VOs propios (no `String` planos) para que la
validación de formato viva en el dominio y sea imposible construir una
`DatosDeContacto` con un correo o teléfono inválido — la excepción se
lanza en la construcción del VO, no en un validador externo.

## 3. Enum/VO: `EstadoSolicitud` — máquina de estados

```
      registrar()
          │
          ▼
      ┌───────┐
      │ NUEVA │
      └───┬───┘
     ┌─────┴─────┐
     ▼           ▼
┌────────────┐ ┌────────────┐
│ CONTACTADA │ │ DESCARTADA │  (terminal)
└─────┬──────┘ └────────────┘
   ┌──┴──┐
   ▼     ▼
┌──────────┐ ┌────────────┐
│CONVERTIDA│ │ DESCARTADA │  (terminal)
└──────────┘ └────────────┘
   (terminal)
```

Transiciones válidas:

| Desde | Hacia | Caso de uso típico |
|---|---|---|
| `NUEVA` | `CONTACTADA` | El fundador ya se comunicó con el lead |
| `NUEVA` | `DESCARTADA` | Se identifica como spam/no calificado sin contactar |
| `CONTACTADA` | `CONVERTIDA` | El lead se volvió cliente |
| `CONTACTADA` | `DESCARTADA` | El lead no siguió adelante |

`CONVERTIDA` y `DESCARTADA` son estados **terminales**: cualquier intento
de transición desde ellos (incluida una transición "a sí mismo") es
inválido y lanza `TransicionDeEstadoInvalidaException`. Cualquier par
(origen, destino) no listado en la tabla es inválido, incluyendo saltos
como `NUEVA → CONVERTIDA` directo.

## 4. Value Object: `ConsentimientoDatos`

Registra la aceptación del tratamiento de datos personales exigida por la
Ley 1581 de 2012 (Colombia).

| Atributo | Tipo | Notas |
|---|---|---|
| `aceptado` | `boolean` | Debe ser `true` para poder construir una `SolicitudDeContacto` |
| `fechaAceptacion` | `Instant` | Timestamp del momento de aceptación, recibido como parámetro |
| `versionPoliticaAceptada` | `String` | Identificador/versión del texto de política aceptado (trazabilidad si el texto legal cambia en el futuro) |

## 5. Puertos

### Puertos de entrada (driving ports — casos de uso)
- `RegistrarSolicitudUseCase` — recibe datos de contacto, servicio de
  interés, mensaje y consentimiento; crea y persiste una
  `SolicitudDeContacto`; dispara notificación.
- `CambiarEstadoSolicitudUseCase` — cambia el estado de una solicitud
  existente, validando la transición.
- `ListarSolicitudesUseCase` — lista solicitudes para el panel admin,
  con filtro opcional por `EstadoSolicitud`.

### Puertos de salida (driven ports)
- `SolicitudRepositorio` — `guardar(SolicitudDeContacto)`,
  `buscarPorId(SolicitudId)`, `listar()`, `listarPorEstado(EstadoSolicitud)`.
- `NotificadorPort` — `notificarNuevaSolicitud(SolicitudDeContacto)`;
  implementado en infraestructura como adaptador de correo (ver
  [[02-arquitectura]]).

## 6. Invariantes de negocio

1. Una `SolicitudDeContacto` no puede crearse sin `ConsentimientoDatos`
   con `aceptado = true`.
2. Una `SolicitudDeContacto` no puede crearse con `DatosDeContacto`
   inválidos: la validación de correo y teléfono ocurre en la
   construcción de sus VOs, es decir, falla en el dominio, no solo en el
   borde HTTP.
3. Toda solicitud nace en estado `NUEVA`; no existe forma de construir
   una solicitud directamente en otro estado.
4. Las transiciones de estado solo siguen el grafo de la sección 3;
   cualquier transición no listada se rechaza con
   `TransicionDeEstadoInvalidaException`.
5. `CONVERTIDA` y `DESCARTADA` son terminales.
6. `nombre` y `mensaje` no pueden estar vacíos ni ser solo espacios en
   blanco.
7. El dominio **no** llama al reloj del sistema directamente
   (`Instant.now()` no aparece en `dominio/`): los timestamps se reciben
   como parámetro desde la capa de aplicación. Esto mantiene el dominio
   puro y las pruebas unitarias deterministas (ver [[06-plan-de-pruebas]]).

## 7. Fuera del dominio (a propósito)

Dos mecanismos mencionados en el brief funcional **no son reglas de
dominio** y no viven en `dominio/`, para no contaminar el modelo con
preocupaciones técnicas:

- **Honeypot anti-spam**: es una validación de borde (se verifica en la
  capa de aplicación o incluso antes de invocar el caso de uso); si el
  campo trampa viene lleno, la solicitud ni siquiera llega a
  `RegistrarSolicitudUseCase`.
- **Rate limiting**: es un mecanismo transversal de infraestructura
  (interceptor/filtro HTTP), no una regla del agregado
  `SolicitudDeContacto`.

Ambos se detallan como issues técnicos en [[05-backlog-issues]] dentro de
la épica E2, pero se implementan en `infraestructura/` y, como mucho, se
orquestan desde `aplicacion/` — nunca desde `dominio/`.

# Parte 2 — Contexto `usuarios`

Identidad de quienes acceden a áreas autenticadas. Nació en F5 con un
único usuario (el fundador, panel admin); desde la fase **F8** (Etapa
3, ver [[10-vision-v2]]) también cubre a los **clientes registrados**:
registro público, verificación de correo y recuperación de contraseña.

## 1. Entidad: `Usuario`

Agregado raíz del contexto `usuarios`. Identidad propia (`UsuarioId`).

| Atributo | Tipo | Notas |
|---|---|---|
| `id` | `UsuarioId` (VO sobre UUID) | Se asigna al crear |
| `correo` | `Correo` (VO, mismo tipo que en el contexto `leads`) | Identificador de login; único |
| `contrasenaHash` | `String` | Nunca la contraseña en claro; el hash se calcula en `infraestructura/` (ver `CifradorDeContrasenas` abajo) |
| `rol` | `Rol` (enum) | `ADMIN` o `CLIENTE` |
| `verificado` | `boolean` | Desde F8. Un cliente nace sin verificar y no puede iniciar sesión hasta abrir el enlace del correo. El admin sembrado nace verificado |

Factorías: `crearAdmin(correo, hash)` (ADMIN, verificado) y
`registrarCliente(correo, hash)` (CLIENTE, no verificado). El record es
inmutable: `verificar()` y `conContrasena(nuevoHash)` devuelven copias.

## 2. Enum: `Rol`

`ADMIN` y `CLIENTE` (desde F8). **Decisión registrada al arrancar F8**
(la pedía [[10-vision-v2]]): se mantiene **un solo rol por usuario** —
no `Set<Rol>` — porque ningún caso real de hoy necesita más de uno; la
migración a multi-rol se evalúa en F11 cuando existan los roles
internos (FACTURACION, DISENO, DESARROLLO).

## 3. Value Object: `ContrasenaPlana` (desde F8)

Envuelve una contraseña en claro **solo en tránsito** (nunca se
persiste). Invariante: mínimo 10 caracteres — lanza
`ContrasenaInvalidaException`. Su `toString()` está enmascarado para
que jamás caiga a un log. Aplica únicamente a los flujos nuevos
(registro y restablecimiento); el login y el *seed* del admin no la
usan, para no romper credenciales existentes.

## 4. Entidad: `TokenDeUsuario` (desde F8)

Token de un solo uso enviado por correo, para dos propósitos
(`VERIFICACION`, vigencia 24 h; `RECUPERACION`, vigencia 1 h).

| Atributo | Tipo | Notas |
|---|---|---|
| `id` | UUID | |
| `usuarioId` | `UsuarioId` | Dueño del token |
| `valorHash` | `String` | SHA-256 del valor en claro — el valor real solo viaja en el enlace del correo, nunca se persiste |
| `proposito` | enum | `VERIFICACION` \| `RECUPERACION` |
| `creadoEn` / `expiraEn` | `Instant` | Vigencia según propósito |
| `usadoEn` | `Instant?` | Un solo uso |

`TokenDeUsuario.generar(usuarioId, proposito, ahora, duracion)` produce
la entidad **y** el valor en claro (con `SecureRandom` + `MessageDigest`
de `java.base` — el dominio sigue sin Spring/JPA, ArchUnit intacto).

## 5. Puertos

### Puertos de entrada (driving ports)
- `AutenticarUsuarioUseCase` — correo + contraseña en claro → sesión
  autenticada (token JWT + rol + correo); `CredencialesInvalidasException`
  genérica si fallan; `CuentaNoVerificadaException` si las credenciales
  son correctas pero la cuenta no está verificada (desde F8).
- `CrearUsuarioUseCase` — alta directa (solo la usa el *seed* del admin).
- `RegistrarClienteUseCase` (F8) — registro público: crea el cliente sin
  verificar, genera token de verificación y envía el correo
  (*best-effort*); 409 si el correo ya existe.
- `VerificarCorreoUseCase` (F8) — consume un token de verificación
  vigente y marca la cuenta verificada.
- `ReenviarVerificacionUseCase` (F8) — silencioso si el correo no
  existe o ya está verificado; invalida tokens previos.
- `SolicitarRecuperacionUseCase` (F8) — respuesta siempre genérica;
  genera token de recuperación y envía el correo.
- `RestablecerContrasenaUseCase` (F8) — consume token de recuperación,
  cambia el hash y marca la cuenta verificada (probó ser dueño del
  correo).

### Puertos de salida (driven ports)
- `UsuarioRepositorio` — `guardar(Usuario)`, `buscarPorCorreo(Correo)`
  (case-insensitive en el adaptador), `buscarPorId(UsuarioId)` (F8).
- `TokenDeUsuarioRepositorio` (F8) — `guardar`, `buscarPorValorHash`,
  `invalidarActivos(usuarioId, proposito)`,
  `contarRecientes(usuarioId, proposito, desde)` (para el límite por
  correo, ver invariante 6).
- `CifradorDeContrasenas` — `hash(contrasenaEnClaro)`,
  `verificar(contrasenaEnClaro, hash)` (BCrypt vive en infraestructura).
- `GeneradorDeToken` — `generar(Usuario)` → sesión JWT.
- `EnviadorDeCorreosDeCuenta` (F8) —
  `enviarVerificacion(correo, tokenEnClaro)`,
  `enviarRecuperacion(correo, tokenEnClaro)`. El **adaptador** arma el
  enlace completo (URL del frontend + ruta) — la aplicación no conoce
  rutas del frontend.

## 6. Invariantes de negocio

1. `AutenticarUsuarioUseCase` nunca revela si la causa del fallo fue
   "el correo no existe" o "la contraseña no coincide" — mismo mensaje
   genérico en ambos casos.
2. `correo` es único por usuario. El registro público con un correo
   existente responde 409 explícito — trade-off consciente de
   usabilidad sobre anti-enumeración (documentado en HU-30).
3. La contraseña en claro nunca se persiste ni se registra en logs —
   solo su hash. `ContrasenaPlana` refuerza esto con `toString()`
   enmascarado.
4. Un cliente no verificado no puede iniciar sesión (solo se le informa
   después de validar la contraseña — no filtra existencia de cuentas).
5. Los tokens de correo son de un solo uso, con vigencia por propósito
   (24 h verificación, 1 h recuperación) y solo se persiste su hash.
   El error hacia el usuario es único: "enlace inválido o vencido",
   sin distinguir la causa.
6. Máximo 3 envíos de correo por usuario y propósito cada 15 minutos —
   control **por correo** en la capa de aplicación (el rate limiting
   por IP es solo respaldo: en producción todas las peticiones llegan
   con la IP del proxy del frontend, ver [[10-vision-v2]] y ADR-09).
7. La recuperación de contraseña responde siempre lo mismo, exista o
   no la cuenta.

## 7. Fuera de este contexto, a propósito

- **Revocación de sesión antes de su expiración natural** (denylist):
  sigue sin construirse (ADR-08). Consecuencia nueva de F8, aceptada y
  documentada: restablecer la contraseña **no** invalida los JWT ya
  emitidos (hasta 8 h de vida restante).
- **Cambio de contraseña autenticado**: no existe en F8 — lo cubre el
  flujo de recuperación (HU-32).
- **Login social (Google/…), 2FA, roles internos** (FACTURACION,
  DISENO, DESARROLLO): explícitamente fuera; los roles internos son de
  F11.
- **Limpieza de tokens vencidos**: la tabla crece sin poda en F8 —
  follow-up documentado en [[05-backlog-issues]].


# Parte 3 — Contexto `asistente` (fase F9)

Contexto nuevo del asistente IA (ADR-10 en [[02-arquitectura]]). Sin
persistencia en v1: la conversación vive en la petición (y en la
memoria del navegador durante la sesión de página); no hay entidades
con identidad, solo objetos de valor y un puerto de salida.

## Objetos de valor

- **`MensajeDeChat`**: `rol` (`USUARIO` | `ASISTENTE`) + `texto`.
  Invariantes: texto no vacío, longitud máxima (configurable, del
  orden de 1.000 caracteres) — protege el costo por tokens y evita
  abusos.
- **`ConversacionDeAsistente`**: lista ordenada de `MensajeDeChat`.
  Invariantes: historial acotado (máximo N mensajes por petición, del
  orden de 20); el último mensaje debe ser del USUARIO.

## Puertos (interfaces del dominio)

- **`GeneradorDeRespuestas`**: `RespuestaDelAsistente responder(ConversacionDeAsistente conversacion)`
  — la infraestructura lo implementa con Groq (adaptador HTTP,
  compatible con OpenAI). `RespuestaDelAsistente` lleva el texto y si
  el asistente sugirió escalar a un humano.

## Invariantes del contexto

1. El prompt de sistema se ancla al contenido real del sitio
   (`asistente-contexto.md`) — el asistente nunca inventa precios ni
   promesas; ante preguntas fuera de contexto escala al humano.
2. Los límites de uso se aplican ANTES de llamar al proveedor: global
   diario (techo de la capa gratis), por usuario registrado, por
   sesión anónima; el de IP es respaldo grueso en el filtro.
3. Un fallo del proveedor (timeout, 429, 5xx) nunca llega al visitante
   como error técnico: se traduce a la respuesta de indisponibilidad
   con la alternativa humana.
4. La `GROQ_API_KEY` jamás aparece en logs, respuestas ni en el
   navegador.
