import { Page, expect, test } from '@playwright/test';

/**
 * ISS-228: lo que se escribe antes de que la página hidrate no se
 * pierde. Se imita un celular lento reteniendo `main.js` unos segundos,
 * que es lo que vive el visitante mientras baja el JavaScript: la página
 * prerenderizada ya está en pantalla y se escribe en ella antes de que
 * Angular tome el control. Antes del arreglo, en producción y con un
 * celular lento simulado, se borraba todo lo escrito durante el primer
 * segundo y medio.
 *
 * Cada prueba comprueba además que de verdad escribió antes de hidratar
 * (el enlace de registro del header solo existe después): si un día la
 * página hidratara antes, la prueba fallaría en vez de pasar sin
 * probar nada.
 */

test.describe.configure({ timeout: 120_000 });

async function abrirEnCelularLento(page: Page, ruta: string, primerCampo: string) {
  await page.route('**/main*.js', async (peticion) => {
    await new Promise((listo) => setTimeout(listo, 2500));
    await peticion.continue();
  });
  await page.goto(ruta, { waitUntil: 'commit' });
  await page.locator(primerCampo).waitFor();
}

async function confirmarQueAunNoHidrata(page: Page) {
  expect(await page.locator('header a[href="/registro"]').count(), 'la página hidrató antes de escribir').toBe(0);
}

async function esperarHidratacion(page: Page) {
  await expect(page.locator('header a[href="/registro"]')).toBeAttached({ timeout: 90_000 });
}

test('contacto: lo escrito al instante llega completo al envío', async ({ page }) => {
  await abrirEnCelularLento(page, '/contacto', '#nombre');
  await page.fill('#nombre', 'Visitante Apurado');
  await page.fill('#correo', `apurado-${Date.now()}@crearcode-test.com`);
  await page.fill('#telefono', '300 123 4567');
  await page.fill('#mensaje', 'Escribí esto antes de que la página terminara de cargar.');
  await confirmarQueAunNoHidrata(page);

  await esperarHidratacion(page);
  await expect(page.locator('#nombre')).toHaveValue('Visitante Apurado');
  await page.selectOption('#servicioDeInteres', 'DESARROLLO_A_LA_MEDIDA');
  await page.check('#aceptaConsentimiento');
  await page.click('button[type="submit"]');

  await expect(page.getByText('¡Listo! Ya recibimos tu mensaje.')).toBeVisible({ timeout: 30_000 });
});

test('ingreso: el correo y la contraseña escritos al instante llegan al servidor', async ({ page }) => {
  await abrirEnCelularLento(page, '/ingreso', '#correo');
  await page.fill('#correo', `nadie-${Date.now()}@crearcode-test.com`);
  await page.fill('#contrasena', 'una-contrasena-cualquiera');
  await confirmarQueAunNoHidrata(page);

  await esperarHidratacion(page);
  await page.click('button[type="submit"]');

  // Si se hubieran perdido, saldría «campo obligatorio» y no llegaría al servidor.
  await expect(page.getByText('Correo o contraseña incorrectos.')).toBeVisible({ timeout: 30_000 });
});

test('recuperar contraseña: el correo escrito al instante se envía', async ({ page }) => {
  await abrirEnCelularLento(page, '/recuperar-contrasena', '#correo');
  await page.fill('#correo', `olvidadizo-${Date.now()}@crearcode-test.com`);
  await confirmarQueAunNoHidrata(page);

  await esperarHidratacion(page);
  await page.click('button[type="submit"]');

  await expect(page.getByText('Si tu correo está registrado, te llegará un enlace')).toBeVisible({ timeout: 30_000 });
});

test('registro: el correo escrito al instante sigue ahí y es válido', async ({ page }) => {
  await abrirEnCelularLento(page, '/registro', '#correo');
  await page.fill('#correo', `nuevo-${Date.now()}@crearcode-test.com`);
  await confirmarQueAunNoHidrata(page);

  await esperarHidratacion(page);
  await page.click('button[type="submit"]');

  // Faltan la contraseña y la política, pero el correo no reclama nada.
  await expect(page.locator('#contrasena-error')).toBeVisible();
  await expect(page.locator('#correo-error')).toHaveCount(0);
});

test('tarjeta del demo en la Home: lo escrito al instante queda para el boceto', async ({ page }) => {
  await abrirEnCelularLento(page, '/', '#hero-sector');
  await page.fill('#hero-sector', 'Ferretería');
  await confirmarQueAunNoHidrata(page);

  await esperarHidratacion(page);
  await page.getByRole('button', { name: 'Ver mi boceto con IA' }).click();
  await page.waitForURL('**/registro');

  const guardado = await page.evaluate(() => localStorage.getItem('crearcode-boceto-pendiente'));
  expect(JSON.parse(guardado ?? '{}').sector).toBe('Ferretería');
});

test('simulador de chatbot: el nombre del negocio escrito al instante titula el chat', async ({ page }) => {
  await abrirEnCelularLento(page, '/herramientas', '#simulador-nombre');
  await page.fill('#simulador-nombre', 'Panadería La Esquina');
  await confirmarQueAunNoHidrata(page);

  await esperarHidratacion(page);
  await expect(page.locator('.simulador-chat__titulo')).toContainText('Panadería La Esquina');
});
