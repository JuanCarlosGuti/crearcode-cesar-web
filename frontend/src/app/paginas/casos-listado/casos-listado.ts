import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CASOS } from '../../../contenido/casos';

@Component({
  selector: 'app-pagina-casos-listado',
  imports: [RouterLink],
  templateUrl: './casos-listado.html',
  styleUrl: './casos-listado.scss',
})
export class CasosListadoPage {
  protected readonly casos = CASOS;
}
