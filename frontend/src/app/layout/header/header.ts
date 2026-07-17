import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';

import { EMPRESA } from '../../../contenido/empresa';
import { HOME } from '../../../contenido/home';
import { SERVICIOS } from '../../../contenido/servicios';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
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

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
