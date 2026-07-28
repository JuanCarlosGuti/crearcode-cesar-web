import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';

const CLAVE_STORAGE = 'crearcode-sesion';

export type RolDeSesion = 'ADMIN' | 'CLIENTE';

export interface DatosDeSesion {
  token: string;
  rol: RolDeSesion;
  correo: string;
}

/**
 * Sesión en sessionStorage: se pierde al cerrar la pestaña, sin
 * revocación en el servidor antes de que expire el JWT (trade-off
 * consciente de v1, ver ADR-08). Desde F8 guarda también rol y correo
 * para distinguir el panel admin de la cuenta de cliente.
 */
@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly datos = signal<DatosDeSesion | null>(this.leerSesionAlmacenada());

  readonly estaAutenticado = computed(() => this.datos() !== null);
  readonly esAdmin = computed(() => this.datos()?.rol === 'ADMIN');
  readonly esCliente = computed(() => this.datos()?.rol === 'CLIENTE');
  readonly rol = computed(() => this.datos()?.rol ?? null);
  readonly correo = computed(() => this.datos()?.correo ?? null);

  obtenerToken(): string | null {
    return this.datos()?.token ?? null;
  }

  iniciarSesion(datos: DatosDeSesion): void {
    const sesion: DatosDeSesion = { token: datos.token, rol: datos.rol, correo: datos.correo };
    this.datos.set(sesion);
    if (this.esNavegador) {
      sessionStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion));
    }
  }

  cerrarSesion(): void {
    this.datos.set(null);
    if (this.esNavegador) {
      sessionStorage.removeItem(CLAVE_STORAGE);
    }
  }

  private leerSesionAlmacenada(): DatosDeSesion | null {
    if (!this.esNavegador) {
      return null;
    }
    const almacenado = sessionStorage.getItem(CLAVE_STORAGE);
    if (almacenado === null) {
      return null;
    }
    try {
      const sesion = JSON.parse(almacenado) as DatosDeSesion;
      return sesion.token && sesion.rol && sesion.correo ? sesion : null;
    } catch {
      return null;
    }
  }
}
