import express from 'express';
import type { Server } from 'node:http';
import { join } from 'node:path';
import { afterAll, beforeAll, describe, expect, it } from 'vitest';

import { cabeceraHsts, esPeticionHttps, valorDeHsts } from './hsts';

describe('valorDeHsts', () => {
  it('arranca conservador en un dia cuando no hay variable de entorno', () => {
    expect(valorDeHsts(undefined)).toBe('max-age=86400; includeSubDomains');
  });

  it('toma el max-age de la variable de entorno para poder subirlo sin tocar codigo', () => {
    expect(valorDeHsts('31536000')).toBe('max-age=31536000; includeSubDomains');
  });

  it('ignora un valor en blanco y cae al default', () => {
    expect(valorDeHsts('   ')).toBe('max-age=86400; includeSubDomains');
  });

  // Entrar a la lista de precarga es practicamente irreversible: no se
  // emite hasta que el max-age lleve meses en 31536000.
  it('nunca incluye preload', () => {
    expect(valorDeHsts('31536000')).not.toContain('preload');
  });
});

describe('esPeticionHttps', () => {
  it('reconoce https', () => {
    expect(esPeticionHttps('https')).toBe(true);
  });

  it('con varios saltos de proxy se queda con el protocolo del cliente', () => {
    expect(esPeticionHttps('https,http')).toBe(true);
    expect(esPeticionHttps('http,https')).toBe(false);
  });

  it('sin la cabecera (desarrollo local por http) devuelve false', () => {
    expect(esPeticionHttps(undefined)).toBe(false);
    expect(esPeticionHttps('http')).toBe(false);
  });
});

describe('cabeceraHsts montada como en server.ts', () => {
  let servidor: Server;
  let base: string;

  beforeAll(async () => {
    const app = express();
    // Mismo orden que server.ts: HSTS primero, estaticos despues, y el
    // handler de la aplicacion al final.
    app.use(cabeceraHsts());
    app.use(express.static(join(process.cwd(), 'public'), { index: false, redirect: false }));
    app.use((_req, res) => res.type('html').send('<!doctype html><title>home</title>'));

    servidor = await new Promise<Server>((listo) => {
      const s = app.listen(0, () => listo(s));
    });
    const direccion = servidor.address();
    const puerto = typeof direccion === 'object' && direccion ? direccion.port : 0;
    base = `http://127.0.0.1:${puerto}`;
  });

  afterAll(async () => {
    await new Promise<void>((listo) => servidor.close(() => listo()));
  });

  it('emite la cabecera en la raiz (que la sirve el router de Angular)', async () => {
    const respuesta = await fetch(`${base}/`, { headers: { 'x-forwarded-proto': 'https' } });

    expect(respuesta.headers.get('strict-transport-security')).toBe('max-age=86400; includeSubDomains');
  });

  it('emite la cabecera tambien en el estatico /monday-app-association.json', async () => {
    const respuesta = await fetch(`${base}/monday-app-association.json`, {
      headers: { 'x-forwarded-proto': 'https' },
    });

    expect(respuesta.status).toBe(200);
    expect(respuesta.headers.get('strict-transport-security')).toBe('max-age=86400; includeSubDomains');
  });

  it('no la emite por http, para no romper el desarrollo local', async () => {
    const raiz = await fetch(`${base}/`);
    const estatico = await fetch(`${base}/monday-app-association.json`);

    expect(raiz.headers.get('strict-transport-security')).toBeNull();
    expect(estatico.headers.get('strict-transport-security')).toBeNull();
  });
});
