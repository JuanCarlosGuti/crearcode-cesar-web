// Correo corporativo del dominio propio (11 ago 2026). Reemplaza al
// crearcodecesar@gmail.com temporal que se usó hasta comprar el dominio.
export const CORREO_CORPORATIVO = 'admin@crearcodecesar.com';
export const WHATSAPP_NUMERO = '323 988 5883';
export const WHATSAPP_NUMERO_INTERNACIONAL = '573239885883';

/**
 * Aviso que acompana a cada herramienta de IA, debajo del campo de
 * texto. Texto del documento legal del 28 sep 2026 ("Textos cortos
 * para el sitio"): nombra el pais del proveedor y los tres tipos de
 * dato que no deben escribirse, porque un dueno de consultorio o de
 * comercio pega datos de sus pacientes o clientes sin pensarlo — datos
 * que la Ley 1581 de 2012 considera sensibles.
 */
export const AVISO_IA =
  'Tu texto lo procesa un proveedor de IA externo (EE. UU.) para generar la respuesta. No escribas datos personales de tus clientes, datos de salud ni contraseñas. Las respuestas son orientativas.';

/**
 * Texto de la casilla de consentimiento del formulario de contacto.
 * Obligatoria y sin marcar por defecto: la autorizacion tiene que ser
 * previa, expresa e informada, y "informada" incluye decir que los
 * datos salen del pais.
 */
export const CONSENTIMIENTO_CONTACTO =
  'Autorizo a Crear Code Cesar S.A.S. a tratar mis datos para responder esta solicitud y enviarme una propuesta. Algunos proveedores tratan los datos en EE. UU. y Alemania.';

/**
 * Casilla comercial: OPCIONAL, sin marcar y separada de la
 * obligatoria. No es cosmetica — la Ley 2300 de 2023 fija horarios y
 * frecuencia para el contacto comercial, y la politica v2 (seccion 13)
 * promete que esta finalidad tiene su propia casilla. Mezclarla con la
 * obligatoria seria obtener la autorizacion a cambio de responder una
 * solicitud, que es justo lo que la ley no permite.
 */
export const CONSENTIMIENTO_COMERCIAL =
  'Quiero recibir contenido y novedades de Crear Code por correo o WhatsApp (máximo una vez por semana). Puedo darme de baja cuando quiera.';

/** Casilla del registro de cuenta (misma regla: previa y expresa). */
export const CONSENTIMIENTO_REGISTRO =
  'Autorizo el tratamiento de mi correo para crear y administrar mi cuenta, según la';

/**
 * Fecha de entrada en vigor de la version actual de los documentos
 * legales. La propia politica promete que la fecha de la version
 * vigente se indica al pie, y hasta la auditoria del 28 sep 2026 no se
 * indicaba en ninguna parte.
 */

/**
 * Politica de tratamiento de datos, version 2.
 *
 * Texto del documento legal del 28 sep 2026, que reemplaza los seis
 * parrafos de la v1 (que describian solo el formulario de contacto y
 * se publicaban con un recuadro diciendo que eran un borrador).
 *
 * REVISAR CON ABOGADO. El documento fuente marcaba seis puntos como
 * [CONFIRMAR]; asi quedaron:
 *
 * 1. Horarios y frecuencia de contacto comercial de la Ley 2300 de
 *    2023 (L-V 7 a. m.-7 p. m., sabados 8 a. m.-3 p. m., maximo una vez
 *    por semana, nunca domingos ni festivos). La politica se remite a
 *    la ley sin transcribir el horario: ASI QUE HAY QUE CUMPLIRLO el
 *    dia que se envie la primera comunicacion comercial.
 * 2. Entrenamiento de los proveedores de IA: la politica NO promete lo
 *    que Groq o Cloudflare hacen con el texto (v2.1, decision de Juan,
 *    30 sep 2026). No se puede verificar ni hacerle seguimiento; se
 *    remite a sus condiciones y se pide no escribir datos sensibles,
 *    que es la proteccion que si depende de nosotros.
 * 3. Que las conversaciones de IA no se guardan: CONFIRMADO en el
 *    codigo. Ninguna entidad JPA las persiste (viven en la peticion) y
 *    el manejador de errores registra la causa tecnica sin el texto del
 *    visitante, con un IT que lo exige.
 * 4. Copias de seguridad: NO se publican, por decision de Juan (30 sep
 *    2026). Existen desde ese dia, pero la ley pide tener medidas de
 *    seguridad, no publicarlas, y cada promesa escrita aqui se vuelve
 *    una obligacion. Punto para el abogado: aclarar que los respaldos
 *    guardan lo borrado hasta 30 dias.
 * 5. Plazo de 15 dias habiles para reportar incidentes a la SIC: se
 *    publica como compromiso; falta confirmar el plazo exacto.
 * 6. Pollinations: se mantiene como respaldo del demo y se declara. Ya
 *    no recibe el texto del cliente —solo el titulo que genero el
 *    modelo— desde la correccion P1-8c de la auditoria.
 *
 * Analitica: las menciones a Google Analytics y Microsoft Clarity
 * describen algo construido pero APAGADO mientras contenido/analitica.ts
 * no tenga ids. Si el dueno decide no medir, hay que borrar esas dos
 * menciones (seccion 4 y la tabla de encargados).
 *
 * Pendiente de inscripcion en el RNBD: obligatoria solo por encima de
 * 100.000 UVT de activos (Decreto 090 de 2018) — confirmar con el
 * contador.
 */
export const POLITICA_DE_DATOS = {
  titulo: 'Política de tratamiento de datos personales',
  metaDescripcion:
    'Política de tratamiento de datos personales de Crear Code Cesar S.A.S., conforme a la Ley 1581 de 2012 y la Circular 002 de 2024 de la SIC.',
  version: 'v2.1',
  vigenteDesde: '30 de septiembre de 2026',
  secciones: [
    {
      titulo: '1. Quiénes somos',
      parrafos: [
        'Crear Code Cesar S.A.S. ("Crear Code"), NIT 901.941.017-0, con domicilio en la Calle 4B # 20-36, oficina 303, Valledupar, Cesar, Colombia, es la responsable del tratamiento de los datos personales que se recogen en crearcodecesar.com y en nuestros canales comerciales.',
        `Contacto para temas de datos: correo ${CORREO_CORPORATIVO} (asunto "Datos personales"), WhatsApp +57 ${WHATSAPP_NUMERO}, o correo físico a la dirección de arriba.`,
      ],
    },
    {
      titulo: '2. Por qué existe esta política',
      parrafos: [
        'Cumplimos la Ley 1581 de 2012, el Decreto 1074 de 2015 y las instrucciones de la Superintendencia de Industria y Comercio (SIC), incluida la Circular Externa 002 de 2024 sobre inteligencia artificial. Aquí explicamos, en lenguaje claro, qué datos tratamos, para qué, con quién los compartimos y cómo puedes ejercer tus derechos.',
      ],
    },
    {
      titulo: '3. Definiciones básicas',
      parrafos: [
        'Titular: la persona a quien se refieren los datos.',
        'Tratamiento: cualquier operación sobre ellos (recoger, guardar, usar, enviar, borrar).',
        'Responsable: quien decide sobre el tratamiento; en este caso, Crear Code.',
        'Encargado: quien trata datos por cuenta del responsable (por ejemplo, un proveedor de servidores).',
        'Dato sensible: el que afecta la intimidad o puede generar discriminación, como la salud, la biometría o la orientación política.',
      ],
    },
    {
      titulo: '4. Qué datos tratamos y de dónde vienen',
      parrafos: [
        'Formulario de contacto: nombre, empresa (opcional), correo, celular o WhatsApp, servicio de interés y el mensaje que escribas.',
        'Cuenta de usuario: correo, contraseña (guardada solo como hash cifrado; nunca la conocemos), fecha de registro y verificación del correo.',
        'Herramientas gratuitas (asistente, simulador de chatbot, diagnóstico, demo de diseño): el texto que escribas, el nombre y el rubro de tu negocio, y tus respuestas al cuestionario. También guardamos un identificador técnico y un contador de usos para aplicar los límites diarios.',
        'Cotizaciones y relación comercial: datos de contacto y de facturación de tu empresa, el alcance del proyecto y tu aceptación o rechazo de la propuesta.',
        'WhatsApp y correo: los mensajes que nos envíes por esos canales.',
        'Datos técnicos: dirección IP y datos del navegador, usados para seguridad, prevención de abuso y límites de uso. La dirección IP no se guarda tal cual: se transforma en un resumen criptográfico con una sal que cambia cada día, del que no se puede volver a la dirección original. Si aceptas las cookies de medición, también recogemos estadísticas de navegación con Google Analytics y Microsoft Clarity.',
        'No pedimos datos sensibles ni datos de menores de edad. Te pedimos que no escribas datos de salud, datos financieros ni datos de tus clientes o pacientes en las herramientas de IA ni en el formulario. Si igual los incluyes, los eliminaremos cuando lo detectemos.',
      ],
    },
    {
      titulo: '5. Para qué usamos tus datos',
      parrafos: [
        'Responder tu solicitud y darte información sobre nuestros servicios.',
        'Preparar, enviar y hacer seguimiento a cotizaciones y propuestas.',
        'Ejecutar el contrato: desarrollo, soporte, facturación y cobro.',
        'Crear y administrar tu cuenta, verificar tu correo y recuperar tu contraseña.',
        'Prestar las herramientas gratuitas y aplicar sus límites de uso.',
        'Proteger el sitio contra fraude, spam y abuso.',
        'Mejorar el sitio y nuestros servicios con información agregada o anonimizada.',
        'Solo si lo autorizas aparte: enviarte contenido, invitaciones y novedades comerciales, respetando los horarios y la frecuencia que fija la Ley 2300 de 2023. Puedes retirar esa autorización en cualquier momento sin que afecte lo demás.',
        'Cumplir obligaciones legales, contables y tributarias.',
        'No vendemos ni alquilamos tus datos.',
      ],
    },
    {
      titulo: '6. Inteligencia artificial',
      parrafos: [
        'Qué hacemos: el asistente, el simulador de chatbot y el diagnóstico envían tu texto a un modelo de lenguaje de un proveedor externo para generar la respuesta. El demo de diseño envía a un modelo de imágenes la descripción de la solución, no el texto que tú escribiste.',
        'Qué no hacemos: no usamos tus conversaciones para entrenar modelos propios. Lo que cada proveedor haga con el texto que recibe se rige por sus propias condiciones de uso. Por eso te pedimos no escribir en estas herramientas datos sensibles (salud, información financiera, documentos de identidad) ni datos de otras personas. Tampoco tomamos decisiones que te afecten de forma legal o significativa solo con un proceso automatizado: toda cotización final y toda decisión comercial la revisa una persona.',
        'Resultados orientativos: las respuestas de la IA, los rangos del cotizador y los bocetos son referencias. Pueden contener errores y no son una oferta.',
        'Evaluación y minimización: aplicamos límites de longitud y de uso, enviamos al proveedor solo el texto necesario, y revisamos el riesgo de estas herramientas al menos una vez al año, siguiendo la Circular 002 de 2024 de la SIC.',
      ],
    },
    {
      titulo: '7. Con quién compartimos tus datos',
      parrafos: [
        'Solo con los proveedores que necesitamos para operar, bajo sus condiciones de confidencialidad y seguridad. Actúan como encargados del tratamiento:',
        'Netcup GmbH (Alemania): el servidor donde viven el sitio y la base de datos.',
        'Groq, Inc. (Estados Unidos): procesa el texto del asistente, el simulador y el diagnóstico.',
        'Cloudflare, Inc. (Estados Unidos): DNS del dominio y generación de imágenes del demo de diseño.',
        'Pollinations (servicio abierto): genera las imágenes del demo cuando el proveedor principal falla. Recibe únicamente el título que generó el modelo, nunca el texto que tú escribiste.',
        'Resend, Inc. (Estados Unidos): envío de correos (verificación de cuenta, recuperación de contraseña y cotizaciones).',
        'Google LLC (Estados Unidos): solo si aceptas las cookies de medición, Google Analytics. Las tipografías del sitio se sirven desde nuestro propio servidor, así que navegar por aquí no le comunica nada a Google.',
        'Microsoft Corporation (Estados Unidos): Microsoft Clarity, solo si aceptas las cookies de medición.',
        'Meta Platforms (Estados Unidos): solo si nos escribes por WhatsApp; esa conversación se rige además por la política de Meta.',
        'También podemos entregar datos a las autoridades que los pidan en ejercicio de sus funciones legales.',
      ],
    },
    {
      titulo: '8. Transferencias internacionales',
      parrafos: [
        'Algunos proveedores tratan tus datos en Alemania y en Estados Unidos. Ambos países figuran en la lista de la SIC de países con nivel adecuado de protección (Circular Única, Título V). Al aceptar esta política, autorizas de forma expresa esa transmisión o transferencia.',
      ],
    },
    {
      titulo: '9. Cuánto tiempo guardamos tus datos',
      parrafos: [
        'Solicitudes de contacto que no se convierten en clientes: 24 meses desde el último contacto.',
        'Datos de clientes y contratos: lo que dure la relación, más 10 años por obligaciones contables y comerciales.',
        'Cuentas de usuario: mientras la cuenta esté activa; si pasan 24 meses sin uso, te avisamos y la eliminamos.',
        'Conversaciones con las herramientas de IA: no las guardamos. Solo quedan los contadores de uso, que no contienen tu texto y se reinician cada día. Los proveedores las conservan según sus propias políticas.',
        'Datos técnicos y de seguridad: hasta 90 días.',
        'Al vencer el plazo, eliminamos o anonimizamos la información.',
      ],
    },
    {
      titulo: '10. Tus derechos',
      parrafos: [
        'Como titular puedes: conocer, actualizar y rectificar tus datos; pedir prueba de la autorización que nos diste; saber qué uso le hemos dado a tus datos; revocar la autorización o pedir que los eliminemos, salvo que exista un deber legal o contractual de conservarlos; acceder gratis a ellos; y presentar quejas ante la SIC, después de agotar el trámite con nosotros.',
        'Además, de forma voluntaria, te entregamos una copia de tus datos en un formato de uso común si la pides (portabilidad) y te dejamos oponerte al uso de tus datos con fines comerciales.',
      ],
    },
    {
      titulo: '11. Cómo ejercer tus derechos',
      parrafos: [
        `Escríbenos a ${CORREO_CORPORATIVO} o por WhatsApp e incluye tu nombre, tu forma de contacto, qué pides y los documentos que quieras hacer valer. Si actúa otra persona en tu nombre, debe acreditar que te representa.`,
        'Consultas (saber qué datos tenemos): respondemos en máximo 10 días hábiles. Si no es posible, te explicamos por qué y respondemos en máximo 5 días hábiles más.',
        'Reclamos (corregir, borrar o revocar): respondemos en máximo 15 días hábiles, prorrogables 8 días hábiles más con aviso. Si el reclamo está incompleto, te pedimos corregirlo en 5 días; si pasan 2 meses sin que respondas, entendemos que desististe.',
        'También puedes pedirnos que eliminemos tu cuenta escribiéndonos por cualquiera de esos canales.',
      ],
    },
    {
      titulo: '12. Cómo protegemos tus datos',
      parrafos: [
        'Conexión cifrada (HTTPS) en todo el sitio.',
        'Contraseñas guardadas con hash BCrypt: ni nosotros podemos leerlas.',
        'Tokens de un solo uso para verificar el correo y recuperar la contraseña.',
        'Acceso al panel interno solo para personal autorizado.',
        'Límites de uso y protección contra abuso.',
        'No escribimos datos personales en los registros técnicos del servidor ni en las direcciones web.',
        'Si ocurre un incidente que comprometa tus datos, lo reportamos a la SIC dentro de los 15 días hábiles siguientes a su detección y te avisamos a ti cuando el incidente pueda afectarte.',
      ],
    },
    {
      titulo: '13. Autorización',
      parrafos: [
        'Te pedimos autorización previa, expresa e informada mediante una casilla que no viene marcada. Guardamos la prueba: la fecha, la versión de la política que aceptaste y el canal. Las finalidades comerciales opcionales tienen su propia casilla, separada de la obligatoria.',
      ],
    },
    {
      titulo: '14. Cambios a esta política',
      parrafos: [
        'Si cambiamos algo sustancial, publicamos la nueva versión en esta página con su fecha. Si el cambio afecta las finalidades, te avisamos por correo y, cuando la ley lo exija, te pedimos una nueva autorización.',
      ],
    },
    {
      titulo: '15. Vigencia',
      parrafos: [
        'Esta versión rige desde la fecha indicada al pie de esta página. Las bases de datos se conservarán mientras se cumplan las finalidades descritas.',
      ],
    },
  ],
} as const;

/**
 * REVISAR CON ABOGADO — las secciones de herramientas con IA y de
 * cuentas de cliente son nuevas (auditoria del 28 sep 2026); las demas
 * vienen de la v1 sin cambios de fondo.
 */
export const TERMINOS_DE_USO = {
  titulo: 'Términos de uso',
  metaDescripcion: 'Términos de uso del sitio web de Crear Code Cesar S.A.S.',
  version: 'v2',
  vigenteDesde: '28 de septiembre de 2026',
  secciones: [
    {
      titulo: 'Sobre este sitio',
      parrafos: [
        'Este sitio web es operado por Crear Code Cesar S.A.S. (NIT 901.941.017-0, Valledupar, Cesar, Colombia) con fines informativos y de contacto comercial. El uso del formulario de contacto no genera, por sí solo, ninguna relación contractual entre el visitante y Crear Code Cesar S.A.S.',
      ],
    },
    {
      titulo: 'Uso permitido',
      parrafos: [
        'El contenido de este sitio (textos, proyectos, artículos) puede consultarse libremente; su reproducción total o parcial con fines comerciales requiere autorización previa de Crear Code Cesar S.A.S.',
      ],
    },
    {
      titulo: 'Herramientas con inteligencia artificial',
      parrafos: [
        'El asistente, el simulador de chatbot, el diagnóstico digital y el demo de diseño generan sus respuestas con modelos de inteligencia artificial. Sus resultados son orientativos y pueden contener errores: no son una cotización, ni una asesoría profesional, ni un compromiso de precio o de plazo.',
        'Cualquier cifra, alcance o fecha solo es vinculante cuando está en una cotización firmada por Crear Code Cesar S.A.S.',
        'Estas herramientas tienen cupos diarios de uso y pueden dejar de estar disponibles sin aviso, por límites o fallos del proveedor externo que las genera.',
      ],
    },
    {
      titulo: 'Cuentas de cliente',
      parrafos: [
        'Eres responsable de mantener en reserva la contraseña de tu cuenta y de la actividad que se realice con ella. Si detectas un uso que no reconoces, escríbenos de inmediato.',
        'Podemos suspender una cuenta que se use para abusar de las herramientas del sitio o para intentar afectar su funcionamiento.',
      ],
    },
    {
      titulo: 'Disponibilidad',
      parrafos: [
        'Se hace un esfuerzo razonable por mantener el sitio disponible y actualizado, sin garantizar disponibilidad ininterrumpida.',
      ],
    },
    {
      titulo: 'Contacto',
      parrafos: [
        `Para cualquier consulta sobre estos términos, escribe a ${CORREO_CORPORATIVO} o por WhatsApp al ${WHATSAPP_NUMERO}.`,
      ],
    },
  ],
} as const;
