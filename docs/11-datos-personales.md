# 11 — Datos personales: evaluación de impacto y procedimiento

Dos documentos que la **política de tratamiento de datos v2** promete
por escrito y que, sin existir, la harían falsa:

1. La **evaluación de impacto** de las herramientas de IA, que la
   política dice revisar «al menos una vez al año» siguiendo la
   Circular Externa 002 de 2024 de la SIC.
2. El **procedimiento** para responder consultas y reclamos dentro de
   los plazos que la política se compromete a cumplir.

> **Revisar con abogado.** Esto describe lo que el sistema hace y cómo
> se responde; no es un dictamen jurídico. La política y este documento
> se revisan juntos.

**Última revisión: 28 de septiembre de 2026.** Próxima: septiembre de
2027, o antes si cambia un proveedor o se añade una herramienta.

---

## Parte 1 — Evaluación de impacto de las herramientas de IA

### 1.1 Qué se evalúa

Las cuatro herramientas de `/herramientas` que envían texto del
visitante a un modelo de un tercero:

| Herramienta | Qué manda al proveedor | Proveedor | Cupo diario |
|---|---|---|---|
| Asistente | La conversación (máx. 20 turnos de 1000 caracteres) | Groq (EE. UU.) | 800 global / 50 con cuenta / 10 anónimo |
| Simulador de chatbot | Nombre y rubro del negocio + la conversación | Groq (EE. UU.) | 400 / 50 / 10 |
| Diagnóstico digital | Seis pares pregunta-respuesta del cuestionario | Groq (EE. UU.) | 200 / 10 / 2 |
| Demo de diseño | Sector, qué hace y qué necesita (texto); al modelo de **imágenes** solo el título que generó el modelo de texto | Groq + Cloudflare Workers AI, con Pollinations de respaldo | 100 / 3, solo cuentas registradas |

### 1.2 El riesgo real

El riesgo no es que el visitante escriba sus propios datos —eso lo
decide él—, sino que **escriba los de terceros**: un dueño de
consultorio pegando el historial de un paciente, o un comercio pegando
su lista de clientes, para «ver qué dice el chatbot». Son datos
sensibles de personas que no están en la conversación y que no
autorizaron nada.

Segundo riesgo, menor: el texto **sale del país** hacia proveedores
cuyas políticas de retención no controlamos.

### 1.3 Idóneo, necesario, razonable y proporcional

Los cuatro criterios que exige la Circular 002 de 2024:

- **Idóneo.** Las herramientas sirven para que una pyme vea qué se
  puede automatizar en su negocio. No perfilan personas, no puntúan a
  nadie y no deciden nada sobre nadie.
- **Necesario.** El texto que se envía es el que el visitante escribió
  para obtener la respuesta; no se añade ningún dato que él no haya
  puesto. Al proveedor de imágenes, desde septiembre de 2026, ni
  siquiera se le manda su texto: solo el título que generó el modelo.
- **Razonable.** El beneficio (una orientación gratuita e inmediata) es
  proporcionado al dato tratado (una descripción de un negocio).
- **Proporcional.** Hay tope de longitud por mensaje (1000 caracteres),
  de conversación (20 turnos) y cupos diarios por persona y por red.

### 1.4 Medidas aplicadas

| Medida | Dónde vive en el código |
|---|---|
| Aviso visible en las cuatro herramientas: no escribas datos de tus clientes, de salud ni contraseñas | `contenido/legales.ts` (`AVISO_IA`) |
| No se guarda ninguna conversación — viven en la petición | Ninguna entidad JPA las persiste |
| Los registros técnicos no llevan el texto del visitante | `GlobalExceptionHandler`, con un IT que lo exige |
| Topes de longitud y de turnos | `MensajeDeChat`, `ConversacionDeAsistente` |
| Cupos por persona, por red y global | `CupoDeIa`, `HuellaDeRed` |
| La IP no se guarda: resumen con sal que cambia cada día | `HuellaDeRed` |
| El texto del visitante no puede salirse de su delimitador en el prompt | `DatoDelVisitante` |
| Ninguna decisión automatizada: toda cotización la revisa una persona | Política v2 §6; el cotizador da rangos, no ofertas |
| Respaldo si el proveedor falla, sin exponer más datos | `GeneradorDeRespuestasConRespaldo` |

### 1.5 Riesgo residual

- **Lo que el visitante decida escribir.** El aviso reduce la
  probabilidad; no la elimina. Si se detectan datos de terceros en una
  conversación, no hay nada que borrar de nuestro lado —no se guarda—
  pero sí hay que revisar si el aviso se está leyendo.
- **La retención y el entrenamiento en el proveedor.** Fuera de nuestro
  control más allá de sus condiciones. Por eso, desde la v2.1 (30 sep
  2026), la política **no promete** lo que Groq o Cloudflare hacen con
  el texto: se remite a sus condiciones y pide no escribir datos
  sensibles, que es la mitigación que sí depende de nosotros.

### 1.6 Qué revisar cada año

1. ¿Cambió algún proveedor, o alguno empezó a entrenar con los datos
   de API?
2. ¿Se añadió una herramienta que mande datos a un tercero?
3. ¿Sigue visible el aviso en las cuatro?
4. ¿Hubo algún incidente o reclamo relacionado con las herramientas?
5. ¿Sirvieron los cupos, o hubo abuso?

---

## Parte 2 — Procedimiento para consultas y reclamos

### 2.1 Los plazos, que no son negociables

La política se compromete a estos y son los de la ley:

| Tipo | Plazo | Prórroga |
|---|---|---|
| **Consulta** (saber qué datos tenemos) | 10 días hábiles | 5 días hábiles más, avisando por qué |
| **Reclamo** (corregir, borrar, revocar) | 15 días hábiles | 8 días hábiles más, avisando |
| Reclamo incompleto | Se pide completarlo en 5 días | A los 2 meses sin respuesta, se entiende desistido |

Los días son **hábiles**: no cuentan sábados, domingos ni festivos
colombianos, que son muchos.

### 2.2 Qué hacer cuando llega uno

1. **Reconocer el mismo día.** Responder que se recibió y en qué plazo
   se contestará. El reloj ya está corriendo.
2. **Registrarlo** en la hoja de control (fecha de llegada, canal,
   titular, qué pide, fecha límite).
3. **Verificar quién pide.** Si escribe desde el correo de la cuenta,
   basta. Si actúa un tercero, tiene que acreditar que representa al
   titular.
4. **Buscar los datos.** Panel `/admin` para solicitudes y
   cotizaciones; la cuenta en la tabla de usuarios.
5. **Resolver:**
   - *Conocer* → enviar qué hay, en lenguaje claro.
   - *Actualizar o rectificar* → corregirlo y confirmarlo.
   - *Suprimir* → la persona puede hacerlo sola desde
     **/mi-cuenta → Eliminar mi cuenta**. Si pide borrar solicitudes o
     cotizaciones, revisar antes si hay un deber legal de conservarlas
     (una cotización aceptada es registro contable).
   - *Revocar la autorización comercial* → quitar la marca y no
     volver a escribirle con fines comerciales.
6. **Responder por escrito** y guardar la respuesta.
7. **Cerrar** en la hoja de control.

### 2.3 Si hay un incidente de seguridad

1. Contenerlo: cortar el acceso, rotar credenciales.
2. Anotar qué pasó, cuándo se detectó, qué datos y cuántas personas.
3. **Reportarlo a la SIC dentro de los 15 días hábiles** siguientes a
   la detección —desde que se sabe, no desde que se resuelve—, en el
   micrositio de Protección de Datos de sic.gov.co, módulo **"Reporte
   de incidentes de seguridad"**. Aplica aunque la empresa no esté
   inscrita en el RNBD (SIC, concepto 21-17773 de 2021, verificado el
   30 sep 2026). Se puede mandar un reporte preliminar y completarlo
   después: no esperar a tener todo.
4. Avisar a las personas afectadas cuando el incidente pueda
   perjudicarlas.
5. Escribir qué se cambió para que no se repita.
6. **Guardar la prueba**: quién manejó el incidente, el comprobante del
   reporte a la SIC, el aviso a los titulares si lo hubo y la
   evaluación del riesgo.

### 2.4 Comunicaciones comerciales (Ley 2300 de 2023)

Hoy el sitio no envía publicidad. Esto es la regla para el día que se
mande la primera promoción por correo, WhatsApp, SMS o llamada. No
aplica a responderle a quien escribió, ni a los correos que el propio
cliente provoca (verificación, recuperación, una cotización que pidió).

- **Solo a quien marcó la casilla comercial**, que es aparte de la
  obligatoria (art. 5, parágrafo 2).
- **Horario propio, decidido por el dueño (30 sep 2026)**: lunes a
  jueves de 8:00 a. m. a 6:00 p. m. y viernes de 8:00 a. m. a 3:00
  p. m. **Nunca sábados, domingos ni festivos.** Es más estricto que la
  ley (L-V 7 a. m.-7 p. m., sábados 8 a. m.-3 p. m.), y eso siempre
  está permitido.
- **Máximo una vez al día y por un solo canal en la misma semana** (art.
  3): si esta semana se le escribió por correo, no se le escribe
  también por WhatsApp.
- **Forma fácil de darse de baja** en cada mensaje: un enlace en el
  correo, "responde BAJA" en WhatsApp (art. 5, parágrafo 2).

### 2.5 Lo que todavía falta

- [x] **Copias de seguridad** del PostgreSQL: activas desde el 30 sep
      2026 y con restauración probada.
- [x] **Sacarlas del servidor**: en Cloudflare R2 desde el 30 sep
      2026, con restauración probada desde allí. La política **no** las
      menciona, por decisión del dueño: la ley pide tenerlas, no
      publicarlas. Queda para el abogado aclarar que lo borrado sigue
      en los respaldos hasta 30 días.
- [x] Plazo y canal de reporte de incidentes: verificados en la norma
      (§2.3).
- [x] Horarios y frecuencia de la Ley 2300: verificados en la norma, y
      con horario propio más estricto (§2.4).
- [ ] Para el abogado, si algún día se consulta: que lo borrado sigue
      hasta 30 días en los respaldos, y que remitir a las condiciones de
      Groq y Cloudflare (v2.1) basta como responsable.
- [ ] Confirmar con el contador si aplica el **RNBD** (obligatorio por
      encima de 100.000 UVT de activos, Decreto 090 de 2018).
- [ ] Hoja de control de consultas y reclamos: mientras no haya
      ninguna, una hoja de cálculo basta; conviene que exista antes de
      la primera.
