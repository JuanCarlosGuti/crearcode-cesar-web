import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';

/**
 * ISS-078: checklist de accesibilidad de docs/06-plan-de-pruebas.md §5,
 * aplicado a Home, una pagina de servicio, Contacto y el panel admin.
 * Requiere el stack completo corriendo (docker compose, backend, frontend
 * con proxy a /api) — mismo requisito que contacto-e2e.spec.ts, no se
 * ejecuta como parte de `npm test`.
 */

const API_BASE_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const ADMIN_USERNAME = process.env['E2E_ADMIN_USERNAME'] ?? 'admin@crearcode-cesar.local';
const ADMIN_PASSWORD = process.env['E2E_ADMIN_PASSWORD'] ?? 'cambiar-en-produccion';

const PAGINAS_PUBLICAS = [
  { nombre: 'Home', ruta: '/' },
  { nombre: 'Servicio (Desarrollo a la medida)', ruta: '/servicios/desarrollo-a-la-medida' },
  { nombre: 'Contacto', ruta: '/contacto' },
];

for (const { nombre, ruta } of PAGINAS_PUBLICAS) {
  test(`${nombre}: sin violaciones de accesibilidad (axe)`, async ({ page }) => {
    await page.goto(ruta);
    const resultados = await new AxeBuilder({ page }).analyze();
    expect(resultados.violations, JSON.stringify(resultados.violations, null, 2)).toEqual([]);
  });
}

test('Contacto: el honeypot nunca recibe foco al navegar con Tab', async ({ page }) => {
  await page.goto('/contacto');
  await page.locator('#nombre').focus();

  for (let i = 0; i < 12; i++) {
    await page.keyboard.press('Tab');
    const idEnfocado = await page.evaluate(() => document.activeElement?.id ?? null);
    expect(idEnfocado).not.toBe('sitioWeb');
  }
});

test('Contacto: el foco de teclado es visible (outline no eliminado)', async ({ page }) => {
  await page.goto('/contacto');
  await page.locator('#nombre').focus();

  const outlineStyle = await page.evaluate(() => getComputedStyle(document.activeElement as Element).outlineStyle);
  expect(outlineStyle).not.toBe('none');
});

for (const { nombre, ruta } of PAGINAS_PUBLICAS) {
  test(`${nombre}: usable con zoom de texto al 200% sin overflow horizontal`, async ({ page }) => {
    await page.goto(ruta);
    // Se simula el zoom de texto (no de pagina completa) escalando el
    // font-size raiz — el sitio usa rem tanto en tipografia como en los
    // media queries de layout, asi que esto tambien desplaza los breakpoints
    // como lo haria un zoom de texto real del navegador.
    await page.evaluate(() => {
      document.documentElement.style.fontSize = '200%';
    });

    const hayOverflowHorizontal = await page.evaluate(
      () => document.documentElement.scrollWidth > document.documentElement.clientWidth + 1,
    );
    expect(hayOverflowHorizontal).toBe(false);
  });
}

test('Contacto: la jerarquia de encabezados no salta niveles', async ({ page }) => {
  await page.goto('/contacto');
  const resultados = await new AxeBuilder({ page }).withRules(['heading-order']).analyze();
  expect(resultados.violations).toEqual([]);
});

test.describe('panel admin', () => {
  test('login: sin violaciones de accesibilidad', async ({ page }) => {
    await page.goto('/admin/login');
    const resultados = await new AxeBuilder({ page }).analyze();
    expect(resultados.violations, JSON.stringify(resultados.violations, null, 2)).toEqual([]);
  });

  test('listado y detalle: sin violaciones de accesibilidad', async ({ page, request }) => {
    const correoUnico = `e2e-a11y-${Date.now()}@crearcode-test.com`;
    await request.post(`${API_BASE_URL}/api/solicitudes`, {
      data: {
        nombre: 'Visitante E2E Accesibilidad',
        correo: correoUnico,
        telefono: '300 123 4567',
        servicioDeInteres: 'DESARROLLO_A_LA_MEDIDA',
        mensaje: 'Solicitud generada para verificar accesibilidad del panel admin.',
        aceptaConsentimiento: true,
        sitioWeb: '',
      },
    });

    await page.goto('/admin/login');
    await page.fill('#correo', ADMIN_USERNAME);
    await page.fill('#contrasena', ADMIN_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/admin');

    const resultadosListado = await new AxeBuilder({ page }).analyze();
    expect(resultadosListado.violations, JSON.stringify(resultadosListado.violations, null, 2)).toEqual([]);

    await page.getByRole('link', { name: 'Visitante E2E Accesibilidad' }).first().click();
    await page.waitForURL('**/admin/solicitudes/**');

    const resultadosDetalle = await new AxeBuilder({ page }).analyze();
    expect(resultadosDetalle.violations, JSON.stringify(resultadosDetalle.violations, null, 2)).toEqual([]);
  });
});
