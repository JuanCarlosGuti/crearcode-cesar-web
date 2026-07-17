import { ValorEmpresa } from './tipos';

export const SOBRE_NOSOTROS = {
  historia:
    'Crear Code Cesar S.A.S. nació en Valledupar con una idea simple: las pymes colombianas merecen la misma calidad de tecnología que las grandes empresas, explicada en su idioma y a su medida. Trabajamos desde el Caribe colombiano para todo el país — presencial cuando el proyecto lo pide, remoto cuando no — con costos más competitivos que las agencias de las grandes ciudades, sin sacrificar la calidad del trabajo.',
  fundador: {
    nombre: 'Juan Carlos Gutiérrez',
    perfil:
      'Juan Carlos Gutiérrez es arquitecto de software senior y administrador de empresas de la Universidad Nacional, con 7 años de experiencia como instructor SENA. Esa combinación no es casualidad: significa que en Crear Code Cesar hablamos el idioma del negocio, no solo el de la técnica. Entendemos tanto el código como los números, las metas y las preocupaciones reales de quien dirige una empresa.',
    linkedinUrl: 'https://www.linkedin.com/in/juan-carlos-gutierrez-huerfano369582/',
  },
  formaDeTrabajar:
    'No empezamos a escribir código el primer día. Primero documentamos qué se va a construir y por qué, para que tú sepas exactamente qué esperar. Luego definimos cómo lo vamos a probar, antes de construirlo, para asegurarnos de que funcione de verdad y no solo "a simple vista". Recién ahí construimos. Este mismo sitio web se hizo siguiendo ese proceso — es la mejor prueba de cómo trabajamos.',
} as const;

export const VALORES: readonly ValorEmpresa[] = [
  {
    titulo: 'Claridad',
    descripcion: 'Te explicamos todo sin jerga, para que decidas con información real.',
  },
  {
    titulo: 'Compromiso con el resultado',
    descripcion: 'No medimos el éxito en líneas de código, sino en si tu negocio mejoró.',
  },
  {
    titulo: 'Educación como parte del servicio',
    descripcion: 'Te acompañamos y te enseñamos, no solo entregamos y desaparecemos.',
  },
  {
    titulo: 'Cercanía con raíces caribeñas y alcance nacional',
    descripcion: 'Trabajamos desde Valledupar para toda Colombia, con la misma calidad en cualquier ciudad.',
  },
];
