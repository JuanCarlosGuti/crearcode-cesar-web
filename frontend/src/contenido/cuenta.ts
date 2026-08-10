/**
 * Textos de las páginas de cuentas de cliente (fase F8, HU-30 a
 * HU-33). Fuente: docs/08-contenido.md §Cuentas de cliente.
 */
export const CUENTA = {
  registro: {
    titulo: 'Crea tu cuenta',
    intro: 'Regístrate para acceder a los servicios para clientes de Crear Code Cesar. Es gratis y toma un minuto.',
    labelCorreo: 'Correo electrónico',
    labelContrasena: 'Contraseña (mínimo 10 caracteres)',
    labelConfirmacion: 'Confirma tu contraseña',
    labelPolitica: 'He leído y acepto la',
    enlacePolitica: 'Política de tratamiento de datos',
    boton: 'Crear cuenta',
    botonEnviando: 'Creando cuenta…',
    exitoAntesDelCorreo: '¡Ya casi! Te enviamos un correo a',
    exitoDespuesDelCorreo: 'con un enlace para verificar tu cuenta. Revisa también la carpeta de spam.',
    errorYaRegistrada: 'Ya existe una cuenta con este correo. ¿Quieres',
    enlaceIniciarSesion: 'iniciar sesión',
    errorYaRegistradaO: 'o',
    enlaceRecuperar: 'recuperar tu contraseña',
    errorYaRegistradaCierre: '?',
  },
  ingreso: {
    titulo: 'Inicia sesión',
    intro: 'Entra a tu cuenta de Crear Code Cesar.',
    labelCorreo: 'Correo electrónico',
    labelContrasena: 'Contraseña',
    boton: 'Iniciar sesión',
    botonEnviando: 'Ingresando…',
    enlaceRecuperar: '¿Olvidaste tu contraseña?',
    enlaceRegistro: '¿No tienes cuenta? Regístrate',
    errorCredenciales: 'Correo o contraseña incorrectos.',
    errorSinVerificar: 'Tu cuenta aún no está verificada. Revisa el correo que te enviamos o',
    enlaceReenviar: 'reenvíalo aquí',
  },
  verificacion: {
    verificando: 'Estamos verificando tu correo…',
    exito: '¡Listo! Tu cuenta quedó verificada. Ya puedes',
    enlaceIniciarSesion: 'iniciar sesión',
    error: 'Este enlace es inválido o ya venció. Escribe tu correo y te enviamos uno nuevo.',
    labelCorreo: 'Correo electrónico',
    botonReenviar: 'Reenviar verificación',
    reenvioConfirmado:
      'Si tu correo está registrado y pendiente de verificar, te llegará un enlace nuevo.',
  },
  recuperacion: {
    titulo: 'Recupera tu contraseña',
    intro:
      'Escribe el correo con el que te registraste y te enviamos un enlace para crear una contraseña nueva.',
    labelCorreo: 'Correo electrónico',
    boton: 'Enviar enlace',
    botonEnviando: 'Enviando…',
    exito: 'Si tu correo está registrado, te llegará un enlace en unos minutos. Revisa también la carpeta de spam.',
  },
  restablecimiento: {
    titulo: 'Crea tu nueva contraseña',
    labelContrasena: 'Nueva contraseña (mínimo 10 caracteres)',
    labelConfirmacion: 'Confírmala',
    boton: 'Guardar contraseña nueva',
    botonEnviando: 'Guardando…',
    exito: 'Tu contraseña quedó actualizada. Ya puedes',
    enlaceIniciarSesion: 'iniciar sesión',
    exitoDespuesDelEnlace: 'con ella.',
    errorEnlace: 'Este enlace es inválido o ya venció. Escribe tu correo y te enviamos uno nuevo.',
  },
  miCuenta: {
    titulo: 'Mi cuenta',
    sesionIniciadaComo: 'Sesión iniciada como',
    botonCerrarSesion: 'Cerrar sesión',
    notaContrasenaAntes: '¿Quieres cambiar tu contraseña? Usa',
    enlaceRecuperar: 'recuperar contraseña',
    notaContrasenaDespues: '— te llegará un enlace al correo.',
  },
  header: {
    ingresar: 'Ingresar',
    miCuenta: 'Mi cuenta',
  },
} as const;

/**
 * Sección "Tu cuenta te da más" (Home y /registro — HU-34, fase F8.5).
 * Regla de honestidad (docs/08): lo que aún no existe lleva SIEMPRE la
 * etiqueta "Muy pronto". Con F10 completa (10 ago 2026) las
 * herramientas ya están VIVAS — cero badges (ISS-131).
 */
export const BENEFICIOS_CUENTA = {
  titulo: 'Tu cuenta te da más',
  intro:
    'Crear tu cuenta es gratis, toma un minuto y no es obligatoria para nada del sitio. Pero con ella tienes más de cada herramienta:',
  etiquetaMuyPronto: 'Muy pronto',
  tarjetas: [
    {
      titulo: 'Más usos del asistente y las herramientas',
      muyPronto: false,
      descripcion:
        'El asistente, el chatbot de tu negocio y el diagnóstico digital tienen cupo diario gratis para todos — con cuenta, el tuyo se multiplica.',
    },
    {
      titulo: 'Demo de diseño con IA',
      muyPronto: false,
      descripcion:
        'Describe tu negocio y mira un primer boceto de tu solución generado con inteligencia artificial, con funcionalidades sugeridas. Exclusivo para cuentas: 3 bocetos al día.',
    },
    {
      titulo: 'Acceso anticipado',
      muyPronto: false,
      descripcion:
        'Cada herramienta nueva del sitio la estrenan primero las personas registradas — sin costo y sin compromiso.',
    },
  ],
  ctaPrincipal: 'Crear mi cuenta gratis',
  ctaSecundario: 'Ya tengo cuenta',
} as const;

/**
 * Tabla "visitante vs. con cuenta" (Home, F10e — prototipo aprobado).
 * Los números espejan los defaults de las variables de límites del
 * backend (ASISTENTE_*, SIMULADOR_*, DIAGNOSTICO_*, DEMO_*).
 */
export const TABLA_CUENTA = {
  columnaConcepto: 'Cada día tienes…',
  columnaVisitante: 'Visitante',
  columnaCuenta: 'Con cuenta',
  filas: [
    { concepto: 'Bocetos del demo de diseño', visitante: '—', conCuenta: '3' },
    { concepto: 'Diagnósticos digitales', visitante: '2', conCuenta: '10' },
    { concepto: 'Mensajes al chatbot de tu negocio', visitante: '10', conCuenta: '50' },
    { concepto: 'Consultas al asistente', visitante: '10', conCuenta: '50' },
    { concepto: 'Herramientas nuevas', visitante: 'Cuando salen', conCuenta: 'Acceso anticipado' },
  ],
} as const;

export const MENSAJE_ERROR_CORREO_CUENTA = 'Escribe un correo válido, ej. nombre@empresa.com.';
export const MENSAJE_ERROR_CONTRASENA_CORTA = 'La contraseña debe tener al menos 10 caracteres.';
export const MENSAJE_ERROR_CONTRASENAS_NO_COINCIDEN = 'Las contraseñas no coinciden.';
export const MENSAJE_ERROR_POLITICA = 'Debes aceptar la política de datos para crear tu cuenta.';
export const MENSAJE_ERROR_CONTRASENA_REQUERIDA = 'Escribe tu contraseña.';
export const MENSAJE_ERROR_CORREO_REQUERIDO = 'Escribe tu correo.';
