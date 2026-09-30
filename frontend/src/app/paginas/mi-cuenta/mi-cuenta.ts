import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { CUENTA } from '../../../contenido/cuenta';
import { MiCuentaApi } from '../../api/mi-cuenta-api';
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
  private readonly api = inject(MiCuentaApi);

  protected readonly sesion = inject(SesionService);
  protected readonly textos = CUENTA.miCuenta;

  // Confirmacion en dos pasos, en la propia pagina: un window.confirm
  // no se puede estilar, no siempre se lee y el navegador puede
  // suprimirlo. Borrar la cuenta no se deshace.
  protected readonly confirmando = signal(false);
  protected readonly eliminando = signal(false);
  protected readonly errorAlEliminar = signal(false);

  protected cerrarSesion(): void {
    this.sesion.cerrarSesion();
    this.router.navigateByUrl('/');
  }

  protected pedirConfirmacion(): void {
    this.errorAlEliminar.set(false);
    this.confirmando.set(true);
  }

  protected cancelarEliminacion(): void {
    this.confirmando.set(false);
  }

  protected eliminarCuenta(): void {
    if (this.eliminando()) {
      return;
    }
    this.eliminando.set(true);
    this.errorAlEliminar.set(false);
    this.api.eliminar().subscribe({
      next: () => {
        // La sesion apunta a una cuenta que ya no existe: cerrarla aqui
        // evita que la siguiente peticion salga con un token huerfano.
        this.sesion.cerrarSesion();
        this.router.navigateByUrl('/');
      },
      error: () => {
        this.eliminando.set(false);
        this.errorAlEliminar.set(true);
      },
    });
  }
}
