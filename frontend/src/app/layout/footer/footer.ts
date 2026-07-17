import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';

import { EMPRESA } from '../../../contenido/empresa';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { mensajeWhatsappParaRuta } from '../mensaje-whatsapp-por-ruta';

@Component({
  selector: 'app-footer',
  imports: [RouterLink, WhatsappCta],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  private readonly router = inject(Router);
  private readonly urlActual = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  protected readonly empresa = EMPRESA;
  protected readonly mensajeWhatsapp = computed(() => mensajeWhatsappParaRuta(this.urlActual()));
  protected readonly anioActual = new Date().getFullYear();
}
