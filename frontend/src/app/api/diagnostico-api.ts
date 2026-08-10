import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface ParDeDiagnostico {
  pregunta: string;
  respuesta: string;
}

export interface OportunidadDeAutomatizacion {
  titulo: string;
  detalle: string;
  beneficio: string;
}

export interface InformeDeDiagnostico {
  veredicto: string;
  oportunidades: OportunidadDeAutomatizacion[];
}

@Injectable({ providedIn: 'root' })
export class DiagnosticoApi {
  private readonly http = inject(HttpClient);

  generar(respuestas: ParDeDiagnostico[], idSesionAnonima: string) {
    return this.http.post<InformeDeDiagnostico>(
      '/api/asistente/diagnostico',
      { respuestas },
      { headers: { 'X-Sesion-Anonima': idSesionAnonima } },
    );
  }
}
