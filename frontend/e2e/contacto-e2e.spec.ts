import { expect, test } from '@playwright/test';

/**
 * ISS-052: unico e2e del flujo critico de negocio (ver docs/06-plan-de-pruebas.md
 * §2.4). Requiere el stack completo corriendo (docker compose, backend, frontend
 * con proxy a /api) — no se ejecuta como parte de `npm test`, ver README de este
 * directorio / CLAUDE.md para el comando de CI.
 *
 * La notificacion por correo (RegistrarSolicitudUseCase -> NotificadorPort) ya se
 * verifica a nivel de integracion en NotificadorEmailAdapterIT (backend); no se
 * repite aqui, para mantener este e2e minimo.
 */

const API_BASE_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const ADMIN_USERNAME = process.env['E2E_ADMIN_USERNAME'] ?? 'admin@crearcode-cesar.local';
const ADMIN_PASSWORD = process.env['E2E_ADMIN_PASSWORD'] ?? 'cambiar-en-produccion';

test('un visitante llena el formulario de contacto y la solicitud queda registrada', async ({ page, request }) => {
  const correoUnico = `e2e-${Date.now()}@crearcode-test.com`;
  const mensaje = `Solicitud de prueba e2e ${Date.now()}`;

  await page.goto('/contacto');

  await page.fill('#nombre', 'Visitante E2E');
  await page.fill('#correo', correoUnico);
  await page.fill('#telefono', '300 123 4567');
  await page.selectOption('#servicioDeInteres', 'DESARROLLO_A_LA_MEDIDA');
  await page.fill('#mensaje', mensaje);
  await page.check('#aceptaConsentimiento');
  await page.click('button[type="submit"]');

  await expect(page.getByText('¡Listo! Ya recibimos tu mensaje.')).toBeVisible();

  const loginResponse = await request.post(`${API_BASE_URL}/api/auth/login`, {
    data: { correo: ADMIN_USERNAME, contrasena: ADMIN_PASSWORD },
  });
  expect(loginResponse.ok()).toBe(true);
  const { token } = await loginResponse.json();

  const respuesta = await request.get(`${API_BASE_URL}/api/solicitudes`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(respuesta.ok()).toBe(true);

  const solicitudes = await respuesta.json();
  const registrada = solicitudes.find((s: { correo: string }) => s.correo === correoUnico);

  expect(registrada).toBeDefined();
  expect(registrada.estado).toBe('NUEVA');
  expect(registrada.mensaje).toBe(mensaje);
});
