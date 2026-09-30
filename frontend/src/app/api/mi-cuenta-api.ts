import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

/**
 * La cuenta del cliente sobre sí misma. El backend toma el correo del
 * token, así que aquí no se manda ninguna identidad: no hay forma de
 * pedir que se borre una cuenta ajena.
 */
@Injectable({ providedIn: 'root' })
export class MiCuentaApi {
  private readonly http = inject(HttpClient);

  eliminar() {
    return this.http.delete<void>('/api/mi-cuenta');
  }
}
