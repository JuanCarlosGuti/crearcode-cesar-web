import { APIRequestContext, Page, expect, test } from '@playwright/test';

/**
 * La sesión del cliente no se pierde al cambiar de pestaña (ISS-240).
 *
 * Nació de una prueba del dueño: con la cuenta abierta, «Ver demo»
 * abrió el sitio en otra pestaña y ahí el demo de diseño le pidió crear
 * cuenta. Lo mismo le pasaba a un cliente que abría el enlace de un
 * correo. Requiere el stack completo (Postgres, Mailpit, backend y
 * frontend), como cuentas-e2e.spec.ts.
 */

const API_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const MAILPIT_URL = process.env['E2E_MAILPIT_URL'] ?? 'http://localhost:8025';

/** Registra un cliente y verifica su correo con el enlace real de Mailpit. */
async function clienteVerificado(request: APIRequestContext) {
  const correo = `sesion-e2e-${Date.now()}@correo-de-prueba.com`;
  const contrasena = 'contrasena-sesion-e2e';
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

async function ingresar(page: Page, correo: string, contrasena: string) {
  await page.fill('#correo', correo);
  await page.fill('#contrasena', contrasena);
  await page.click('button[type="submit"]');
}

const enlaceDeCuenta = (page: Page) => page.locator('header').getByRole('link', { name: 'Mi cuenta' });
const enlaceDeIngreso = (page: Page) => page.locator('header').getByRole('link', { name: 'Ingresar' });

test('la cuenta abierta en una pestaña también lo está en otra, y salir de una cierra las dos', async ({
  context,
  page,
  request,
}) => {
  const { correo, contrasena } = await clienteVerificado(request);
  await page.goto('/ingreso');
  await ingresar(page, correo, contrasena);
  await expect(page).toHaveURL(/\/mi-cuenta$/);

  // Como «Ver demo» o el enlace de un correo: otra pestaña.
  const otraPestana = await context.newPage();
  await otraPestana.goto('/herramientas#demo-diseno');
  await expect(enlaceDeCuenta(otraPestana)).toBeVisible();
  await expect(otraPestana.locator('#demo-sector')).toBeVisible();

  await page.getByRole('button', { name: 'Cerrar sesión' }).click();

  // Sin recargar: la otra pestaña se entera sola.
  await expect(enlaceDeIngreso(otraPestana)).toBeVisible();
  await expect(otraPestana.locator('#demo-sector')).toHaveCount(0);
});

// ISS-241: ingresar devuelve a donde se iba.
test('una página que pide sesión devuelve a ella después de ingresar', async ({ page, request }) => {
  const { correo, contrasena } = await clienteVerificado(request);

  // Como el enlace de un correo abierto sin sesión.
  await page.goto('/mi-cuenta/cotizaciones');
  await expect(page).toHaveURL(/\/ingreso\?volver=/);

  await ingresar(page, correo, contrasena);

  await expect(page).toHaveURL(/\/mi-cuenta\/cotizaciones$/);
});

test('«Ya tengo cuenta» en el demo de diseño vuelve al demo listo para usar', async ({ page, request }) => {
  const { correo, contrasena } = await clienteVerificado(request);
  await page.goto('/herramientas#demo-diseno');

  await page.getByRole('link', { name: 'Ya tengo cuenta, iniciar sesión' }).click();
  await ingresar(page, correo, contrasena);

  await expect(page).toHaveURL(/\/herramientas#demo-diseno$/);
  await expect(page.locator('#demo-sector')).toBeVisible();
});

// ISS-242: a quien ya tiene cuenta no se le invita a crearla.
test('con la cuenta abierta, el sitio deja de invitar a crear una', async ({ page, request }) => {
  const { correo, contrasena } = await clienteVerificado(request);
  const invitacionDeHerramientas = page.getByRole('heading', { name: 'Con cuenta gratis tienes más usos y acceso anticipado' });

  // Sin sesión, la invitación está: es lo que ve un visitante.
  await page.goto('/herramientas');
  await expect(invitacionDeHerramientas).toBeVisible();

  await page.goto('/ingreso');
  await ingresar(page, correo, contrasena);
  await expect(page).toHaveURL(/\/mi-cuenta$/);

  await page.goto('/herramientas');
  await expect(enlaceDeCuenta(page)).toBeVisible();
  await expect(invitacionDeHerramientas).toHaveCount(0);

  await page.goto('/');
  await expect(enlaceDeCuenta(page)).toBeVisible();
  await expect(page.locator('.seccion-cuenta')).toHaveCount(0);
});
