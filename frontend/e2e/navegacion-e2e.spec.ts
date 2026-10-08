import { expect, test } from '@playwright/test';

/**
 * ISS-227: cambiar de página empieza desde arriba, y volver atrás deja
 * al visitante donde iba. Sin esto el router mantenía el scroll de la
 * página anterior: un enlace del footer llevaba al fondo de la página
 * siguiente, y el aviso de /registro que pide ISS-226 quedaba fuera de
 * la pantalla en el celular. Solo se ve navegando de verdad.
 */

test.use({ viewport: { width: 375, height: 812 } });

test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

test('al cambiar de pagina se empieza desde arriba, y al volver se recupera donde iba (ISS-227)', async ({
  page,
}) => {
  await page.goto('/');
  // El enlace de registro del header solo existe tras hidratar.
  await expect(page.locator('header a[href="/registro"]')).toBeAttached();

  const enlace = page.locator('footer a[href="/sobre-nosotros"]');
  await enlace.scrollIntoViewIfNeeded();
  const dondeIba = await page.evaluate(() => window.scrollY);
  expect(dondeIba).toBeGreaterThan(1000);

  await enlace.click();
  await page.waitForURL('**/sobre-nosotros');
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBe(0);

  await page.goBack();
  await page.waitForURL(/\/$/);
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBeGreaterThan(1000);
});

test('pedir el boceto desde la tarjeta deja a la vista el aviso de registro (ISS-226 + ISS-227)', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('header a[href="/registro"]')).toBeAttached();

  await page.fill('#hero-sector', 'Ferretería');
  await page.getByRole('button', { name: 'Ver mi boceto con IA' }).click();

  await page.waitForURL('**/registro');
  await expect(page.locator('.pagina-registro__aviso-boceto')).toBeInViewport();
});
