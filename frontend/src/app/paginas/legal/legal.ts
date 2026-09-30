import { Component, input } from '@angular/core';

import { VIGENTE_DESDE } from '../../../contenido/legales';
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

  // La propia politica dice que la fecha de la version vigente se
  // indica al pie; hasta la auditoria del 28 sep 2026 no se indicaba.
  protected readonly vigenteDesde = VIGENTE_DESDE;

  constructor() {
    establecerMetadatosDePagina(() => ({
      titulo: `${this.documento().titulo} — Crear Code Cesar`,
      descripcion: this.documento().metaDescripcion,
      ruta: this.ruta(),
    }));
  }
}
