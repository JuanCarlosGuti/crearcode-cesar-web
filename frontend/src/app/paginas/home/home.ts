import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ASISTENTE } from '../../../contenido/asistente';
import { BENEFICIOS_CUENTA, TABLA_CUENTA } from '../../../contenido/cuenta';
import { HOME } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { TarjetaServicio } from '../../componentes/tarjeta-servicio/tarjeta-servicio';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { AsistenteUiService } from '../../nucleo/asistente-ui';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

/**
 * Home rediseñada (F10e, ISS-133 — prototipo aprobado): hero con la
 * tarjeta del demo, sección de herramientas, asistente con preguntas
 * sugeridas, tabla visitante vs. cuenta y placeholders honestos en vez
 * de testimonios ficticios.
 */
@Component({
  selector: 'app-pagina-home',
  imports: [RouterLink, TarjetaServicio, WhatsappCta, AparecerAlVer],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomePage {
  private readonly asistenteUi = inject(AsistenteUiService);

  protected readonly home = HOME;
  protected readonly servicios = SERVICIOS;
  protected readonly beneficios = BENEFICIOS_CUENTA;
  protected readonly tablaCuenta = TABLA_CUENTA;
  protected readonly sugerencias = ASISTENTE.sugerencias;

  // El titular se parte en la ultima coma para resaltar el remate
  // ("no al reves.") en el color de acento. El texto completo del <h1>
  // no cambia: se concatena igual, sin espacios extra.
  protected readonly titularInicio = HOME.headline.slice(0, HOME.headline.lastIndexOf(',') + 2);
  protected readonly titularResaltado = HOME.headline.slice(HOME.headline.lastIndexOf(',') + 2);

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_HOME, ruta: '/' }));
  }

  protected preguntarAlAsistente(pregunta: string): void {
    this.asistenteUi.abrir(pregunta);
  }
}
