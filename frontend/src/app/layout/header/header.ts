import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { EMPRESA } from '../../../contenido/empresa';
import { HOME } from '../../../contenido/home';
import { SERVICIOS } from '../../../contenido/servicios';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, WhatsappCta],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  protected readonly servicios = SERVICIOS;
  protected readonly nombreEmpresa = EMPRESA.razonSocial;
  protected readonly mensajeWhatsapp = HOME.mensajeWhatsapp;
  protected readonly menuAbierto = signal(false);

  alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
