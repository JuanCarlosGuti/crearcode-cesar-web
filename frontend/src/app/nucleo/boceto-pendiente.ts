import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';

import { SolicitudDeDemo } from '../api/demo-api';

export const CLAVE_BOCETO_PENDIENTE = 'crearcode-boceto-pendiente';
export const VIGENCIA_DEL_BOCETO_PENDIENTE_MS = 24 * 60 * 60 * 1000;

interface BocetoGuardado extends SolicitudDeDemo {
  guardadoEn: number;
}

/**
 * Lo que el visitante escribió en la tarjeta del demo de la Home, a la
 * espera de que pueda generar su boceto (ISS-226, decisión 31 de
 * docs/10).
 *
 * <p>
 * Va en localStorage y no en sessionStorage, que es lo que usa la
 * sesión, porque entre la tarjeta y el boceto suele haber un registro:
 * el enlace de verificación del correo abre otra pestaña, y
 * sessionStorage no cruza pestañas. Nunca sale del navegador hasta que
 * se genera, dura 24 horas y se entrega una sola vez.
 *
 * <p>
 * Todo acceso al almacenamiento va en try/catch: en modo privado o con
 * los datos del sitio bloqueados, el navegador lanza al leer o
 * escribir, y eso no puede tumbar la Home ni el ingreso.
 */
@Injectable({ providedIn: 'root' })
export class BocetoPendienteService {
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  /** Lo último que se escribió gana; si no se escribió nada, se olvida lo anterior. */
  guardar(datos: SolicitudDeDemo): void {
    const limpio: SolicitudDeDemo = {
      sector: datos.sector.trim(),
      queHace: datos.queHace.trim(),
      queNecesita: datos.queNecesita.trim(),
    };
    if (!limpio.sector && !limpio.queHace && !limpio.queNecesita) {
      this.borrar();
      return;
    }
    this.escribir(JSON.stringify({ ...limpio, guardadoEn: Date.now() } satisfies BocetoGuardado));
  }

  /** Pregunta sin consumirlo: para decidir a dónde llevar al visitante. */
  hay(): boolean {
    return this.leer() !== null;
  }

  /** Lo entrega una sola vez: quien lo toma es quien genera el boceto. */
  tomar(): SolicitudDeDemo | null {
    const guardado = this.leer();
    this.borrar();
    if (guardado === null) {
      return null;
    }
    return { sector: guardado.sector, queHace: guardado.queHace, queNecesita: guardado.queNecesita };
  }

  private leer(): BocetoGuardado | null {
    if (!this.esNavegador) {
      return null;
    }
    let crudo: string | null;
    try {
      crudo = localStorage.getItem(CLAVE_BOCETO_PENDIENTE);
    } catch {
      return null;
    }
    if (crudo === null) {
      return null;
    }
    let guardado: BocetoGuardado;
    try {
      guardado = JSON.parse(crudo) as BocetoGuardado;
    } catch {
      this.borrar();
      return null;
    }
    if (!this.tieneLaForma(guardado) || Date.now() - guardado.guardadoEn >= VIGENCIA_DEL_BOCETO_PENDIENTE_MS) {
      this.borrar();
      return null;
    }
    return guardado;
  }

  private tieneLaForma(guardado: BocetoGuardado): boolean {
    return (
      typeof guardado === 'object' &&
      guardado !== null &&
      typeof guardado.sector === 'string' &&
      typeof guardado.queHace === 'string' &&
      typeof guardado.queNecesita === 'string' &&
      typeof guardado.guardadoEn === 'number'
    );
  }

  private escribir(valor: string): void {
    if (!this.esNavegador) {
      return;
    }
    try {
      localStorage.setItem(CLAVE_BOCETO_PENDIENTE, valor);
    } catch {
      // Sin almacenamiento el visitante pierde lo escrito, no la página.
    }
  }

  private borrar(): void {
    if (!this.esNavegador) {
      return;
    }
    try {
      localStorage.removeItem(CLAVE_BOCETO_PENDIENTE);
    } catch {
      // Igual que al escribir.
    }
  }
}
