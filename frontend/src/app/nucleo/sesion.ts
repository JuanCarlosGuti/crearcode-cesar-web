import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';

const CLAVE_STORAGE = 'crearcode-admin-token';

@Injectable({ providedIn: 'root' })
export class SesionService {
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly token = signal<string | null>(this.leerTokenAlmacenado());

  readonly estaAutenticado = computed(() => this.token() !== null);

  obtenerToken(): string | null {
    return this.token();
  }

  iniciarSesion(token: string): void {
    this.token.set(token);
    if (this.esNavegador) {
      sessionStorage.setItem(CLAVE_STORAGE, token);
    }
  }

  cerrarSesion(): void {
    this.token.set(null);
    if (this.esNavegador) {
      sessionStorage.removeItem(CLAVE_STORAGE);
    }
  }

  private leerTokenAlmacenado(): string | null {
    return this.esNavegador ? sessionStorage.getItem(CLAVE_STORAGE) : null;
  }
}
