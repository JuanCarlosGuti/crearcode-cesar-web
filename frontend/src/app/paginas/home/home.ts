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

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_HOME, ruta: '/' }));
  }

  protected preguntarAlAsistente(pregunta: string): void {
    this.asistenteUi.abrir(pregunta);
  }
}
