import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BENEFICIOS_CUENTA } from '../../../contenido/cuenta';
import { HOME, TESTIMONIOS } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { TarjetaServicio } from '../../componentes/tarjeta-servicio/tarjeta-servicio';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

@Component({
  selector: 'app-pagina-home',
  imports: [RouterLink, TarjetaServicio, WhatsappCta, AparecerAlVer],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomePage {
  protected readonly home = HOME;
  protected readonly servicios = SERVICIOS;
  protected readonly testimonios = TESTIMONIOS;
  protected readonly beneficios = BENEFICIOS_CUENTA;

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_HOME, ruta: '/' }));
  }
}
