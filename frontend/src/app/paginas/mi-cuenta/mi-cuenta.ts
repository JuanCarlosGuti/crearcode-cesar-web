import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { CUENTA } from '../../../contenido/cuenta';
import { PROYECTOS } from '../../../contenido/proyectos';
import { MiCuentaApi } from '../../api/mi-cuenta-api';
import { MisProyectosApi, Proyecto, entregablesDe } from '../../api/proyectos-api';
import { BarraDeAvance } from '../../componentes/barra-de-avance/barra-de-avance';
import { formatearPesos } from '../../nucleo/formato';
import { SesionService } from '../../nucleo/sesion';

/**
 * La cuenta del cliente. Desde F12, si tiene proyectos, van primero:
 * es lo que vino a ver (HU-49). Debajo sigue lo de F8 y F11 —sus
 * cotizaciones, cerrar sesión, eliminar la cuenta—. El cambio de
 * contraseña lo cubre el flujo de recuperación (docs/08). Protegida por
 * clienteGuard y sin SSR (RenderMode.Client).
 */
@Component({
  selector: 'app-pagina-mi-cuenta',
  templateUrl: './mi-cuenta.html',
  styleUrl: './mi-cuenta.scss',
  imports: [RouterLink, BarraDeAvance],
})
export class MiCuentaPage implements OnInit {
  private readonly router = inject(Router);
  private readonly api = inject(MiCuentaApi);
  private readonly proyectosApi = inject(MisProyectosApi);

  protected readonly sesion = inject(SesionService);
  protected readonly textos = CUENTA.miCuenta;
  protected readonly textosDeProyectos = PROYECTOS.cuenta;
  protected readonly estadosDelProyecto = PROYECTOS.estadosDelProyecto;
  protected readonly pesos = formatearPesos;

  protected readonly proyectos = signal<Proyecto[]>([]);
  protected readonly errorDeProyectos = signal(false);

  // Confirmacion en dos pasos, en la propia pagina: un window.confirm
  // no se puede estilar, no siempre se lee y el navegador puede
  // suprimirlo. Borrar la cuenta no se deshace.
  protected readonly confirmando = signal(false);
  protected readonly eliminando = signal(false);
  protected readonly errorAlEliminar = signal(false);

  ngOnInit(): void {
    this.proyectosApi.listar().subscribe({
      next: (proyectos) => this.proyectos.set(proyectos),
      // Sin proyectos visibles la cuenta sigue sirviendo: no se bloquea.
      error: () => this.errorDeProyectos.set(true),
    });
  }

  protected textoParaRevisar(proyecto: Proyecto): string | null {
    const cuantos = entregablesDe(proyecto).filter((entregable) => entregable.estado === 'EN_REVISION').length;
    if (cuantos === 0) {
      return null;
    }
    return cuantos === 1
      ? this.textosDeProyectos.paraRevisarUno
      : this.textosDeProyectos.paraRevisarVarios.replace('{n}', String(cuantos));
  }

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
