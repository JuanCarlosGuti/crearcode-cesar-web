// Script de auditoria manual, no forma parte de la suite de tests.
// Requiere el build de produccion servido (ver README/CLAUDE.md):
//   npx ng build && node dist/frontend/server/server.mjs
// Uso: node scripts/lighthouse-audit.mjs <baseUrl>

import { spawn } from 'node:child_process';
import { mkdtempSync, rmSync, writeFileSync, mkdirSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { chromium } from 'playwright';
import lighthouse from 'lighthouse';

const baseUrl = process.argv[2] ?? 'http://localhost:4300';
const rutas = ['/', '/servicios/desarrollo-a-la-medida', '/herramientas', '/contacto'];
const PORT = 9222;

const userDataDir = mkdtempSync(join(tmpdir(), 'lh-chrome-'));
const chromeProc = spawn(
  chromium.executablePath(),
  [`--remote-debugging-port=${PORT}`, '--headless=new', '--disable-gpu', `--user-data-dir=${userDataDir}`],
  { stdio: 'ignore' },
);

async function esperarCdp() {
  for (let intento = 0; intento < 50; intento++) {
    try {
      const res = await fetch(`http://localhost:${PORT}/json/version`);
      if (res.ok) return;
    } catch {
      // Chrome todavia no levanto el endpoint CDP; se reintenta.
    }
    await new Promise((r) => setTimeout(r, 200));
  }
  throw new Error('Chrome no expuso el puerto de depuracion a tiempo');
}

try {
  await esperarCdp();

  mkdirSync('lighthouse-reports', { recursive: true });
  const resumen = [];

  for (const ruta of rutas) {
    const url = `${baseUrl}${ruta}`;
    const runnerResult = await lighthouse(url, { port: PORT, output: 'json', logLevel: 'error' });
    const { categories } = runnerResult.lhr;
    const scores = {
      ruta,
      performance: Math.round(categories.performance.score * 100),
      accesibilidad: Math.round(categories.accessibility.score * 100),
      buenasPracticas: Math.round(categories['best-practices'].score * 100),
      seo: Math.round(categories.seo.score * 100),
    };
    resumen.push(scores);
    console.log(JSON.stringify(scores));

    const nombreArchivo = ruta === '/' ? 'home' : ruta.replaceAll('/', '_').replace(/^_/, '');
    writeFileSync(join('lighthouse-reports', `${nombreArchivo}.json`), runnerResult.report);
  }

  console.log('\nResumen:');
  console.table(resumen);
} finally {
  await new Promise((resolve) => {
    chromeProc.once('exit', resolve);
    chromeProc.kill();
  });
  // En Windows el directorio de perfil de Chrome puede quedar bloqueado un
  // instante tras salir el proceso; se reintenta antes de darse por vencido.
  rmSync(userDataDir, { recursive: true, force: true, maxRetries: 5, retryDelay: 200 });
}
