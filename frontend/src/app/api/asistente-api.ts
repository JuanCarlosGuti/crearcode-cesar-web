import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export type RolMensaje = 'USUARIO' | 'ASISTENTE';

export interface MensajeEnviado {
  rol: RolMensaje;
  texto: string;
}

export interface RespuestaAsistente {
  texto: string;
  escalarAHumano: boolean;
}

@Injectable({ providedIn: 'root' })
export class AsistenteApi {
  private readonly http = inject(HttpClient);

  /**
   * El interceptor agrega el Bearer si hay sesión (identidad
   * registrada, límite mayor); el header de sesión anónima viaja
   * siempre — el backend lo ignora cuando hay token.
   */
  enviar(mensajes: MensajeEnviado[], idSesionAnonima: string) {
    return this.http.post<RespuestaAsistente>(
      '/api/asistente/mensajes',
      { mensajes },
      { headers: { 'X-Sesion-Anonima': idSesionAnonima } },
    );
  }
}
