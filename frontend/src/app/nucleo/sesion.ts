import { isPlatformBrowser } from '@angular/common';
import { Injectable, OnDestroy, PLATFORM_ID, computed, inject, signal } from '@angular/core';

const CLAVE_STORAGE = 'crearcode-sesion';

export type RolDeSesion = 'ADMIN' | 'CLIENTE';

export interface DatosDeSesion {
  token: string;
  rol: RolDeSesion;
  correo: string;
}

/**
 * Lee el `exp` del JWT sin verificar la firma.
 *
 * <p>
 * No es un control de seguridad y no pretende serlo: quien manda es el
 * servidor, que rechaza cualquier token vencido. Esto es para que la
 * interfaz no mienta — hasta la auditoría del 28 sep 2026 una pestaña
 * abierta con el token ya vencido seguía pintando «Mi cuenta», pasaba
 * los guards y mandaba el Bearer muerto, y el visitante solo se
 * enteraba cuando algo fallaba con un 401.
 */
function vencimientoDelToken(token: string): number | null {
  const partes = token.split('.');
  if (partes.length !== 3) {
    return null;
  }
  try {
    const carga = JSON.parse(atob(partes[1].replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
    return typeof carga.exp === 'number' ? carga.exp * 1000 : null;
  } catch {
    // Un token que no se puede leer se trata como vencido: es el lado
    // seguro, y de todas formas el servidor lo rechazaría.
    return 0;
  }
}

/**
 * Sesión en sessionStorage: se pierde al cerrar la pestaña, sin
 * revocación en el servidor antes de que expire el JWT (trade-off
 * consciente de v1, ver ADR-08). Desde F8 guarda también rol y correo
 * para distinguir el panel admin de la cuenta de cliente.
 *
 * <p>
 * La sesión se cierra sola al vencer el token: al restaurarla, al
 * pedirlo y con un temporizador programado para el instante exacto.
 * Lo que había antes era solo reactivo —el interceptor limpiaba al
 * recibir un 401—, así que entre el vencimiento y la siguiente
 * petición la interfaz aseguraba una sesión que ya no existía.
 */
@Injectable({ providedIn: 'root' })
export class SesionService implements OnDestroy {
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly datos = signal<DatosDeSesion | null>(this.leerSesionAlmacenada());
  private cierreProgramado: ReturnType<typeof setTimeout> | null = null;

  readonly estaAutenticado = computed(() => this.datos() !== null);
  readonly esAdmin = computed(() => this.datos()?.rol === 'ADMIN');
  readonly esCliente = computed(() => this.datos()?.rol === 'CLIENTE');
  readonly rol = computed(() => this.datos()?.rol ?? null);
  readonly correo = computed(() => this.datos()?.correo ?? null);

  constructor() {
    this.programarCierre();
  }

  ngOnDestroy(): void {
    this.cancelarCierre();
  }

  obtenerToken(): string | null {
    const sesion = this.datos();
    if (sesion === null) {
      return null;
    }
    if (this.estaVencida(sesion)) {
      this.cerrarSesion();
      return null;
    }
    return sesion.token;
  }

  iniciarSesion(datos: DatosDeSesion): void {
    const sesion: DatosDeSesion = { token: datos.token, rol: datos.rol, correo: datos.correo };
    this.datos.set(sesion);
    if (this.esNavegador) {
      sessionStorage.setItem(CLAVE_STORAGE, JSON.stringify(sesion));
    }
    this.programarCierre();
  }

  cerrarSesion(): void {
    this.cancelarCierre();
    this.datos.set(null);
    if (this.esNavegador) {
      sessionStorage.removeItem(CLAVE_STORAGE);
    }
  }

  private estaVencida(sesion: DatosDeSesion): boolean {
    const vence = vencimientoDelToken(sesion.token);
    return vence !== null && vence <= Date.now();
  }

  /**
   * Cierra la sesión en el instante en que el token deja de valer, sin
   * esperar a que el visitante haga algo. Si falta más de lo que cabe
   * en un setTimeout (~24,8 días) no se programa: el JWT dura 8 horas,
   * así que eso solo pasaría con un token absurdo.
   */
  private programarCierre(): void {
    this.cancelarCierre();
    const sesion = this.datos();
    if (!this.esNavegador || sesion === null) {
      return;
    }
    const vence = vencimientoDelToken(sesion.token);
    if (vence === null) {
      return;
    }
    const restante = vence - Date.now();
    if (restante <= 0) {
      this.cerrarSesion();
      return;
    }
    if (restante > 2_147_483_647) {
      return;
    }
    this.cierreProgramado = setTimeout(() => this.cerrarSesion(), restante);
  }

  private cancelarCierre(): void {
    if (this.cierreProgramado !== null) {
      clearTimeout(this.cierreProgramado);
      this.cierreProgramado = null;
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
      if (!sesion.token || !sesion.rol || !sesion.correo) {
        return null;
      }
      if (this.estaVencida(sesion)) {
        sessionStorage.removeItem(CLAVE_STORAGE);
        return null;
      }
      return sesion;
    } catch {
      return null;
    }
  }
}
