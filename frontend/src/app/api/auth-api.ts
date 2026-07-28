import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { RolDeSesion } from '../nucleo/sesion';

export interface SesionAutenticada {
  token: string;
  expiraEn: string;
  rol: RolDeSesion;
  correo: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(correo: string, contrasena: string) {
    return this.http.post<SesionAutenticada>('/api/auth/login', { correo, contrasena });
  }

  registrar(correo: string, contrasena: string) {
    return this.http.post<void>('/api/auth/registro', { correo, contrasena });
  }

  verificarCorreo(token: string) {
    return this.http.post<void>('/api/auth/verificacion', { token });
  }

  reenviarVerificacion(correo: string) {
    return this.http.post<void>('/api/auth/reenvio-verificacion', { correo });
  }

  solicitarRecuperacion(correo: string) {
    return this.http.post<void>('/api/auth/recuperacion', { correo });
  }

  restablecerContrasena(token: string, contrasena: string) {
    return this.http.post<void>('/api/auth/restablecimiento', { token, contrasena });
  }
}
