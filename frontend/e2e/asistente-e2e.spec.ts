import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';

/**
 * ISS-117: e2e del asistente IA (F9) contra el stub de Groq
 * (stub-groq.mjs — determinista, sin red externa ni key). Requiere el
 * stack completo corriendo con el backend apuntando al stub:
 *   node e2e/stub-groq.mjs   (puerto 9099)
 *   GROQ_API_URL=http://localhost:9099/openai/v1 ASISTENTE_LIMITE_ANONIMO=3 ./mvnw spring-boot:run
 * (el límite anónimo bajo hace verificable HU-38 sin decenas de
 * mensajes; en CI se configura igual).
 */

// El scroll-reveal de F8.5 se apaga para que axe no escanee en plena
// transición (misma razón que en accesibilidad-e2e.spec.ts).
test.beforeEach(async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
});

async function abrirAsistente(page: import('@playwright/test').Page) {
  await page.locator('.chat-burbuja').click();
  await expect(page.locator('.chat-panel')).toBeVisible();
}

async function preguntar(page: import('@playwright/test').Page, texto: string) {
  await page.fill('#pregunta-asistente', texto);
  await page.locator('.chat-formulario button[type="submit"]').click();
}

test('un visitante abre el asistente, usa una sugerencia y recibe respuesta', async ({ page }) => {
  await page.goto('/');
  await abrirAsistente(page);

  await page.locator('.chat-sugerencia').first().click();
  await expect(page.getByText('desarrollo a la medida, IA y automatización')).toBeVisible();

  // Accesibilidad del widget abierto con conversación (axe)
  const resultados = await new AxeBuilder({ page }).analyze();
  expect(resultados.violations, JSON.stringify(resultados.violations, null, 2)).toEqual([]);
});

test('preguntar por precios escala a un humano con el WhatsApp contextual', async ({ page }) => {
  await page.goto('/servicios/desarrollo-a-la-medida');
  await abrirAsistente(page);

  await preguntar(page, '¿Cuánto cuesta una app?');

  const escalamiento = page.locator('.chat-escalamiento');
  await expect(escalamiento).toBeVisible();
  await expect(escalamiento.getByText('mejor hablemos')).toBeVisible();
  await expect(escalamiento.locator('a[href^="https://wa.me/"]')).toBeVisible();
  await expect(escalamiento.locator('a[href="/contacto"]')).toBeVisible();
});

test('al agotar el cupo anonimo aparece la invitacion a crear cuenta', async ({ page }) => {
  await page.goto('/');
  await abrirAsistente(page);

  for (let i = 1; i <= 3; i++) {
    await preguntar(page, `pregunta número ${i}`);
    await expect(page.locator('.chat-mensaje')).toHaveCount(i * 2);
  }

  await preguntar(page, 'una más');

  const aviso = page.locator('.chat-aviso');
  await expect(aviso).toBeVisible();
  await expect(aviso.getByText('Crea tu cuenta')).toBeVisible();
  await expect(aviso.locator('a[href="/registro"]')).toBeVisible();
});
