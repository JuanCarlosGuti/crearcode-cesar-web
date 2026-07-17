import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface SesionAutenticada {
  token: string;
  expiraEn: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(correo: string, contrasena: string) {
    return this.http.post<SesionAutenticada>('/api/auth/login', { correo, contrasena });
  }
}
