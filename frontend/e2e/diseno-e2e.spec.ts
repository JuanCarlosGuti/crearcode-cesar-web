import { APIRequestContext, Page, expect, test } from '@playwright/test';

/**
 * Guardias de diseño (ISS-243 a ISS-246). Nacieron de un recorrido del
 * sitio a 375 y 1280 px antes de una auditoría de diseño: cosas que
 * axe no ve porque no son de accesibilidad estricta, o que ninguna
 * prueba medía. Cada una mide lo que se vio mal, para que no vuelva.
 */

const API_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const MAILPIT_URL = process.env['E2E_MAILPIT_URL'] ?? 'http://localhost:8025';

/** Registra un cliente y verifica su correo con el enlace real de Mailpit. */
async function clienteVerificado(request: APIRequestContext) {
  const correo = `diseno-e2e-${Date.now()}@correo-de-prueba.com`;
  const contrasena = 'contrasena-diseno-e2e';
  await request.post(`${API_URL}/api/auth/registro`, { data: { correo, contrasena } });

  let token = '';
  for (let intento = 0; intento < 20 && !token; intento++) {
    const busqueda = await request.get(`${MAILPIT_URL}/api/v1/search?query=to:"${correo}"`);
    if (busqueda.ok()) {
      const { messages } = (await busqueda.json()) as { messages: { ID: string }[] };
      if (messages.length > 0) {
        const mensaje = await request.get(`${MAILPIT_URL}/api/v1/message/${messages[0].ID}`);
        const { Text } = (await mensaje.json()) as { Text: string };
        token = Text.match(/token=([A-Za-z0-9_-]+)/)?.[1] ?? '';
      }
    }
    if (!token) {
      await new Promise((listo) => setTimeout(listo, 500));
    }
  }
  await request.post(`${API_URL}/api/auth/verificacion`, { data: { token } });
  return { correo, contrasena };
}

const PUBLICAS = [
  '/',
  '/servicios/desarrollo-a-la-medida',
  '/herramientas',
  '/casos',
  '/casos/uparya',
  '/sobre-nosotros',
  '/blog',
  '/blog/senales-automatizar-atencion-whatsapp',
  '/contacto',
  '/legales/politica-de-datos',
  '/registro',
  '/ingreso',
  '/recuperar-contrasena',
];

const CELULAR = { width: 375, height: 812 };
const ESCRITORIO = { width: 1280, height: 800 };

test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

/** Los controles más chicos que 24 px (WCAG 2.2 AA, 2.5.8), sin contar los enlaces dentro de un texto. */
async function controlesChicos(page: Page): Promise<string[]> {
  return page.evaluate(() => {
    const chicos: string[] = [];
    for (const el of document.querySelectorAll<HTMLElement>('a[href], button, input, select, textarea, summary')) {
      const r = el.getBoundingClientRect();
      const estilo = getComputedStyle(el);
      if (r.width === 0 || r.height === 0 || estilo.visibility === 'hidden') continue;
      // El honeypot está fuera de la vista a propósito.
      if (el.closest('.campo-honeypot')) continue;
      // Un enlace dentro de una frase está exento (2.5.8, excepción «en línea»).
      if (el.tagName === 'A' && estilo.display === 'inline' && el.closest('p, li, label, td')) continue;
      if (Math.min(r.width, r.height) < 24) {
        chicos.push(`${el.tagName.toLowerCase()} «${(el.innerText || el.id || '').trim().slice(0, 40)}» ${Math.round(r.width)}x${Math.round(r.height)}`);
      }
    }
    return chicos;
  });
}

for (const [nombre, viewport] of [['celular', CELULAR], ['escritorio', ESCRITORIO]] as const) {
  test(`ningún control del sitio público es más chico que 24 px (${nombre})`, async ({ page }) => {
    await page.setViewportSize(viewport);
    const encontrados: string[] = [];
    for (const ruta of PUBLICAS) {
      await page.goto(ruta);
      await page.waitForLoadState('networkidle');
      for (const control of await controlesChicos(page)) {
        encontrados.push(`${ruta}: ${control}`);
      }
    }
    expect(encontrados).toEqual([]);
  });

  // /herramientas usaba una clase .seccion que no existía: el título
  // quedaba pegado al encabezado (ISS-243).
  test(`el título de cada página respira bajo el encabezado (${nombre})`, async ({ page }) => {
    await page.setViewportSize(viewport);
    const pegados: string[] = [];
    for (const ruta of PUBLICAS.filter((r) => r !== '/')) {
      await page.goto(ruta);
      const distancia = await page.evaluate(() => {
        const encabezado = document.querySelector('header')!.getBoundingClientRect();
        const titulo = document.querySelector('main h1, h1')!.getBoundingClientRect();
        return Math.round(titulo.top - encabezado.bottom);
      });
      if (distancia < 32) pegados.push(`${ruta}: ${distancia}px`);
    }
    expect(pegados).toEqual([]);
  });
}

// El ejemplo de un campo es lo que le dice al visitante qué escribir:
// cortado a la mitad («sin satura») no ayuda (ISS-244).
test('ningún texto de ejemplo se corta en un campo de una línea (celular)', async ({ page }) => {
  await page.setViewportSize(CELULAR);
  const cortados: string[] = [];
  for (const ruta of ['/', '/herramientas', '/contacto', '/registro', '/ingreso']) {
    await page.goto(ruta);
    await page.waitForLoadState('networkidle');
    const enEstaPagina = await page.evaluate(() => {
      const lienzo = document.createElement('canvas').getContext('2d')!;
      return [...document.querySelectorAll<HTMLInputElement>('input[placeholder]')]
        .filter((campo) => campo.getBoundingClientRect().width > 0)
        .filter((campo) => {
          const estilo = getComputedStyle(campo);
          lienzo.font = `${estilo.fontWeight} ${estilo.fontSize} ${estilo.fontFamily}`;
          const disponible = campo.clientWidth - parseFloat(estilo.paddingLeft) - parseFloat(estilo.paddingRight);
          return lienzo.measureText(campo.placeholder).width > disponible;
        })
        .map((campo) => `«${campo.placeholder}»`);
    });
    cortados.push(...enEstaPagina.map((c) => `${ruta}: ${c}`));
  }
  expect(cortados).toEqual([]);
});

// La casilla quedaba sola en una línea y su texto en la siguiente.
test('en el registro, la casilla de la política va al lado de su texto (celular)', async ({ page }) => {
  await page.setViewportSize(CELULAR);
  await page.goto('/registro');

  const casilla = (await page.locator('#aceptaPolitica').boundingBox())!;
  const texto = (await page.locator('label[for="aceptaPolitica"]').boundingBox())!;

  expect(texto.x).toBeGreaterThan(casilla.x + casilla.width);
  expect(texto.y).toBeLessThan(casilla.y + casilla.height);
});

// Los botones flotantes tapaban el borde de «Agenda tu consulta gratuita».
test('en la Home, los botones flotantes no tapan los botones del hero (celular)', async ({ page }) => {
  await page.setViewportSize(CELULAR);
  await page.goto('/');
  await page.waitForLoadState('networkidle');

  const solapes = await page.evaluate(() => {
    const flotantes = [...document.querySelectorAll<HTMLElement>('.whatsapp-flotante, .chat-burbuja')]
      .map((el) => el.getBoundingClientRect())
      .filter((r) => r.width > 0);
    const botones = [...document.querySelectorAll<HTMLElement>('.hero a.boton, .hero button.boton')].map((el) => ({
      texto: el.innerText.trim(),
      r: el.getBoundingClientRect(),
    }));
    const cruzan = (a: DOMRect, b: DOMRect) => a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
    return {
      flotantes: flotantes.length,
      tapados: botones.filter((b) => b.r.bottom <= window.innerHeight && flotantes.some((f) => cruzan(f, b.r))).map((b) => b.texto),
    };
  });

  expect(solapes.flotantes).toBeGreaterThan(0);
  expect(solapes.tapados).toEqual([]);
});

// Los flotantes ocupan la esquina de abajo a la derecha: un botón de
// enviar a todo el ancho pasa por debajo de ellos (ISS-245).
test('los botones de enviar de los formularios no pasan por debajo de los flotantes (celular)', async ({ page }) => {
  await page.setViewportSize(CELULAR);
  const tapados: string[] = [];
  for (const ruta of ['/registro', '/ingreso', '/recuperar-contrasena', '/contacto']) {
    await page.goto(ruta);
    await page.waitForLoadState('networkidle');
    const resultado = await page.evaluate(() => {
      const flotantes = [...document.querySelectorAll<HTMLElement>('.whatsapp-flotante, .chat-burbuja')]
        .map((el) => el.getBoundingClientRect())
        .filter((r) => r.width > 0);
      const bordeDeLosFlotantes = Math.min(...flotantes.map((r) => r.left));
      const enviar = document.querySelector<HTMLElement>('main form button[type="submit"], form button[type="submit"]')!;
      return { flotantes: flotantes.length, derecha: enviar.getBoundingClientRect().right, bordeDeLosFlotantes };
    });
    expect(resultado.flotantes, ruta).toBeGreaterThan(0);
    if (resultado.derecha > resultado.bordeDeLosFlotantes) tapados.push(ruta);
  }
  expect(tapados).toEqual([]);
});

test('en Mi cuenta, los botones de las secciones no quedan pegados (celular)', async ({ page, request }) => {
  const { correo, contrasena } = await clienteVerificado(request);
  await page.setViewportSize(CELULAR);
  await page.goto('/ingreso');
  await page.fill('#correo', correo);
  await page.fill('#contrasena', contrasena);
  await page.click('button[type="submit"]');
  await expect(page).toHaveURL(/\/mi-cuenta$/);

  const botones = page.locator('.pagina-cuenta__secciones .boton');
  await expect(botones).toHaveCount(2);
  const primero = (await botones.nth(0).boundingBox())!;
  const segundo = (await botones.nth(1).boundingBox())!;
  const separacion = Math.max(segundo.y - (primero.y + primero.height), segundo.x - (primero.x + primero.width));
  expect(separacion).toBeGreaterThanOrEqual(8);
});

// La tabla de solicitudes empujaba toda la página 251 px hacia el lado
// en el celular: ahora se desplaza dentro de su caja, como las de
// cotizaciones y proyectos (ISS-246).
test('el panel admin no se sale de la pantalla y se puede tocar en el celular', async ({ page, request }) => {
  const respuesta = await request.post(`${API_URL}/api/auth/login`, {
    data: { correo: 'admin@crearcode-cesar.local', contrasena: 'cambiar-en-produccion' },
  });
  const sesion = (await respuesta.json()) as { token: string; rol: string; correo: string };
  await page.addInitScript((datos) => {
    sessionStorage.setItem('crearcode-sesion', JSON.stringify(datos));
  }, { token: sesion.token, rol: sesion.rol, correo: sesion.correo });
  await page.setViewportSize(CELULAR);

  for (const ruta of ['/admin', '/admin/cotizaciones', '/admin/proyectos']) {
    await page.goto(ruta);
    await page.waitForLoadState('networkidle');
    const desborde = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    expect(desborde, ruta).toBe(0);
    expect(await controlesChicos(page), ruta).toEqual([]);
  }
});
