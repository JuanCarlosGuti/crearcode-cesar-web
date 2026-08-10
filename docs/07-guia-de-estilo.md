# 07 — Guía de estilo

## Parte 1 — Convenciones de código

### Dominio en español
Todo el modelo de dominio (`dominio/`) usa nombres en español:
`SolicitudDeContacto`, `EstadoSolicitud`, `RegistrarSolicitudUseCase`,
etc. (ver [[03-modelo-de-dominio]]). Las capas `aplicacion/` e
`infraestructura/` heredan esa convención para todo lo que representa
concepto de negocio (`SolicitudRepositorio`, `NotificadorPort`);
términos puramente técnicos sin equivalente natural de negocio pueden
quedar en inglés cuando es lo estándar del ecosistema (`DTO`,
`Controller`, `Repository` como sufijo técnico, `Mapper`).

### Clean code (backend y frontend)
- Nombres que explican intención, no comentarios que la parcheen.
- Funciones/métodos cortos, con un nivel de abstracción por función.
- Sin comentarios que expliquen *qué* hace el código cuando el nombre ya
  lo dice; comentarios solo para el *por qué* cuando no es obvio (una
  regla de negocio no evidente, una limitación externa).
- Sin código muerto ni features a medio terminar en el repositorio.
- Los tests son documentación viva: un test bien nombrado
  (`debeRechazarTransicionDeConvertidaADescartada`) vale más que un
  comentario.

### Commits
Formato tipo *Conventional Commits*, con la descripción en español:

```
<tipo>(<alcance opcional>): <descripción breve en imperativo>

[cuerpo opcional explicando el porqué, no el qué]
```

Tipos usados: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`,
`build`, `ci`. Ejemplos:

```
feat(dominio): agrega maquina de estados de EstadoSolicitud
test(aplicacion): cubre caso triste de RegistrarSolicitudUseCase sin consentimiento
docs: agrega guia de estilo con propuestas de paleta
fix(rest): evita exponer stacktrace en error 500 de SolicitudController
```

Cada commit de la Etapa 2 corresponde, en general, a un issue o a un
paso TDD claro (test → implementación → refactor), no a una mezcla de
cambios no relacionados.

### Frontend: signals-first
- **Preferir siempre** `signal`, `computed` y `effect` sobre patrones
  legacy (`BehaviorSubject` para estado de UI simple, `ngOnChanges`
  extensivo, `ChangeDetectorRef` manual).
- **Control flow moderno**: usar `@if`, `@for`, `@switch` en templates;
  no usar `*ngIf`/`*ngFor` (sintaxis legacy de directivas estructurales).
- **Sin Zone.js**: el proyecto corre en modo zoneless desde el inicio
  (ver ADR-01 en [[02-arquitectura]]); cualquier librería que dependa de
  Zone.js para funcionar queda descartada salvo justificación explícita
  documentada como nuevo ADR.
- **Standalone siempre**: sin `NgModule` en código nuevo.
- **Signal Forms** para formularios con validación reactiva (el
  formulario de contacto es el caso principal); evitar
  `ReactiveFormsModule` clásico en código nuevo.
- Estado derivado siempre con `computed`, nunca duplicado manualmente en
  otro signal que haya que sincronizar a mano.

## Parte 2 — Guía visual

### Tono de los textos
Cercano, claro, profesional, en español colombiano. Sin jerga técnica
hacia el cliente ("automatizamos la atención de tu negocio", nunca
"implementamos agentes LLM con RAG"). Frases cortas, orientadas a
beneficio del negocio del lector, no a la tecnología usada. Ver textos
completos en [[08-contenido]].

### Propuestas de paleta de color

No existía identidad de marca previa (logo/colores), así que se
propusieron tres opciones sobrias y profesionales. **Paleta elegida:
Opción C — "Minimal Corporativo"** (ver detalle más abajo); las
opciones A y B quedan documentadas como alternativas descartadas, por
si se quiere revisar la decisión más adelante. Las tres cumplen
contraste WCAG AA para texto sobre fondo en sus combinaciones
principales (a verificar de nuevo con los tonos finales exactos durante
implementación).

#### Opción A — "Caribe Confiable" (descartada)
Azul profundo (confianza, tecnología) con un acento cálido que evoca el
Caribe colombiano sin caer en lo turístico/informal.

| Uso | Color | Hex |
|---|---|---|
| Primario | Azul profundo | `#0B3B5C` |
| Primario claro (hover/fondos sutiles) | Azul medio | `#1D5C82` |
| Acento / CTA | Naranja coral | `#F2994A` |
| Fondo | Blanco cálido | `#FBFAF8` |
| Texto principal | Gris casi negro | `#1A1D21` |
| Texto secundario | Gris medio | `#5B6470` |
| Éxito (admin) | Verde | `#2E9E5B` |
| Alerta/nueva (admin) | Ámbar | `#D98E04` |
| Error | Rojo | `#C4453A` |

#### Opción B — "Tecnología Cercana" (descartada)
Verde azulado (teal) más moderno, con acento dorado. Transmite
innovación sin perder seriedad.

| Uso | Color | Hex |
|---|---|---|
| Primario | Teal oscuro | `#0F4C4C` |
| Primario claro | Teal medio | `#1B7A72` |
| Acento / CTA | Dorado | `#E0A93A` |
| Fondo | Blanco hueso | `#FAFBF9` |
| Texto principal | Gris carbón | `#1E2422` |
| Texto secundario | Gris medio | `#5C6663` |
| Éxito (admin) | Verde | `#2E9E5B` |
| Alerta/nueva (admin) | Ámbar | `#D98E04` |
| Error | Rojo | `#C4453A` |

#### Opción C — "Minimal Corporativo" (elegida)
Monocromo grafito/azul marino con un único acento verde (crecimiento).
La opción más austera, orientada al público de TI de empresa mediana.
Esta es la paleta oficial del sitio a partir de ahora.

| Uso | Color | Hex |
|---|---|---|
| Primario | Grafito azulado | `#22303F` |
| Primario claro | Gris azulado medio | `#4C5F73` |
| Acento / CTA | Verde crecimiento | `#2E7D53` |
| Fondo | Blanco frío | `#F7F8FA` |
| Texto principal | Casi negro | `#171B1F` |
| Texto secundario | Gris medio | `#5A6472` |
| Éxito (admin) | Verde (= acento) | `#2E7D53` |
| Alerta/nueva (admin) | Ámbar | `#8F5C08` |
| Error | Rojo | `#C4453A` |

> Decisión tomada por el usuario: se adopta la Opción C — "Minimal
> Corporativo" como paleta oficial del sitio, a usar desde la fase F3
> de la Etapa 2.
>
> **Ajuste F6 (ISS-077/ISS-078)**: tres colores se oscurecieron por
> contraste insuficiente como texto (mínimo AA 4.5:1,
> [[06-plan-de-pruebas]] §5): acento/éxito `#3C9D6E` → `#2E7D53`
> (3.36:1 → ~5:1), verde de WhatsApp `#25D366` → `#158542` solo en el
> fondo de los botones — el ícono oficial no se toca — (1.98:1 → ~4.7:1),
> y el ámbar de alerta `#D98E04` → `#8F5C08` (2.33:1 en el badge
> "NUEVA" del panel admin → ~5:1).

### Tipografía sugerida

- **Opción recomendada**: una sola familia variable para todo el sitio
  — **Inter** (encabezados y cuerpo). Es gratuita, tiene excelente
  legibilidad en pantalla, soporte completo de acentos/ñ del español, y
  al ser variable reduce el peso de descarga (bueno para Lighthouse
  Performance).
- **Alternativa con más carácter**: **Poppins** para encabezados
  (geométrica, cercana, un poco más cálida) + **Inter** para cuerpo de
  texto (mejor legibilidad en párrafos largos que Poppins).
- Cargar solo los pesos realmente usados (ej. 400, 500, 600, 700) y con
  `font-display: swap` para no bloquear el renderizado del texto.

### Componentes UI base

- **Botón primario**: fondo color primario, texto blanco, usado para el
  CTA principal ("Agenda tu consulta gratuita").
- **Botón secundario/WhatsApp**: contorno o fondo con el verde de
  WhatsApp reconocible, usado específicamente para el CTA de WhatsApp
  (no se reemplaza por el color de marca, para mantener reconocimiento
  de plataforma).
- **Tarjeta de servicio**: ícono o imagen simple + título + resumen
  breve + enlace "Conocer más"; misma estructura para los 3 servicios.
- **CTA doble**: bloque con los dos botones (agendar / WhatsApp) uno
  junto al otro en desktop, apilados en móvil.
- **Badge de estado (panel admin)**: cápsula de color por estado —
  `NUEVA` (acento/alerta), `CONTACTADA` (primario claro), `CONVERTIDA`
  (éxito/verde), `DESCARTADA` (gris neutro, no rojo — no es un error,
  es una decisión comercial).
- **Campo de formulario**: label siempre visible (no solo placeholder),
  mensaje de error en rojo debajo del campo, borde de error visible sin
  depender solo del color (icono o texto, por accesibilidad).
- **Sección FAQ**: acordeón simple, un ítem expandido a la vez o varios
  simultáneos (decisión de implementación libre, no de negocio).

### Evolución visual (fase F8.5, 28 jul 2026)

Decisión del usuario al cerrar F8: el sitio es correcto pero plano —
se **evoluciona** la paleta oficial (Opción C se mantiene como
identidad) añadiendo profundidad y movimiento, sin sacrificar nada de
lo ganado en F6 (AA, Lighthouse 98-100). Valores nuevos:

- **Gradiente de marca** (fondos de hero y secciones destacadas):
  de primario `#22303F` a un azul petróleo profundo `#14424D`
  (dirección ~135°). Solo como fondo decorativo: el texto encima va
  en blanco `#FFFFFF` o el gris claro de fondo `#F7F8FA`, y cada
  combinación texto/zona del gradiente debe verificar AA 4.5:1.
- **Acento luminoso** (solo detalles decorativos — subrayados,
  íconos, bordes de tarjeta en hover — **nunca texto**): verde claro
  `#4CC38A`. Para texto sigue valiendo únicamente el acento oficial
  `#2E7D53`.
- **Elevación (sombras)**, tres niveles sobre fondo claro:
  `--sombra-1: 0 1px 3px rgb(23 27 31 / 8%)` (reposo),
  `--sombra-2: 0 4px 12px rgb(23 27 31 / 10%)` (tarjetas),
  `--sombra-3: 0 12px 32px rgb(23 27 31 / 14%)` (hover/destacados).
- **Transiciones estándar**: 180ms `ease-out` para hover/focus;
  entrada de secciones (scroll-reveal) 400ms con desplazamiento
  máximo de 16px, **una sola vez** por elemento.
- **`prefers-reduced-motion: reduce` apaga todo movimiento** (las
  utilidades de animación son no-op y el contenido aparece de
  inmediato). El contenido nunca depende de JS para ser visible
  (progressive enhancement: sin JS no hay animación, pero se ve todo).
- **Radios**: tarjetas y bloques destacados pueden usar un radio mayor
  (`--radio-borde-grande: 16px`) que el radio base de los controles.

Los componentes nuevos de esta fase (tarjetas de beneficios de la
cuenta, hero con gradiente) usan estos tokens; los valores exactos
viven como custom properties en `styles.scss` — este documento es la
fuente de verdad de sus valores.

### Rediseño F10e (10 ago 2026) — patrones nuevos

El rediseño de la fase F10e (prototipo aprobado, ISS-133 a ISS-135)
no cambia la paleta ni los tokens de F8.5: los reutiliza. Patrones
que quedan como convención del sitio:

- **Tarjeta clara sobre gradiente**: cualquier tarjeta que viva sobre
  el gradiente de marca (tarjeta del demo en el hero) va con fondo
  blanco y `--sombra-2`; dentro de ella el botón primario recupera su
  color normal (fuera, sobre el gradiente, se invierte a blanco).
- **Rejillas con `minmax(0, 1fr)`**, no `1fr` a secas: con zoom de
  texto al 200% el contenido mínimo de una tarjeta puede exceder su
  columna y desbordar la página. Lo atrapó el e2e de accesibilidad en
  la rejilla de herramientas de la Home.
- **Espacios reservados honestos**: borde discontinuo, badge "Espacio
  reservado" y texto que explica por qué está vacío. Reemplazan a
  cualquier contenido ficticio (los testimonios inventados de la v1
  se eliminaron con esta regla).
- **Aside pegajoso** en páginas de contenido largo (servicios): dos
  columnas desde `64rem`, `position: sticky` solo en esa media query;
  en móvil cae al final del flujo, después del CTA.
- **Presupuesto de estilos por componente**: `anyComponentStyle` sube
  a 8kB de aviso / 12kB de error en `angular.json`. La Home tiene
  siete secciones con estilos propios (7.1 kB) y el límite anterior de
  4 kB ya no describe una página real del sitio; el presupuesto sigue
  existiendo para atrapar crecimiento descontrolado.

### Imágenes (convención para cuando existan imágenes de contenido)

Al cierre de la fase F6 el sitio no tiene ninguna imagen de contenido
todavía (`<img>`/`NgOptimizedImage`) — el único archivo de imagen es
`public/imagenes/og-defecto.jpg`, referenciado solo como metadato
`og:image`, no renderizado en ninguna página. No tiene sentido optimizar
imágenes que no existen (ISS-076 se cierra como convención documentada,
no como trabajo sobre assets reales), pero la convención queda fijada
para cuando se agreguen imágenes reales (casos de éxito, blog, equipo):

- **Componente**: usar siempre `NgOptimizedImage` (`ngSrc`, no `src`) —
  aplica lazy loading automático fuera del viewport inicial, exige
  `width`/`height` explícitos (evita *layout shift*, relevante para
  Lighthouse Performance/CLS) y con `priority` solo en la imagen más
  above-the-fold de cada página (ej. la del hero de Home, si llega a
  tener una).
- **Formato**: WebP como formato de entrega (fallback automático de
  `NgOptimizedImage`/el navegador no es necesario en 2026, soporte ya
  es universal); AVIF opcional si el ahorro de peso es significativo
  para una imagen concreta.
- **Tamaño**: exportar al tamaño máximo real de renderizado (no subir
  un original de cámara/diseño sin redimensionar); usar `srcset`/
  `ngSrcset` cuando la misma imagen se muestra en tamaños distintos
  según viewport (ej. tarjeta vs. detalle).
- **Peso**: igual criterio que se usó para `og-defecto.jpg` en ISS-075
  (JPEG/WebP calidad ~85, cada imagen revisada individualmente antes de
  commitear — sin subir binarios de varios MB sin comprimir).
- **Alt text**: siempre descriptivo y en español, nunca vacío salvo que
  la imagen sea puramente decorativa (`alt=""` explícito en ese caso,
  no la omisión del atributo) — requisito también del checklist de
  accesibilidad (ISS-078).
