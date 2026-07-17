import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { HOME, TESTIMONIOS } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { TarjetaServicio } from '../../componentes/tarjeta-servicio/tarjeta-servicio';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

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

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_HOME, ruta: '/' }));
  }
}
