import { expect, test } from '@playwright/test';

/**
 * ISS-122: la página viva /herramientas (F10a). El cotizador funciona
 * sin backend, así que este spec solo necesita el frontend arriba.
 */

test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

test('el centro muestra las herramientas con honestidad de estados', async ({ page }) => {
  await page.goto('/herramientas');

  await expect(page.getByRole('heading', { level: 1 })).toContainText('Herramientas');
  await expect(page.locator('.herramienta-tarjeta')).toHaveCount(5);
  await expect(page.locator('.badge-muy-pronto')).toHaveCount(0);
});

test('el demo de diseno muestra el estado bloqueado a un visitante anonimo (HU-42)', async ({ page }) => {
  await page.goto('/herramientas');

  const demo = page.locator('app-demo-diseno');
  await expect(demo.getByText('Crea tu cuenta gratis para ver tu boceto')).toBeVisible();
  await expect(demo.locator('a[href="/registro"]')).toBeVisible();
  await expect(demo.locator('#demo-sector')).toHaveCount(0);
});

test('un visitante responde el quiz y recibe su radiografia en pantalla (F10c)', async ({ page }) => {
  await page.goto('/herramientas');

  for (let i = 0; i < 6; i++) {
    await page.locator('.diagnostico-opcion').first().click();
  }

  await expect(page.getByText('Tu negocio tiene un problema de tiempo')).toBeVisible({ timeout: 15000 });
  await expect(page.locator('.diagnostico-oportunidad')).toHaveCount(3);
  await expect(page.getByText('Beneficio: Dejas de contestar lo mismo todo el día.')).toBeVisible();
  await expect(page.locator('.diagnostico-cierre a[href="/contacto"]')).toBeVisible();
});

test('un visitante conversa con el chatbot de su negocio (F10b, requiere backend + stub)', async ({ page }) => {
  await page.goto('/herramientas');

  await page.fill('#simulador-nombre', 'Ferretería La 16');
  await page.fill('#simulador-rubro', 'ferretería');
  await page.fill('#simulador-mensaje', '¿Tienen tornillos?');
  await page.locator('.simulador-formulario button[type="submit"]').click();

  await expect(page.locator('.simulador-chat__titulo')).toContainText('Ferretería La 16');
  await expect(page.locator('.simulador-mensaje--usuario')).toContainText('¿Tienen tornillos?');
  // El stub de Groq responde su texto fijo para preguntas sin precio
  await expect(page.getByText('desarrollo a la medida, IA y automatización')).toBeVisible();
});

test('un visitante completa el cotizador y recibe su rango orientativo', async ({ page }) => {
  await page.goto('/herramientas');

  await page.getByRole('button', { name: 'Automatización con IA' }).click();
  await page.getByRole('button', { name: 'Varias funciones conectadas' }).click();
  await page.getByRole('button', { name: 'En 4 a 8 semanas' }).click();

  await expect(page.getByText('COP 9 – 25 millones')).toBeVisible();
  await expect(page.getByText('se cotiza a la medida')).toBeVisible();

  const whatsapp = page.locator('.cotizador-ctas a[href^="https://wa.me/"]');
  await expect(whatsapp).toBeVisible();
  expect(decodeURIComponent((await whatsapp.getAttribute('href'))!)).toContain(
    'Automatización con IA',
  );

  await page.getByRole('button', { name: 'Empezar de nuevo' }).click();
  await expect(page.getByText('¿Qué tipo de proyecto tienes en mente?')).toBeVisible();
});

test('la navegacion del header lleva al centro de herramientas', async ({ page }) => {
  await page.goto('/');
  await page.locator('header nav a[href="/herramientas"]').click();
  await expect(page).toHaveURL(/\/herramientas$/);
  await expect(page.getByRole('heading', { level: 1 })).toContainText('Herramientas');
});
