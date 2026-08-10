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
  await expect(page.locator('.badge-muy-pronto')).toHaveCount(3);
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
