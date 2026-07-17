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

Identidad de quienes acceden al panel admin. En v1 hay un único usuario
(el fundador), pero el modelo ya contempla un campo de rol pensado para
cuando existan más personas con responsabilidades distintas (ver
ADR-08 en [[02-arquitectura]]) — sin construir todavía gestión de
usuarios, registro ni permisos granulares por rol: eso es v2.

## 1. Entidad: `Usuario`

Agregado raíz del contexto `usuarios`. Identidad propia (`UsuarioId`).

| Atributo | Tipo | Notas |
|---|---|---|
| `id` | `UsuarioId` (VO sobre UUID) | Se asigna al crear |
| `correo` | `Correo` (VO, mismo tipo que en el contexto `leads`) | Identificador de login; único |
| `contrasenaHash` | `String` | Nunca la contraseña en claro; el hash se calcula en `infraestructura/` (ver `CifradorDeContrasenas` abajo) |
| `rol` | `Rol` (enum) | Hoy solo `ADMIN` |

## 2. Enum: `Rol`

Un único valor en v1: `ADMIN`. Existe como enum (no como `boolean
esAdmin`) para que agregar un segundo rol el día que haga falta sea
extender el enum y los puntos de autorización que lo consultan, no
rediseñar el modelo.

## 3. Puertos

### Puertos de entrada (driving ports)
- `AutenticarUsuarioUseCase` — recibe correo y contraseña en claro;
  devuelve una sesión autenticada (token) si son válidos; lanza
  `CredencialesInvalidasException` si no (ver invariante 1 abajo).
- `CrearUsuarioUseCase` — crea un `Usuario` nuevo. En v1 solo lo usa el
  *seed* del único admin al arrancar la aplicación (ver
  [[02-arquitectura]] ADR-08); no hay pantalla de alta de usuarios
  todavía.

### Puertos de salida (driven ports)
- `UsuarioRepositorio` — `guardar(Usuario)`,
  `buscarPorCorreo(Correo)` (búsqueda case-insensitive: el adaptador,
  no el dominio, normaliza mayúsculas/minúsculas).
- `CifradorDeContrasenas` — `hash(contrasenaEnClaro)`,
  `verificar(contrasenaEnClaro, hash)`. Existe como puerto porque el
  algoritmo real (BCrypt) es una dependencia de Spring Security que el
  dominio no puede tocar (ver regla de dependencias en
  [[02-arquitectura]]).
- `GeneradorDeToken` — `generar(Usuario)` → token de sesión (JWT en la
  implementación real). El dominio conoce que existe un token, no cómo
  se firma ni valida.

## 4. Invariantes de negocio

1. `AutenticarUsuarioUseCase` nunca revela si la causa del fallo fue
   "el correo no existe" o "la contraseña no coincide" — mismo mensaje
   genérico en ambos casos (evita que alguien pueda enumerar correos
   válidos probando el login).
2. `correo` es único por usuario; el intento de crear dos usuarios con
   el mismo correo se rechaza.
3. La contraseña en claro nunca se persiste ni se registra en logs —
   solo su hash.

## 5. Fuera de este contexto, a propósito (v1)

- **Registro público de usuarios**: no existe. El único usuario se crea
  por *seed* automático al arrancar la aplicación, desde variables de
  entorno (mismo mecanismo de "secretos solo por variable de entorno"
  que ya rige credenciales de correo y de base de datos).
- **Recuperación de contraseña**: no existe en v1 (usuario único,
  gestionado manualmente si hace falta).
- **Revocación de sesión antes de su expiración natural** (denylist de
  tokens): decisión consciente de no construirla todavía — ver ADR-08
  en [[02-arquitectura]]. El *logout* de v1 es responsabilidad del
  cliente (descarta el token); el token en sí sigue siendo válido hasta
  expirar.
- **Permisos granulares por rol**: `Rol` existe como enum extensible,
  pero en v1 no hay ninguna decisión de autorización que dependa de su
  valor más allá de "está autenticado" — es terreno preparado para v2,
  no una funcionalidad de v1.
