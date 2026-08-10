import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { HERRAMIENTAS } from '../../../contenido/herramientas';
import { METADATOS_HERRAMIENTAS } from '../../../contenido/metadatos-paginas';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { Cotizador } from '../../componentes/cotizador/cotizador';
import { DiagnosticoDigital } from '../../componentes/diagnostico-digital/diagnostico-digital';
import { SimuladorChatbot } from '../../componentes/simulador-chatbot/simulador-chatbot';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

/**
 * Centro de herramientas VIVO (F10a, HU-43, decisión 11 de docs/10):
 * las herramientas se usan aquí mismo; las que faltan aparecen con su
 * badge "Muy pronto" y se activan en las sub-fases F10b-F10d.
 */
@Component({
  selector: 'app-pagina-herramientas',
  imports: [RouterLink, AparecerAlVer, Cotizador, DiagnosticoDigital, SimuladorChatbot],
  templateUrl: './herramientas.html',
  styleUrl: './herramientas.scss',
})
export class HerramientasPage {
  protected readonly datos = HERRAMIENTAS;

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_HERRAMIENTAS, ruta: '/herramientas' }));
  }
}
