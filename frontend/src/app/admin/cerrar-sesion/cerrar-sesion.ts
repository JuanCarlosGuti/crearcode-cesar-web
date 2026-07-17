import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { SesionService } from '../../nucleo/sesion';

@Component({
  selector: 'app-cerrar-sesion',
  template: `<button type="button" class="boton boton-secundario" (click)="cerrarSesion()">Cerrar sesión</button>`,
})
export class CerrarSesionButton {
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  protected cerrarSesion(): void {
    this.sesion.cerrarSesion();
    this.router.navigateByUrl('/admin/login');
  }
}
