import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { HOME, TESTIMONIOS } from '../../../contenido/home';
import { SERVICIOS } from '../../../contenido/servicios';
import { TarjetaServicio } from '../../componentes/tarjeta-servicio/tarjeta-servicio';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';

@Component({
  selector: 'app-pagina-home',
  imports: [RouterLink, TarjetaServicio, WhatsappCta],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomePage {
  protected readonly home = HOME;
  protected readonly servicios = SERVICIOS;
  protected readonly testimonios = TESTIMONIOS;
}
