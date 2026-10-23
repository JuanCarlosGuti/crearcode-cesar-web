import { rutaDeVuelta } from './ruta-de-vuelta';

describe('rutaDeVuelta (ISS-241)', () => {
  it('acepta una ruta del sitio, con su consulta y su ancla', () => {
    expect(rutaDeVuelta('/mi-cuenta/proyectos/7', 'CLIENTE')).toBe('/mi-cuenta/proyectos/7');
    expect(rutaDeVuelta('/herramientas#demo-diseno', 'CLIENTE')).toBe('/herramientas#demo-diseno');
    expect(rutaDeVuelta('/mi-cuenta/cotizaciones?id=3', 'CLIENTE')).toBe('/mi-cuenta/cotizaciones?id=3');
  });

  // Un enlace armado para que, tras ingresar, el visitante caiga en una
  // copia del sitio que le pida otra vez la contraseña.
  it('rechaza todo lo que pueda llevar a otro sitio', () => {
    for (const valor of [
      'https://otro-sitio.com',
      '//otro-sitio.com',
      '/\\otro-sitio.com',
      '/\t/otro-sitio.com',
      'javascript:alert(1)',
      'mi-cuenta',
      '',
      null,
      undefined,
    ]) {
      expect(rutaDeVuelta(valor, 'CLIENTE'), String(valor)).toBeNull();
    }
  });

  it('el cliente no vuelve al panel ni el admin sale de él', () => {
    expect(rutaDeVuelta('/admin/proyectos/3', 'CLIENTE')).toBeNull();
    expect(rutaDeVuelta('/admin', 'CLIENTE')).toBeNull();
    expect(rutaDeVuelta('/admin/proyectos/3', 'ADMIN')).toBe('/admin/proyectos/3');
    expect(rutaDeVuelta('/mi-cuenta', 'ADMIN')).toBeNull();
  });

  it('una ruta que solo empieza parecido a /admin es del sitio público', () => {
    expect(rutaDeVuelta('/administracion', 'CLIENTE')).toBe('/administracion');
  });

  it('no vuelve a las páginas de ingreso o registro: sería dar vueltas', () => {
    for (const valor of ['/ingreso', '/ingreso?volver=/mi-cuenta', '/registro', '/recuperar-contrasena']) {
      expect(rutaDeVuelta(valor, 'CLIENTE'), valor).toBeNull();
    }
    expect(rutaDeVuelta('/admin/login', 'ADMIN')).toBeNull();
  });
});
