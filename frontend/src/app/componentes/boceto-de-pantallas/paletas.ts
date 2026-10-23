import { EstiloDeLetra, PaletaDeBoceto } from '../../api/demo-api';

/**
 * Los colores de cada paleta que puede elegir la IA (decisión 35). Los
 * define el sitio, no el modelo, y `paletas.spec.ts` comprueba contraste
 * AA en cada par de texto que se dibuja.
 */
export interface Paleta {
  /** Cómo se presenta en la tarjeta de identidad. */
  nombre: string;
  pantalla: string;
  superficie: string;
  tinta: string;
  suave: string;
  /** Fondo de los dibujos, chips y burbujas del negocio. */
  tinte: string;
  acento: string;
  sobreAcento: string;
  banner: string;
  bannerTexto: string;
}

export const PALETAS: Record<PaletaDeBoceto, Paleta> = {
  FRESCA: {
    nombre: 'Fresca: menta y verde oscuro',
    pantalla: '#f4f7f6',
    superficie: '#ffffff',
    tinta: '#12201f',
    suave: '#4f605e',
    tinte: '#dcefe8',
    acento: '#3ddc97',
    sobreAcento: '#071115',
    banner: '#0f2027',
    bannerTexto: '#f4f7f6',
  },
  CALIDA: {
    nombre: 'Cálida: naranja y terracota',
    pantalla: '#fff8f1',
    superficie: '#ffffff',
    tinta: '#2a1a10',
    suave: '#6b4c3b',
    tinte: '#ffe8d1',
    acento: '#c2410c',
    sobreAcento: '#ffffff',
    banner: '#7c2d12',
    bannerTexto: '#fff7ed',
  },
  NOCTURNA: {
    nombre: 'Nocturna: negro y dorado',
    pantalla: '#151515',
    superficie: '#232323',
    tinta: '#f5f0e6',
    suave: '#c9bfae',
    tinte: '#3a3222',
    acento: '#d4a94a',
    sobreAcento: '#151515',
    banner: '#000000',
    bannerTexto: '#f2d48a',
  },
  SALUD: {
    nombre: 'Salud: blanco y azul',
    pantalla: '#f3f8fc',
    superficie: '#ffffff',
    tinta: '#0f2233',
    suave: '#4a5f73',
    tinte: '#dbeafe',
    acento: '#1d6fb8',
    sobreAcento: '#ffffff',
    banner: '#0f3a5f',
    bannerTexto: '#eaf4fc',
  },
  ELEGANTE: {
    nombre: 'Elegante: vino y rosa',
    pantalla: '#fbf5f6',
    superficie: '#ffffff',
    tinta: '#2b1520',
    suave: '#6e5260',
    tinte: '#fbe3ee',
    acento: '#9d174d',
    sobreAcento: '#ffffff',
    banner: '#4a0e2b',
    bannerTexto: '#fde7f0',
  },
  INDUSTRIAL: {
    nombre: 'Industrial: gris y amarillo',
    pantalla: '#f2f2ef',
    superficie: '#ffffff',
    tinta: '#1c1c1a',
    suave: '#55554f',
    tinte: '#fdf0c2',
    acento: '#f5c518',
    sobreAcento: '#1c1c1a',
    banner: '#262624',
    bannerTexto: '#f5c518',
  },
  NATURAL: {
    nombre: 'Natural: oliva y crema',
    pantalla: '#f6f4ec',
    superficie: '#ffffff',
    tinta: '#1f2a17',
    suave: '#555f48',
    tinte: '#e5efd0',
    acento: '#4d7c0f',
    sobreAcento: '#ffffff',
    banner: '#2f3e1c',
    bannerTexto: '#f0f5e1',
  },
  OCEANO: {
    nombre: 'Océano: azul petróleo y aguamarina',
    pantalla: '#f0f7f8',
    superficie: '#ffffff',
    tinta: '#0b2a33',
    suave: '#4a6670',
    tinte: '#cdf3f9',
    acento: '#0e7490',
    sobreAcento: '#ffffff',
    banner: '#083344',
    bannerTexto: '#e0f7fa',
  },
};

/**
 * Las tres letras de la marca, con fuentes que el sitio ya carga o del
 * sistema: ninguna descarga nueva que le cueste rendimiento a la página.
 */
export const LETRAS: Record<EstiloDeLetra, { nombre: string; familia: string }> = {
  MODERNA: { nombre: 'Letra moderna', familia: "'Space Grotesk', 'Figtree', sans-serif" },
  CLASICA: { nombre: 'Letra clásica', familia: "Georgia, 'Times New Roman', serif" },
  SENCILLA: { nombre: 'Letra sencilla', familia: "'Figtree', -apple-system, 'Segoe UI', sans-serif" },
};

/** Si llega algo fuera de la lista, la de la casa. */
export function paletaDe(nombre: string): Paleta {
  return PALETAS[nombre as PaletaDeBoceto] ?? PALETAS.FRESCA;
}

export function letraDe(nombre: string): { nombre: string; familia: string } {
  return LETRAS[nombre as EstiloDeLetra] ?? LETRAS.MODERNA;
}

/** Las variables CSS con las que el celular y el logo toman la paleta. */
export function variablesDePaleta(paleta: Paleta): Record<string, string> {
  return {
    '--b-pantalla': paleta.pantalla,
    '--b-superficie': paleta.superficie,
    '--b-tinta': paleta.tinta,
    '--b-suave': paleta.suave,
    '--b-tinte': paleta.tinte,
    '--b-acento': paleta.acento,
    '--b-sobre-acento': paleta.sobreAcento,
    '--b-banner': paleta.banner,
    '--b-banner-texto': paleta.bannerTexto,
  };
}
