import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { EMPRESA } from '../../../contenido/empresa';
import { HOME } from '../../../contenido/home';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';

@Component({
  selector: 'app-footer',
  imports: [RouterLink, WhatsappCta],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
  protected readonly empresa = EMPRESA;
  protected readonly mensajeWhatsapp = HOME.mensajeWhatsapp;
  protected readonly anioActual = new Date().getFullYear();
}
