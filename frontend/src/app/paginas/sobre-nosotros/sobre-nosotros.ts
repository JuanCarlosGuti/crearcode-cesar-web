import { Component } from '@angular/core';

import { SOBRE_NOSOTROS, VALORES } from '../../../contenido/sobre-nosotros';

@Component({
  selector: 'app-pagina-sobre-nosotros',
  templateUrl: './sobre-nosotros.html',
  styleUrl: './sobre-nosotros.scss',
})
export class SobreNosotrosPage {
  protected readonly datos = SOBRE_NOSOTROS;
  protected readonly valores = VALORES;
}
