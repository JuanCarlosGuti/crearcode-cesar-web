import AxeBuilder from '@axe-core/playwright';
import { APIRequestContext, Page, expect, test } from '@playwright/test';

/**
 * ISS-154: el ciclo comercial completo de F11 — el equipo cotiza desde
 * un lead, lo envía, y el cliente lo acepta desde su cuenta, con el lead
 * quedando CONVERTIDO.
 *
 * Requiere el stack completo (docker compose + backend + frontend). El
 * correo de la cotización cae en Mailpit, como el resto de correos en
 * local.
 */

const API_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const MAILPIT_URL = process.env['E2E_MAILPIT_URL'] ?? 'http://localhost:8025';
const ADMIN_USUARIO = 'admin@crearcode-cesar.local';
const ADMIN_CONTRASENA = 'cambiar-en-produccion';
const CONTRASENA_CLIENTE = 'contrasena-de-prueba-larga';

test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

/**
 * Cacheado a nivel de módulo: el login tiene su propio rate limit por IP
 * (5/15 min) y toda la suite corre desde la misma, así que cada spec que
 * se loguea de más le quita cupo a los demás.
 */
let tokenAdminCacheado: string | null = null;

async function tokenAdmin(request: APIRequestContext): Promise<string> {
  if (!tokenAdminCacheado) {
    const respuesta = await request.post(`${API_URL}/api/auth/login`, {
      data: { correo: ADMIN_USUARIO, contrasena: ADMIN_CONTRASENA },
    });
    tokenAdminCacheado = (await respuesta.json()).token;
  }
  return tokenAdminCacheado;
}

/** Cliente registrado y verificado con el enlace real del correo. */
async function clienteVerificado(request: APIRequestContext, correo: string) {
  await request.post(`${API_URL}/api/auth/registro`, {
    data: { correo, contrasena: CONTRASENA_CLIENTE },
  });

  let token = '';
  for (let intento = 0; intento < 20 && !token; intento++) {
    const busqueda = await request.get(`${MAILPIT_URL}/api/v1/search?query=to:"${correo}"`);
    if (busqueda.ok()) {
      const { messages } = (await busqueda.json()) as { messages: { ID: string }[] };
      if (messages.length > 0) {
        const mensaje = await request.get(`${MAILPIT_URL}/api/v1/message/${messages[0].ID}`);
        token = ((await mensaje.json()) as { Text: string }).Text.match(/token=([A-Za-z0-9_-]+)/)?.[1] ?? '';
      }
    }
    if (!token) {
      await new Promise((listo) => setTimeout(listo, 500));
    }
  }
  await request.post(`${API_URL}/api/auth/verificacion`, { data: { token } });

  const login = await request.post(`${API_URL}/api/auth/login`, {
    data: { correo, contrasena: CONTRASENA_CLIENTE },
  });
  return (await login.json()) as { token: string; rol: string; correo: string };
}

/** Deja la sesión en sessionStorage, como haría el login del navegador. */
async function entrarComo(page: Page, sesion: { token: string; rol: string; correo: string }) {
  await page.addInitScript((datos) => {
    sessionStorage.setItem('crearcode-sesion', JSON.stringify(datos));
  }, sesion);
}

test('el equipo cotiza desde un lead, lo envia, y el cliente lo acepta desde su cuenta', async ({
  page,
  request,
}) => {
  const correoDelCliente = `cotizacion-e2e-${Date.now()}@correo-de-prueba.com`;
  const admin = await tokenAdmin(request);

  // 1. Llega un lead con el correo de ese cliente.
  const lead = await request.post(`${API_URL}/api/solicitudes`, {
    data: {
      nombre: 'Ana Perez',
      empresa: 'Panaderia El Trigal',
      correo: correoDelCliente,
      telefono: '3001234567',
      servicioDeInteres: 'DESARROLLO_A_LA_MEDIDA',
      mensaje: 'Necesito una app de pedidos para mi panaderia',
      aceptaConsentimiento: true,
    },
  });
  const { id: solicitudId } = await lead.json();
  await request.patch(`${API_URL}/api/solicitudes/${solicitudId}/estado`, {
    headers: { Authorization: `Bearer ${admin}` },
    data: { nuevoEstado: 'CONTACTADA' },
  });

  // 2. El equipo abre la cotización desde ese lead y le agrega un ítem.
  await entrarComo(page, { token: admin, rol: 'ADMIN', correo: ADMIN_USUARIO });
  await page.goto(`/admin/cotizaciones/nueva?solicitud=${solicitudId}`);
  await expect(page.getByText('Los datos del cliente se toman de la solicitud')).toBeVisible();
  await page.click('button[type="submit"]');

  await expect(page).toHaveURL(/\/admin\/cotizaciones\/[0-9a-f-]+$/);
  await page.getByRole('button', { name: 'Agregar ítem' }).click();
  await page.fill('#descripcion-0', 'Desarrollo del modulo de pedidos');
  await page.fill('#cantidad-0', '1');
  await page.fill('#valor-0', '5000000');
  await page.getByRole('button', { name: 'Guardar borrador' }).click();
  await expect(page.locator('.totales')).toContainText('5.000.000');

  // Accesibilidad del formulario del panel con datos reales.
  const axeDelPanel = await new AxeBuilder({ page }).analyze();
  expect(axeDelPanel.violations, JSON.stringify(axeDelPanel.violations, null, 2)).toEqual([]);

  // 3. Se envía: recibe número y queda congelada.
  page.once('dialog', (dialogo) => dialogo.accept());
  await page.getByRole('button', { name: 'Enviar al cliente' }).click();
  await expect(page.getByText('ya fue enviada')).toBeVisible();
  const numero = (await page.locator('h1').textContent())?.trim() ?? '';
  expect(numero).toMatch(/COT-\d{4}-\d{4,}/);

  // 4. El cliente entra a su cuenta y la ve.
  const sesionCliente = await clienteVerificado(request, correoDelCliente);
  const paginaCliente = await page.context().newPage();
  await paginaCliente.emulateMedia({ reducedMotion: 'reduce' });
  await entrarComo(paginaCliente, sesionCliente);
  await paginaCliente.goto('/mi-cuenta/cotizaciones');

  await expect(paginaCliente.getByText(numero)).toBeVisible();
  await paginaCliente.getByRole('button', { name: 'Ver detalle' }).click();
  await expect(paginaCliente.getByText('Desarrollo del modulo de pedidos')).toBeVisible();

  const axeDelCliente = await new AxeBuilder({ page: paginaCliente }).analyze();
  expect(axeDelCliente.violations, JSON.stringify(axeDelCliente.violations, null, 2)).toEqual([]);

  // 5. La acepta.
  paginaCliente.once('dialog', (dialogo) => dialogo.accept());
  await paginaCliente.getByRole('button', { name: 'Aceptar cotización' }).click();
  await expect(paginaCliente.getByText('Ya respondiste esta cotización.')).toBeVisible();

  // 6. Y el lead queda convertido, sin que nadie lo toque a mano.
  const solicitudes = await request.get(`${API_URL}/api/solicitudes?estado=CONVERTIDA`, {
    headers: { Authorization: `Bearer ${admin}` },
  });
  const convertidas = (await solicitudes.json()) as { id: string }[];
  expect(convertidas.map((s) => s.id)).toContain(solicitudId);
});

test('un cliente no puede abrir la cotizacion de otro', async ({ page, request }) => {
  const admin = await tokenAdmin(request);
  const correoDelDueno = `dueno-e2e-${Date.now()}@correo-de-prueba.com`;

  const creada = await request.post(`${API_URL}/api/cotizaciones`, {
    headers: { Authorization: `Bearer ${admin}` },
    data: {
      cliente: { nombre: 'Dueño', correo: correoDelDueno },
      impuestoPorcentaje: 0,
      diasDeValidez: 15,
      items: [{ descripcion: 'Servicio', cantidad: 1, valorUnitario: 1000000 }],
    },
  });
  const { id } = await creada.json();
  await request.post(`${API_URL}/api/cotizaciones/${id}/envio`, {
    headers: { Authorization: `Bearer ${admin}` },
  });

  const intruso = await clienteVerificado(request, `intruso-e2e-${Date.now()}@correo-de-prueba.com`);
  await entrarComo(page, intruso);
  await page.goto('/mi-cuenta/cotizaciones');

  // No la ve en su listado…
  await expect(page.getByText('Todavía no tienes cotizaciones')).toBeVisible();

  // …y pedirla directo por API con SU token tampoco funciona.
  const intento = await request.get(`${API_URL}/api/mis-cotizaciones/${id}`, {
    headers: { Authorization: `Bearer ${intruso.token}` },
  });
  expect(intento.status()).toBe(404);
});
