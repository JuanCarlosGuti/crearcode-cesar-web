import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { MensajeEnviado, RespuestaAsistente } from './asistente-api';

export interface NegocioSimulado {
  nombre: string;
  rubro: string;
}

@Injectable({ providedIn: 'root' })
export class SimuladorApi {
  private readonly http = inject(HttpClient);

  /**
   * Igual que el asistente (F9): Bearer opcional vía interceptor y
   * sesión anónima en el header — el backend cuenta el cupo del
   * simulador por separado.
   */
  enviar(negocio: NegocioSimulado, mensajes: MensajeEnviado[], idSesionAnonima: string) {
    return this.http.post<RespuestaAsistente>(
      '/api/asistente/simulador',
      { negocio, mensajes },
      { headers: { 'X-Sesion-Anonima': idSesionAnonima } },
    );
  }
}
