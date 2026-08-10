import { Component, afterNextRender, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { CUENTA } from '../../../contenido/cuenta';
import { EMPRESA } from '../../../contenido/empresa';
import { SERVICIOS } from '../../../contenido/servicios';
import { SesionService } from '../../nucleo/sesion';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  protected readonly servicios = SERVICIOS;
  protected readonly nombreEmpresa = EMPRESA.razonSocial;
  protected readonly menuAbierto = signal(false);

  protected readonly sesion = inject(SesionService);
  protected readonly textosCuenta = CUENTA.header;

  // El enlace de cuenta depende de sessionStorage, que no existe en el
  // servidor: se muestra solo despues de hidratar para que el HTML
  // prerenderizado y el del cliente coincidan (sin errores de
  // hidratacion en las 14 rutas publicas).
  protected readonly hidratado = signal(false);

  // Los admins no tienen /mi-cuenta (es de clientes): su enlace de
  // sesion los lleva directo a su panel.
  protected readonly enlaceCuenta = computed(() => (this.sesion.esAdmin() ? '/admin' : '/mi-cuenta'));

  constructor() {
    afterNextRender(() => this.hidratado.set(true));
  }

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
