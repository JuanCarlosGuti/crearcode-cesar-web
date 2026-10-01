import AxeBuilder from '@axe-core/playwright';
import { APIRequestContext, Page, expect, test } from '@playwright/test';

/**
 * ISS-223: el ciclo completo de F12 — una cotización aceptada se
 * convierte en proyecto, el equipo manda una entrega a revisión, al
 * cliente le llega el correo, entra por ese enlace y la aprueba; el
 * equipo registra el pago y el saldo del cliente cambia. Un cliente
 * ajeno no ve nada.
 *
 * Requiere el stack completo (docker compose + backend + frontend). Los
 * correos caen en Mailpit, como el resto en local y en CI.
 */

const API_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const MAILPIT_URL = process.env['E2E_MAILPIT_URL'] ?? 'http://localhost:8025';
const ADMIN_USUARIO = 'admin@crearcode-cesar.local';
const ADMIN_CONTRASENA = 'cambiar-en-produccion';
const CONTRASENA_CLIENTE = 'contrasena-de-prueba-larga';

interface Sesion {
  token: string;
  rol: string;
  correo: string;
}

test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

/** Cacheado: el login tiene su propio rate limit por IP. */
let tokenAdminCacheado: string | null = null;

async function tokenAdmin(request: APIRequestContext): Promise<string> {
  if (!tokenAdminCacheado) {
    const respuesta = await request.post(`${API_URL}/api/auth/login`, {
      data: { correo: ADMIN_USUARIO, contrasena: ADMIN_CONTRASENA },
    });
    tokenAdminCacheado = (await respuesta.json()).token;
  }
  return tokenAdminCacheado!;
}

/** Espera un correo en Mailpit y devuelve su texto. */
async function correoRecibido(request: APIRequestContext, para: string, asunto: string): Promise<string> {
  for (let intento = 0; intento < 40; intento++) {
    const busqueda = await request.get(
      `${MAILPIT_URL}/api/v1/search?query=${encodeURIComponent(`to:"${para}" subject:"${asunto}"`)}`,
    );
    if (busqueda.ok()) {
      const { messages } = (await busqueda.json()) as { messages: { ID: string }[] };
      if (messages.length > 0) {
        const mensaje = await request.get(`${MAILPIT_URL}/api/v1/message/${messages[0].ID}`);
        return ((await mensaje.json()) as { Text: string }).Text;
      }
    }
    await new Promise((listo) => setTimeout(listo, 500));
  }
  throw new Error(`No llegó el correo «${asunto}» a ${para}`);
}

/** Cliente registrado y verificado con el enlace real del correo. */
async function clienteVerificado(request: APIRequestContext, correo: string): Promise<Sesion> {
  await request.post(`${API_URL}/api/auth/registro`, { data: { correo, contrasena: CONTRASENA_CLIENTE } });
  const texto = await correoRecibido(request, correo, 'Verifica tu cuenta');
  const token = texto.match(/token=([A-Za-z0-9_-]+)/)?.[1] ?? '';
  await request.post(`${API_URL}/api/auth/verificacion`, { data: { token } });
  const login = await request.post(`${API_URL}/api/auth/login`, { data: { correo, contrasena: CONTRASENA_CLIENTE } });
  return (await login.json()) as Sesion;
}

/** Deja la sesión en sessionStorage, como haría el login del navegador. */
async function entrarComo(page: Page, sesion: Sesion) {
  await page.addInitScript((datos) => {
    sessionStorage.setItem('crearcode-sesion', JSON.stringify(datos));
  }, sesion);
}

async function sinViolacionesDeAccesibilidad(page: Page) {
  const resultado = await new AxeBuilder({ page }).analyze();
  expect(resultado.violations, JSON.stringify(resultado.violations, null, 2)).toEqual([]);
}

/** Cotización de 1 M (diseño) + 3 M (catálogo) con IVA, enviada y aceptada por el cliente. */
async function cotizacionAceptada(request: APIRequestContext, admin: string, cliente: Sesion): Promise<string> {
  const creada = await request.post(`${API_URL}/api/cotizaciones`, {
    headers: { Authorization: `Bearer ${admin}` },
    data: {
      cliente: { nombre: 'Café Valle', correo: cliente.correo },
      impuestoPorcentaje: 19,
      diasDeValidez: 15,
      items: [
        { descripcion: 'Diseño de la tienda', cantidad: 1, valorUnitario: 1000000 },
        { descripcion: 'Catálogo de productos', cantidad: 1, valorUnitario: 3000000 },
      ],
    },
  });
  const { id } = await creada.json();
  await request.post(`${API_URL}/api/cotizaciones/${id}/envio`, { headers: { Authorization: `Bearer ${admin}` } });
  const aceptada = await request.post(`${API_URL}/api/mis-cotizaciones/${id}/aceptacion`, {
    headers: { Authorization: `Bearer ${cliente.token}` },
  });
  expect(aceptada.ok()).toBe(true);
  return id;
}

test('una cotizacion aceptada se vuelve proyecto, el cliente aprueba desde su cuenta y paga', async ({
  page,
  request,
}) => {
  test.setTimeout(120_000);
  const admin = await tokenAdmin(request);
  const cliente = await clienteVerificado(request, `proyecto-e2e-${Date.now()}@correo-de-prueba.com`);
  const cotizacion = await cotizacionAceptada(request, admin, cliente);

  // 1. Desde la cotización aceptada, el equipo crea el proyecto con el plan propuesto.
  await entrarComo(page, { token: admin, rol: 'ADMIN', correo: ADMIN_USUARIO });
  await page.goto(`/admin/cotizaciones/${cotizacion}`);
  await page.getByRole('link', { name: 'Crear proyecto' }).click();
  await expect(page.locator('.suma')).toContainText('4.000.000');
  await page.fill('#nombre-proyecto', 'Tienda en línea de Café Valle');
  await page.getByRole('button', { name: 'Crear proyecto' }).click();
  await expect(page).toHaveURL(/\/admin\/proyectos\/[0-9a-f-]{36}$/);
  const proyecto = page.url().split('/').pop()!;
  await sinViolacionesDeAccesibilidad(page);

  // 2. Manda la primera entrega a revisión.
  const diseno = page.locator('li.entrega', { hasText: 'Diseño de la tienda' });
  await diseno.getByRole('button', { name: 'Empezar' }).click();
  await diseno.getByRole('button', { name: 'Enviar a revisión' }).click();
  await expect(diseno.locator('app-estado-de-entregable')).toContainText('Para revisar');

  // 3. Al cliente le llega el correo con el enlace directo a su proyecto.
  const aviso = await correoRecibido(request, cliente.correo, 'Tienes algo para revisar');
  const enlace = aviso.match(/https?:\/\/[^\s]+\/mi-cuenta\/proyectos\/[0-9a-f-]{36}/)?.[0];
  expect(enlace).toContain(proyecto);

  // 4. Entra por ese enlace, lo ve destacado y lo aprueba.
  const paginaCliente = await page.context().newPage();
  await paginaCliente.emulateMedia({ reducedMotion: 'reduce' });
  await entrarComo(paginaCliente, cliente);
  await paginaCliente.goto(new URL(enlace!).pathname);
  await expect(paginaCliente.locator('.para-revisar')).toContainText('Diseño de la tienda');
  await sinViolacionesDeAccesibilidad(paginaCliente);

  const tarjeta = paginaCliente.locator('.entregable--destacado');
  await tarjeta.getByRole('button', { name: 'Aprobar' }).click();
  await tarjeta.getByRole('button', { name: 'Sí, aprobar' }).click();
  // 1 M aprobado de 4 M: 25 %.
  await expect(paginaCliente.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '25');
  await expect(paginaCliente.getByText(/Lo aprobaste el/)).toBeVisible();

  // 5. Al equipo le llega que el cliente aprobó.
  await correoRecibido(request, 'admin@crearcodecesar.com', 'El cliente aprobó');

  // 6. El equipo registra el anticipo (1 M + IVA) y el cliente lo ve al día.
  await page.reload();
  await page.fill('#pago-monto', '1190000');
  await page.getByRole('button', { name: 'Registrar pago' }).click();
  await expect(page.locator('.historial')).toContainText('1.190.000');
  await correoRecibido(request, cliente.correo, 'Recibimos tu pago');

  await paginaCliente.reload();
  await expect(paginaCliente.locator('.pagos__totales')).toContainText('1.190.000');
  // Lo que queda (el catálogo) todavía no se cobra: nada por pagar ahora.
  await expect(paginaCliente.locator('.pagos__por-pagar')).toContainText('$ 0');
});

test('un cliente no puede ver ni aprobar el proyecto de otro', async ({ page, request }) => {
  test.setTimeout(90_000);
  const admin = await tokenAdmin(request);
  const dueno = await clienteVerificado(request, `dueno-proyecto-e2e-${Date.now()}@correo-de-prueba.com`);
  const cotizacion = await cotizacionAceptada(request, admin, dueno);
  const propuesta = await request.get(`${API_URL}/api/proyectos/propuesta?cotizacion=${cotizacion}`, {
    headers: { Authorization: `Bearer ${admin}` },
  });
  const creado = await request.post(`${API_URL}/api/proyectos`, {
    headers: { Authorization: `Bearer ${admin}` },
    data: {
      cotizacionId: cotizacion,
      descripcion: { nombre: 'Proyecto ajeno', inicio: '2026-09-01' },
      plan: await propuesta.json(),
    },
  });
  const { id } = await creado.json();

  const intruso = await clienteVerificado(request, `intruso-proyecto-e2e-${Date.now()}@correo-de-prueba.com`);
  await entrarComo(page, intruso);
  await page.goto(`/mi-cuenta/proyectos/${id}`);

  // Ni lo ve en la página…
  await expect(page.getByText('No encontramos este proyecto en tu cuenta.')).toBeVisible();

  // …ni puede pedirlo por API con su token: 404, como si no existiera.
  const intento = await request.get(`${API_URL}/api/mis-proyectos/${id}`, {
    headers: { Authorization: `Bearer ${intruso.token}` },
  });
  expect(intento.status()).toBe(404);
});
