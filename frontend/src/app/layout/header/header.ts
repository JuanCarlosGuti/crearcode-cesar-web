import { Component, afterNextRender, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';

import { CUENTA } from '../../../contenido/cuenta';
import { EMPRESA } from '../../../contenido/empresa';
import { HOME } from '../../../contenido/home';
import { SERVICIOS } from '../../../contenido/servicios';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { SesionService } from '../../nucleo/sesion';
import { mensajeWhatsappParaRuta } from '../mensaje-whatsapp-por-ruta';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, WhatsappCta],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private readonly router = inject(Router);
  private readonly urlActual = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  protected readonly servicios = SERVICIOS;
  protected readonly nombreEmpresa = EMPRESA.razonSocial;
  protected readonly mensajeWhatsapp = computed(() => mensajeWhatsappParaRuta(this.urlActual()));
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
