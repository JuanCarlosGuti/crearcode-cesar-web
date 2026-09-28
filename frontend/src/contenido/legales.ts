// Correo corporativo del dominio propio (11 ago 2026). Reemplaza al
// crearcodecesar@gmail.com temporal que se usó hasta comprar el dominio.
export const CORREO_CORPORATIVO = 'admin@crearcodecesar.com';
export const WHATSAPP_NUMERO = '323 988 5883';
export const WHATSAPP_NUMERO_INTERNACIONAL = '573239885883';

/**
 * Aviso que acompana a cada herramienta de IA. Lo que el visitante
 * escribe sale a un proveedor externo (Groq en EE. UU.; en el demo de
 * diseno tambien Cloudflare Workers AI), y un dueno de consultorio o de
 * comercio puede pegar datos de sus clientes sin pensarlo — datos que la
 * Ley 1581 de 2012 considera sensibles. Auditoria del 28 sep 2026, §11.
 */
export const AVISO_IA =
  'No escribas datos personales de tus clientes: lo que escribes se procesa con un proveedor de IA externo.';

/**
 * Fecha de entrada en vigor de la version actual de los documentos
 * legales. La propia politica promete que "la fecha de la version
 * vigente se indica al pie de esta pagina" y hasta la auditoria del 28
 * sep 2026 no se indicaba en ninguna parte.
 */
export const VIGENTE_DESDE = '28 de septiembre de 2026';

/**
 * REVISAR CON ABOGADO — todo este documento.
 *
 * La v1 eran seis parrafos que describian solo el formulario de
 * contacto, con un aviso visible de "esto es un borrador" (auditoria
 * del 28 sep 2026, C3 y P0-3). Se quito ese aviso y se escribio lo que
 * el sitio hace de verdad hoy: cuentas de cliente, cuatro herramientas
 * de IA que mandan texto del visitante fuera del pais, y seis
 * encargados del tratamiento que nunca se habian nombrado.
 *
 * Lo que sigue es una descripcion honesta y completa del tratamiento
 * real, redactada para que un abogado la revise — no un dictamen
 * juridico. Falta que un profesional confirme la forma de las
 * clausulas frente a la Ley 1581 de 2012, el Decreto 1377 de 2013 y el
 * registro de bases de datos ante la SIC.
 */
export const POLITICA_DE_DATOS = {
  titulo: 'Política de tratamiento de datos personales',
  metaDescripcion:
    'Política de tratamiento de datos personales de Crear Code Cesar S.A.S., conforme a la Ley 1581 de 2012.',
  version: 'v2',
  secciones: [
    {
      titulo: 'Responsable del tratamiento',
      parrafos: [
        'Crear Code Cesar S.A.S., NIT 901941017-0, con domicilio en la Calle 4B # 20-36, Oficina 303, Barrio Callejas, Valledupar, Cesar, Colombia.',
        `Para cualquier asunto relacionado con tus datos personales, incluido el ejercicio de tus derechos, escríbenos a ${CORREO_CORPORATIVO} o por WhatsApp al ${WHATSAPP_NUMERO}.`,
      ],
    },
    {
      titulo: 'Qué datos recolectamos y para qué',
      parrafos: [
        'Formulario de contacto: nombre, empresa (si aplica), correo electrónico, teléfono y el mensaje que nos escribas. Los usamos para responderte y hacer seguimiento comercial a tu solicitud.',
        'Cuenta de cliente: correo electrónico y una contraseña, que guardamos siempre cifrada con un algoritmo de un solo sentido — ni nosotros podemos leerla. Los usamos para identificarte, permitirte entrar y mostrarte tus cotizaciones.',
        'Cotizaciones: los datos del negocio y del proyecto necesarios para preparar y enviarte una propuesta.',
        'Herramientas de inteligencia artificial: el texto que escribas en el asistente, el simulador de chatbot, el diagnóstico digital o el demo de diseño. Ese texto se envía a un proveedor externo para generar la respuesta, como se explica más abajo.',
        'Datos técnicos: registramos tu dirección IP de forma transformada —un resumen criptográfico con una sal que cambia cada día, del que no se puede volver a la dirección original— únicamente para controlar cuántas veces se usan las herramientas de inteligencia artificial y para frenar abusos. No usamos cookies de seguimiento ni perfilado publicitario.',
      ],
    },
    {
      titulo: 'Encargados del tratamiento',
      parrafos: [
        'Para operar el sitio nos apoyamos en proveedores que, en los términos de la Ley 1581 de 2012, actúan como encargados del tratamiento. Estos son todos los que pueden tratar datos que te identifican:',
        'Netcup GmbH (Alemania), donde está alojado el servidor que guarda la base de datos del sitio: solicitudes de contacto, cuentas y cotizaciones. El servidor está físicamente en Estados Unidos.',
        'Groq, Inc. (Estados Unidos), que genera las respuestas del asistente, el simulador y el diagnóstico. Recibe el texto que escribas en esas herramientas.',
        'Cloudflare, Inc. (Estados Unidos), cuyo servicio Workers AI genera las imágenes del demo de diseño, y que además presta el servicio de DNS del dominio.',
        'Pollinations (servicio abierto), que genera las imágenes del demo de diseño cuando el proveedor principal falla. Recibe la descripción del boceto dentro de la dirección web de la petición.',
        'Resend, Inc. (Estados Unidos), que entrega los correos que te enviamos: verificación de cuenta, recuperación de contraseña y cotizaciones.',
        'Google LLC (Estados Unidos), cuyo servicio Google Fonts sirve las tipografías del sitio; al cargarlas, tu navegador le comunica tu dirección IP.',
        'Ninguno de estos proveedores recibe tus datos para fines propios, ni para venderlos o cederlos a terceros.',
      ],
    },
    {
      titulo: 'Transferencia internacional de datos',
      parrafos: [
        'Como se desprende de la lista anterior, tus datos se procesan fuera de Colombia, principalmente en Estados Unidos y en la Unión Europea. Al usar el formulario de contacto, crear una cuenta o escribir en las herramientas de inteligencia artificial, autorizas esa transferencia internacional en los términos del artículo 26 de la Ley 1581 de 2012.',
        'Si prefieres no enviar información fuera del país, puedes contactarnos directamente por WhatsApp o por correo electrónico en lugar de usar las herramientas del sitio.',
      ],
    },
    {
      titulo: 'Inteligencia artificial: qué pasa con lo que escribes',
      parrafos: [
        'Las respuestas del asistente y de las herramientas las genera un modelo de lenguaje a partir del texto que escribes. Ese texto sale de nuestro servidor hacia el proveedor de inteligencia artificial y no lo guardamos en nuestra base de datos: la conversación vive en tu navegador mientras dura la visita y desaparece al cerrarla.',
        'No controlamos cuánto tiempo conserva ese texto el proveedor externo ni si lo usa para mejorar sus modelos, más allá de lo que él mismo declare en sus propias políticas.',
        'Por eso te pedimos expresamente, en cada herramienta, que no escribas datos personales de tus clientes ni información confidencial de tu negocio. Están pensadas para describir un negocio, no para procesar los datos de las personas que atiende.',
      ],
    },
    {
      titulo: 'Cuánto tiempo conservamos tus datos',
      parrafos: [
        'Solicitudes de contacto y cotizaciones: mientras dure la relación comercial y, después, durante el tiempo que exijan las obligaciones legales, contables y tributarias aplicables a la empresa.',
        'Cuentas de cliente: mientras la cuenta esté activa. Si pides que la eliminemos, la borramos junto con sus datos asociados, salvo lo que debamos conservar por obligación legal.',
        'Conversaciones con las herramientas de inteligencia artificial: no las guardamos. Los contadores de uso, que no contienen tu texto, se reinician cada día.',
      ],
    },
    {
      titulo: 'Tus derechos',
      parrafos: [
        'De acuerdo con la Ley 1581 de 2012 y sus decretos reglamentarios, como titular de tus datos personales tienes derecho a conocer cuáles tenemos, actualizarlos, rectificarlos si están mal, solicitar prueba de la autorización que nos diste, ser informado del uso que les damos, presentar quejas ante la Superintendencia de Industria y Comercio, revocar tu autorización y solicitar que los eliminemos cuando no exista un deber legal de conservarlos.',
        `Para ejercer cualquiera de estos derechos basta con escribir a ${CORREO_CORPORATIVO} desde el correo con el que te contactaste, indicando qué necesitas. Respondemos las consultas en un plazo máximo de diez días hábiles y los reclamos en un plazo máximo de quince días hábiles, contados como lo establece la ley.`,
      ],
    },
    {
      titulo: 'Cómo protegemos tus datos',
      parrafos: [
        'El sitio completo viaja cifrado y las contraseñas se guardan cifradas con un algoritmo de un solo sentido. La base de datos no está expuesta a internet: solo se alcanza desde el propio servidor.',
        'No escribimos datos personales en los registros técnicos del servidor ni en las direcciones web, y no los publicamos en ninguna parte visible del sitio.',
      ],
    },
    {
      titulo: 'Vigencia y cambios',
      parrafos: [
        'Esta política aplica desde la fecha indicada al pie de esta página y puede actualizarse. Cuando cambie de forma relevante, publicaremos la versión nueva aquí con su fecha.',
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
  secciones: [
    {
      titulo: 'Sobre este sitio',
      parrafos: [
        'Este sitio web es operado por Crear Code Cesar S.A.S. (NIT 901941017-0, Valledupar, Cesar, Colombia) con fines informativos y de contacto comercial. El uso del formulario de contacto no genera, por sí solo, ninguna relación contractual entre el visitante y Crear Code Cesar S.A.S.',
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
