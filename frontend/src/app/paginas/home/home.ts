import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { ConservarLoEscrito } from '../../componentes/conservar-lo-escrito/conservar-lo-escrito';
import { ASISTENTE } from '../../../contenido/asistente';
import { BENEFICIOS_CUENTA, TABLA_CUENTA } from '../../../contenido/cuenta';
import { HOME } from '../../../contenido/home';
import { METADATOS_HOME } from '../../../contenido/metadatos-paginas';
import { SERVICIOS } from '../../../contenido/servicios';
import { AparecerAlVer } from '../../componentes/aparecer-al-ver/aparecer-al-ver';
import { TarjetaServicio } from '../../componentes/tarjeta-servicio/tarjeta-servicio';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { SolicitudDeDemo } from '../../api/demo-api';
import { AsistenteUiService } from '../../nucleo/asistente-ui';
import { BocetoPendienteService } from '../../nucleo/boceto-pendiente';
import { SesionService } from '../../nucleo/sesion';
import { sesionTrasHidratar } from '../../nucleo/sesion-tras-hidratar';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

/**
 * Home rediseñada (F10e, ISS-133 — prototipo aprobado): hero con la
 * tarjeta del demo, sección de herramientas, asistente con preguntas
 * sugeridas, tabla visitante vs. cuenta y placeholders honestos en vez
 * de testimonios ficticios.
 */
@Component({
  selector: 'app-pagina-home',
  imports: [RouterLink, TarjetaServicio, WhatsappCta, AparecerAlVer, ConservarLoEscrito],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomePage {
  private readonly asistenteUi = inject(AsistenteUiService);
  private readonly bocetoPendiente = inject(BocetoPendienteService);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  protected readonly home = HOME;
  protected readonly servicios = SERVICIOS;
  protected readonly beneficios = BENEFICIOS_CUENTA;
  protected readonly tablaCuenta = TABLA_CUENTA;
  protected readonly sugerencias = ASISTENTE.sugerencias;
  protected readonly bocetoEscrito = signal<SolicitudDeDemo>({ sector: '', queHace: '', queNecesita: '' });
  // Con cuenta, la sección que invita a crearla sobra (ISS-242).
  protected readonly conSesion = sesionTrasHidratar();

  // El final del titular ("tu negocio.") va en el color de acento. Lo
  // dice el contenido: antes se partia en la ultima coma, y el eslogan
  // corto ya no tiene coma (ISS-229). El texto del <h1> no cambia.
  protected readonly titularResaltado = HOME.headlineResaltado;
  protected readonly titularInicio = HOME.headline.slice(0, HOME.headline.length - HOME.headlineResaltado.length);

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_HOME, ruta: '/' }));
  }

  protected preguntarAlAsistente(pregunta: string): void {
    this.asistenteUi.abrir(pregunta);
  }

  protected escribirEnBoceto(campo: keyof SolicitudDeDemo, evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    this.bocetoEscrito.update((actual) => ({ ...actual, [campo]: valor }));
  }

  /**
   * La tarjeta no genera el boceto aquí: tarda hasta 30 segundos y la
   * Home es lo primero que carga (decisión 31 de docs/10). Deja lo
   * escrito en el navegador y lleva a donde se genera; sin cuenta, a
   * crearla, y el ingreso retoma desde ahí.
   */
  protected verMiBoceto(): void {
    this.bocetoPendiente.guardar(this.bocetoEscrito());
    if (this.sesion.estaAutenticado()) {
      this.router.navigate(['/herramientas'], { fragment: 'demo-diseno' });
    } else {
      this.router.navigate(['/registro']);
    }
  }
}
