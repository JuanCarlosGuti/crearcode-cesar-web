import AxeBuilder from '@axe-core/playwright';
import { APIRequestContext, expect, test } from '@playwright/test';

/**
 * ISS-099: e2e del flujo de cuentas de cliente (fase F8) con el enlace
 * REAL del correo de verificación, leído de Mailpit por su API REST.
 * Requiere el stack completo corriendo (docker compose con Postgres y
 * Mailpit, backend, frontend con proxy a /api) — mismo requisito que
 * contacto-e2e.spec.ts, no se ejecuta como parte de `npm test`.
 *
 * Para correrlo varias veces seguidas en local sin chocar con los rate
 * limits del backend (registro 5/60 min por IP), arranca el backend con
 * los umbrales relajados, ej.:
 * RATE_LIMIT_REGISTRO_MAX_INTENTOS=1000 RATE_LIMIT_LOGIN_MAX_INTENTOS=1000 ./mvnw spring-boot:run
 */

const MAILPIT_URL = process.env['E2E_MAILPIT_URL'] ?? 'http://localhost:8025';

async function leerRutaDeVerificacion(request: APIRequestContext, correo: string): Promise<string> {
  // El envío es parte de la transacción de registro, pero la entrega
  // SMTP puede tardar unos instantes: se consulta con reintentos.
  for (let intento = 0; intento < 20; intento++) {
    const busqueda = await request.get(`${MAILPIT_URL}/api/v1/search?query=to:"${correo}"`);
    if (busqueda.ok()) {
      const { messages } = (await busqueda.json()) as { messages: { ID: string }[] };
      if (messages.length > 0) {
        const mensaje = await request.get(`${MAILPIT_URL}/api/v1/message/${messages[0].ID}`);
        const { Text } = (await mensaje.json()) as { Text: string };
        const enlace = Text.match(/\/verificar-correo\?token=[A-Za-z0-9_-]+/);
        if (enlace) {
          return enlace[0];
        }
      }
    }
    await new Promise((listo) => setTimeout(listo, 500));
  }
  throw new Error(`No llegó el correo de verificación para ${correo} a Mailpit (${MAILPIT_URL})`);
}

test('un visitante se registra, verifica su correo con el enlace real e ingresa a su cuenta', async ({
  page,
  request,
}) => {
  const correo = `e2e-cuenta-${Date.now()}@crearcode-test.com`;
  const contrasena = 'contrasena-e2e-segura';

  // 1. Registro
  await page.goto('/registro');
  await page.fill('#correo', correo);
  await page.fill('#contrasena', contrasena);
  await page.fill('#confirmacion', contrasena);
  await page.check('#aceptaPolitica');
  await page.click('button[type="submit"]');
  await expect(page.getByText('¡Ya casi!')).toBeVisible();
  await expect(page.getByText(correo)).toBeVisible();

  // 2. Sin verificar todavia: el login queda bloqueado con el aviso
  await page.goto('/ingreso');
  await page.fill('#correo', correo);
  await page.fill('#contrasena', contrasena);
  await page.click('button[type="submit"]');
  await expect(page.getByText('Tu cuenta aún no está verificada.')).toBeVisible();

  // 3. Verificacion con el enlace real del correo (via API de Mailpit)
  const rutaVerificacion = await leerRutaDeVerificacion(request, correo);
  await page.goto(rutaVerificacion);
  await expect(page.getByText('¡Listo! Tu cuenta quedó verificada.')).toBeVisible();

  // 4. Ingreso -> /mi-cuenta con el correo de la sesion
  await page.goto('/ingreso');
  await page.fill('#correo', correo);
  await page.fill('#contrasena', contrasena);
  await page.click('button[type="submit"]');
  await page.waitForURL('**/mi-cuenta');
  await expect(page.getByText('Sesión iniciada como')).toBeVisible();
  await expect(page.getByText(correo)).toBeVisible();

  // 5. Accesibilidad del area autenticada (axe), aprovechando la sesion
  const resultados = await new AxeBuilder({ page }).analyze();
  expect(resultados.violations, JSON.stringify(resultados.violations, null, 2)).toEqual([]);

  // 6. Cerrar sesion -> vuelve al inicio y el header ofrece Ingresar
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await page.waitForURL(/\/$/);
  await expect(page.locator('a[href="/ingreso"]')).toBeVisible();
});
