import { RolDeSesion } from './sesion';

/**
 * El parámetro con el que una página que pide sesión le dice al ingreso
 * a dónde volver (ISS-241). Lo leen `/ingreso` y `/admin/login` como
 * input del componente (`withComponentInputBinding`).
 */
export const PARAMETRO_VOLVER = 'volver';

// Volver a ellas después de ingresar sería dar vueltas.
const PAGINAS_DE_ACCESO = [
  '/ingreso',
  '/registro',
  '/recuperar-contrasena',
  '/restablecer-contrasena',
  '/verificar-correo',
  '/admin/login',
];

/**
 * La ruta a la que se vuelve después de ingresar, o null si no sirve y
 * toca el destino de siempre.
 *
 * <p>
 * Solo rutas del propio sitio: empieza por una sola «/», sin barras
 * invertidas ni espacios, porque «//otro.com», «/\otro.com» o «/ /otro.com»
 * el navegador los lee como otro dominio. El router de Angular no
 * sale del sitio, pero la regla no depende de eso: un enlace armado
 * para que, tras ingresar, el visitante caiga en una copia que le pida
 * otra vez la contraseña es justo lo que hay que evitar. Y cada rol a
 * lo suyo: el admin vuelve dentro de `/admin`, el cliente fuera.
 */
export function rutaDeVuelta(valor: string | null | undefined, rol: RolDeSesion): string | null {
  if (!valor || !valor.startsWith('/') || valor.startsWith('//') || /[\s\\]/.test(valor)) {
    return null;
  }
  const ruta = valor.split(/[?#]/)[0];
  if (PAGINAS_DE_ACCESO.includes(ruta)) {
    return null;
  }
  const esDelPanel = ruta === '/admin' || ruta.startsWith('/admin/');
  return esDelPanel === (rol === 'ADMIN') ? valor : null;
}
