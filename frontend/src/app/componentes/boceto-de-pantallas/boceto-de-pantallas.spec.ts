import { TestBed } from '@angular/core/testing';

import { IdentidadDeMarca, PantallaDeBoceto } from '../../api/demo-api';
import { BocetoDePantallas } from './boceto-de-pantallas';
import { PALETAS } from './paletas';

const pieza = (tipo: string, titulo: string, elementos: [string, string?, string?][] = []) => ({
  tipo: tipo as PantallaDeBoceto['piezas'][number]['tipo'],
  titulo,
  elementos: elementos.map(([texto, detalle = '', icono = '']) => ({ texto, detalle, icono })),
});

const IDENTIDAD: IdentidadDeMarca = {
  nombre: 'Sabor Valduparense',
  eslogan: 'Comida casera hasta tu puerta',
  paleta: 'CALIDA',
  logo: { forma: 'CIRCULO', simbolo: '🍲', letra: 'SENCILLA' },
};

const PANTALLAS: PantallaDeBoceto[] = [
  {
    nombre: 'Inicio',
    piezas: [
      pieza('ENCABEZADO', 'Menú de hoy'),
      pieza('BUSCADOR', 'Busca un plato'),
      pieza('BANNER', 'Menú del día listo', [['Ver menú']]),
      pieza('CATEGORIAS', '', [['Almuerzos', '', '🍛'], ['Desayunos', '', '🍳']]),
      pieza('TARJETAS', '', [['Almuerzo ejecutivo', 'Sopa, seco y jugo', '🍗']]),
      pieza('NAVEGACION', '', [['Inicio', '', '🏠'], ['Pedidos', '', '🧾'], ['Mi cuenta', '', '👤']]),
    ],
  },
  {
    nombre: 'Pedido',
    piezas: [
      pieza('FORMULARIO', 'Datos de entrega', [['Dirección', 'Ej. Calle 16 # 9-20']]),
      pieza('HORARIOS', 'Hora de entrega', [['11:30 a. m.'], ['12:00 m.']]),
      pieza('BOTON', 'Confirmar pedido'),
    ],
  },
  {
    nombre: 'Cocina',
    piezas: [
      pieza('INDICADORES', '', [['Pedidos de hoy', '18', '📦']]),
      pieza('LISTA', 'Pedidos', [['Ana · almuerzo', 'En camino', '🛵'], ['Luis · desayuno', 'En cocina']]),
      pieza('CHAT', '', [
        ['Hola, ¿ya salió mi pedido?', 'cliente'],
        ['Sí, va en camino.', 'negocio'],
      ]),
    ],
  },
];

async function dibujar(pantallas: PantallaDeBoceto[] = PANTALLAS, identidad: IdentidadDeMarca = IDENTIDAD) {
  const fixture = TestBed.createComponent(BocetoDePantallas);
  fixture.componentRef.setInput('pantallas', pantallas);
  fixture.componentRef.setInput('identidad', identidad);
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

describe('BocetoDePantallas (ISS-234, ADR-16; identidad desde ISS-238)', () => {
  it('dibuja cada pantalla en un celular con su nombre', async () => {
    const el = await dibujar();

    expect(el.querySelectorAll('figure.celular')).toHaveLength(3);
    expect(Array.from(el.querySelectorAll('figcaption')).map((nombre) => nombre.textContent?.trim())).toEqual([
      'Inicio',
      'Pedido',
      'Cocina',
    ]);
  });

  it('cada pieza del catálogo se dibuja con su forma y sus textos', async () => {
    const el = await dibujar();

    for (const tipo of [
      'encabezado',
      'buscador',
      'banner',
      'categorias',
      'tarjetas',
      'navegacion',
      'formulario',
      'horarios',
      'boton',
      'indicadores',
      'lista',
      'chat',
    ]) {
      expect(el.querySelector(`.pieza--${tipo}`), tipo).not.toBeNull();
    }
    const texto = el.textContent ?? '';
    for (const visible of ['Menú de hoy', 'Busca un plato', 'Ver menú', 'Almuerzos', 'Sopa, seco y jugo', 'Mi cuenta',
      'Ej. Calle 16 # 9-20', '12:00 m.', 'Confirmar pedido', '18', 'En camino']) {
      expect(texto).toContain(visible);
    }
  });

  it('los celulares toman los colores de la paleta de la marca', async () => {
    const el = await dibujar();

    const tira = el.querySelector<HTMLElement>('.boceto')!;
    expect(tira.style.getPropertyValue('--b-acento')).toBe(PALETAS.CALIDA.acento);
    expect(tira.style.getPropertyValue('--b-pantalla')).toBe(PALETAS.CALIDA.pantalla);
  });

  it('cada celular lleva arriba el logo y el nombre de la marca', async () => {
    const el = await dibujar();

    const barras = el.querySelectorAll('.celular__marca');
    expect(barras).toHaveLength(3);
    expect(barras[0].textContent).toContain('Sabor Valduparense');
    expect(barras[0].querySelector('app-logo-de-marca .logo')?.textContent?.trim()).toBe('🍲');
  });

  it('los productos, categorías, filas, indicadores y secciones llevan su dibujo', async () => {
    const el = await dibujar();

    expect(el.querySelector('.tarjeta__foto')?.textContent?.trim()).toBe('🍗');
    expect(el.querySelector('.pieza--categorias li')?.textContent).toContain('🍛');
    expect(el.querySelector('.pieza--navegacion li')?.textContent).toContain('🏠');
    expect(el.querySelector('.pieza--indicadores li')?.textContent).toContain('📦');
    const filas = el.querySelectorAll('.fila__inicial');
    // Con dibujo, el dibujo; sin él, la inicial.
    expect(filas[0].textContent?.trim()).toBe('🛵');
    expect(filas[1].textContent?.trim()).toBe('L');
  });

  it('los dibujos son decorativos: el texto de cada elemento ya está escrito', async () => {
    const el = await dibujar();

    const dibujos = el.querySelectorAll('.dibujo');
    expect(dibujos.length).toBeGreaterThan(0);
    dibujos.forEach((dibujo) => expect(dibujo.getAttribute('aria-hidden')).toBe('true'));
  });

  it('en el chat, cada mensaje va del lado de quien lo dice', async () => {
    const el = await dibujar();

    expect(el.querySelector('.burbuja--cliente')?.textContent).toContain('¿ya salió mi pedido?');
    expect(el.querySelector('.burbuja--negocio')?.textContent).toContain('va en camino');
  });

  it('un tipo de pieza que no conoce no rompe el dibujo', async () => {
    const el = await dibujar([
      { nombre: 'Inicio', piezas: [pieza('CARRUSEL', 'No existe'), pieza('BOTON', 'Pedir ahora')] },
      { nombre: 'Pedido', piezas: [pieza('ENCABEZADO', 'Tu pedido'), pieza('BOTON', 'Confirmar')] },
    ]);

    expect(el.textContent).not.toContain('No existe');
    expect(el.textContent).toContain('Pedir ahora');
  });

  it('es un dibujo, no una app: no hay botones, campos ni enlaces que se puedan usar', async () => {
    const el = await dibujar();

    expect(el.querySelectorAll('button, input, textarea, select, a')).toHaveLength(0);
  });

  it('la tira de pantallas se puede recorrer con el teclado y tiene nombre', async () => {
    const el = await dibujar();

    const tira = el.querySelector('.boceto')!;
    expect(tira.getAttribute('tabindex')).toBe('0');
    expect(tira.getAttribute('role')).toBe('region');
    expect(tira.getAttribute('aria-label')).toBeTruthy();
  });
});
