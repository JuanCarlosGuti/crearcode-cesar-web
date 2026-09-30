import { Component, input } from '@angular/core';

import { DocumentoLegal } from '../../../contenido/tipos';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

@Component({
  selector: 'app-pagina-legal',
  templateUrl: './legal.html',
  styleUrl: './legal.scss',
})
export class LegalPage {
  readonly documento = input.required<DocumentoLegal>();
  readonly ruta = input.required<string>();

  constructor() {
    establecerMetadatosDePagina(() => ({
      titulo: `${this.documento().titulo} — Crear Code Cesar`,
      descripcion: this.documento().metaDescripcion,
      ruta: this.ruta(),
    }));
  }
}
