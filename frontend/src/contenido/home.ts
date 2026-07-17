import { Testimonio } from './tipos';

export const HOME = {
  headline: 'Tecnología que trabaja para tu negocio, no al revés.',
  subheadline:
    'Desarrollamos software a la medida, ponemos la inteligencia artificial a trabajar por tu pyme y hacemos que cobrar y modernizar tu negocio sea simple. Sin jerga. Sin que la tecnología te quede grande.',
  mensajeWhatsapp: 'Hola, vengo del sitio web de Crear Code Cesar y quiero saber más sobre cómo pueden ayudarme con mi negocio.',
} as const;

export const TESTIMONIOS: readonly Testimonio[] = [
  {
    nombre: '[Nombre placeholder]',
    cargo: '[Cargo/Empresa placeholder]',
    cita: '[Testimonio placeholder — cita corta de un cliente sobre el resultado obtenido]',
  },
];
