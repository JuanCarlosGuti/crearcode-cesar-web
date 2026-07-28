import { Component } from '@angular/core';

import { METADATOS_SOBRE_NOSOTROS } from '../../../contenido/metadatos-paginas';
import { SOBRE_NOSOTROS, VALORES } from '../../../contenido/sobre-nosotros';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

@Component({
  selector: 'app-pagina-sobre-nosotros',
  imports: [AparecerAlVer],
  templateUrl: './sobre-nosotros.html',
  styleUrl: './sobre-nosotros.scss',
})
export class SobreNosotrosPage {
  protected readonly datos = SOBRE_NOSOTROS;
  protected readonly valores = VALORES;

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_SOBRE_NOSOTROS, ruta: '/sobre-nosotros' }));
  }
}
