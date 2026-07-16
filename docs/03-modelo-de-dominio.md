# 03 — Modelo de dominio

Contexto acotado de esta v1: **`leads`** — captura, validación y gestión
del ciclo de vida de las solicitudes de contacto que llegan desde el
sitio público. Es el único contexto de dominio con lógica de negocio real
en v1; el resto del sitio es contenido servido (ver [[08-contenido]]).

Convención: todo el modelo se nombra en español, sin dependencias de
Spring/JPA/framework alguno (ver regla de dependencias en
[[02-arquitectura]]).

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
