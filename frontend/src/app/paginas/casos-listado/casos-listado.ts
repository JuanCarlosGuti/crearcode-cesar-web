import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CASOS } from '../../../contenido/casos';
import { METADATOS_CASOS_LISTADO } from '../../../contenido/metadatos-paginas';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

@Component({
  selector: 'app-pagina-casos-listado',
  imports: [RouterLink, AparecerAlVer],
  templateUrl: './casos-listado.html',
  styleUrl: './casos-listado.scss',
})
export class CasosListadoPage {
  protected readonly casos = CASOS;

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_CASOS_LISTADO, ruta: '/casos' }));
  }
}
