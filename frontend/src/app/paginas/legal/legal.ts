import { Component, input } from '@angular/core';

import { DocumentoLegal } from '../../../contenido/tipos';

@Component({
  selector: 'app-pagina-legal',
  templateUrl: './legal.html',
  styleUrl: './legal.scss',
})
export class LegalPage {
  readonly documento = input.required<DocumentoLegal>();
}
