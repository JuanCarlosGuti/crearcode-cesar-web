import { APIRequestContext, expect, test } from '@playwright/test';

/**
 * ISS-122/ISS-130: la página viva /herramientas. El cotizador funciona
 * sin backend; el simulador, el diagnóstico y el demo de diseño
 * necesitan el stack completo con el stub de IA (stub-groq.mjs, que
 * también sirve las imágenes) y Mailpit para la cuenta del demo.
 */

const API_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8080';
const MAILPIT_URL = process.env['E2E_MAILPIT_URL'] ?? 'http://localhost:8025';

/** Registra, verifica (enlace real de Mailpit) y loguea un cliente vía API. */
async function sesionDeClienteNueva(request: APIRequestContext) {
  const correo = `demo-e2e-${Date.now()}@correo-de-prueba.com`;
  const contrasena = 'contrasena-demo-e2e';
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

  const login = await request.post(`${API_URL}/api/auth/login`, { data: { correo, contrasena } });
  const datos = (await login.json()) as { token: string; rol: string; correo: string };
  return { token: datos.token, rol: datos.rol, correo: datos.correo };
}

test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

test('el centro muestra las herramientas con honestidad de estados', async ({ page }) => {
  await page.goto('/herramientas');

  await expect(page.getByRole('heading', { level: 1 })).toContainText('Herramientas');
  await expect(page.locator('.herramienta-tarjeta')).toHaveCount(5);
  await expect(page.locator('.badge-muy-pronto')).toHaveCount(0);
});

test('el aside de una pagina de servicio lleva al diagnostico anclado (ISS-134)', async ({ page }) => {
  await page.goto('/servicios/desarrollo-a-la-medida');

  await page.locator('.servicio-aside a[href="/herramientas#diagnostico"]').click();

  await expect(page).toHaveURL(/\/herramientas#diagnostico$/);
  await expect(page.locator('#diagnostico')).toBeInViewport();
});

test('el demo de diseno muestra el estado bloqueado a un visitante anonimo (HU-42)', async ({ page }) => {
  await page.goto('/herramientas');

  const demo = page.locator('app-demo-diseno');
  await expect(demo.getByText('Crea tu cuenta gratis para ver tu boceto')).toBeVisible();
  await expect(demo.locator('a[href="/registro"]')).toBeVisible();
  await expect(demo.locator('#demo-sector')).toHaveCount(0);
});

test('un cliente registrado genera su boceto con imagen y funcionalidades (F10d)', async ({ page, request }) => {
  const sesion = await sesionDeClienteNueva(request);
  await page.addInitScript(
    (datos) => sessionStorage.setItem('crearcode-sesion', JSON.stringify(datos)),
    sesion,
  );

  await page.goto('/herramientas');
  await expect(page.locator('#demo-sector')).toBeVisible();

  await page.fill('#demo-sector', 'Restaurante');
  await page.fill('#demo-que-hace', 'Vendemos almuerzos y domicilios');
  await page.fill('#demo-que-necesita', 'Recibir pedidos sin saturar el WhatsApp');
  await page.locator('.demo-generar').click();

  await expect(page.getByText('App de pedidos para tu restaurante')).toBeVisible({ timeout: 20000 });
  await expect(page.locator('.demo-funcionalidad')).toHaveCount(5);
  const imagen = page.locator('.demo-imagen img');
  await expect(imagen).toBeVisible();
  expect(await imagen.getAttribute('src')).toMatch(/^data:image\/(png|jpeg);base64,/);
  await expect(page.locator('.demo-variacion')).toBeVisible();
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
