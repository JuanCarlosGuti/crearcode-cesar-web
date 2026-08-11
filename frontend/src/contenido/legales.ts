// Correo corporativo del dominio propio (11 ago 2026). Reemplaza al
// crearcodecesar@gmail.com temporal que se usó hasta comprar el dominio.
export const CORREO_CORPORATIVO = 'admin@crearcodecesar.com';
export const WHATSAPP_NUMERO = '323 988 5883';
export const WHATSAPP_NUMERO_INTERNACIONAL = '573239885883';

export const POLITICA_DE_DATOS = {
  titulo: 'Política de tratamiento de datos personales',
  metaDescripcion:
    'Política de tratamiento de datos personales de Crear Code Cesar S.A.S., conforme a la Ley 1581 de 2012.',
  version: 'v1',
  parrafos: [
    'Responsable del tratamiento: Crear Code Cesar S.A.S., con domicilio en Valledupar, Cesar, Colombia.',
    'Datos que recolectamos: nombre, empresa (si aplica), correo electrónico, teléfono y la información que nos compartas en el formulario de contacto de este sitio.',
    'Finalidad: usamos tus datos únicamente para responder tu solicitud, contactarte respecto a los servicios de Crear Code Cesar S.A.S. y, si lo autorizas, mantenerte informado sobre contenido y novedades relevantes. No vendemos ni compartimos tus datos con terceros para fines distintos a los aquí descritos.',
    `Tus derechos: de acuerdo con la Ley 1581 de 2012 y sus decretos reglamentarios, tienes derecho a conocer, actualizar, rectificar y solicitar la eliminación de tus datos personales, así como a revocar tu autorización en cualquier momento, escribiendo a ${CORREO_CORPORATIVO}.`,
    'Cómo protegemos tus datos: aplicamos medidas técnicas y organizativas razonables para proteger tu información contra acceso no autorizado, y no publicamos tus datos personales en ningún lugar visible del sitio ni de sus registros técnicos.',
    'Vigencia: esta política aplica desde su publicación y puede actualizarse; la fecha de la versión vigente se indica al pie de esta página.',
  ],
  notaBorrador:
    'Este texto es un borrador base para que Crear Code Cesar S.A.S. lo revise y, de ser necesario, lo ajuste con asesoría legal antes de publicarlo.',
} as const;

export const TERMINOS_DE_USO = {
  titulo: 'Términos de uso',
  metaDescripcion: 'Términos de uso del sitio web de Crear Code Cesar S.A.S.',
  parrafos: [
    'Sobre este sitio: este sitio web es operado por Crear Code Cesar S.A.S. (Valledupar, Cesar, Colombia) con fines informativos y de contacto comercial. El uso del formulario de contacto no genera, por sí solo, ninguna relación contractual entre el visitante y Crear Code Cesar S.A.S.',
    'Uso permitido: el contenido de este sitio (textos, casos, artículos) puede consultarse libremente; su reproducción total o parcial con fines comerciales requiere autorización previa de Crear Code Cesar S.A.S.',
    'Disponibilidad: se hace un esfuerzo razonable por mantener el sitio disponible y actualizado, sin garantizar disponibilidad ininterrumpida.',
    `Contacto: para cualquier consulta sobre estos términos, escribe a ${CORREO_CORPORATIVO} o por WhatsApp al ${WHATSAPP_NUMERO}.`,
  ],
  notaBorrador: 'Este texto es un borrador base para revisión y ajuste con asesoría legal antes de publicarlo.',
} as const;
