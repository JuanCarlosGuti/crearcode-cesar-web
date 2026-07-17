import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Servicio } from '../../../contenido/tipos';

@Component({
  selector: 'app-tarjeta-servicio',
  imports: [RouterLink],
  templateUrl: './tarjeta-servicio.html',
  styleUrl: './tarjeta-servicio.scss',
})
export class TarjetaServicio {
  readonly servicio = input.required<Servicio>();
}
