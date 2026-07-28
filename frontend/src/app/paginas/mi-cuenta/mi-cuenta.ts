import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { CUENTA } from '../../../contenido/cuenta';
import { SesionService } from '../../nucleo/sesion';

/**
 * Área mínima del cliente en F8: correo de la sesión y cerrar sesión.
 * El cambio de contraseña lo cubre el flujo de recuperación (docs/08).
 * Protegida por clienteGuard y sin SSR (RenderMode.Client).
 */
@Component({
  selector: 'app-pagina-mi-cuenta',
  templateUrl: './mi-cuenta.html',
  styleUrl: './mi-cuenta.scss',
  imports: [RouterLink],
})
export class MiCuentaPage {
  private readonly router = inject(Router);

  protected readonly sesion = inject(SesionService);
  protected readonly textos = CUENTA.miCuenta;

  protected cerrarSesion(): void {
    this.sesion.cerrarSesion();
    this.router.navigateByUrl('/');
  }
}
