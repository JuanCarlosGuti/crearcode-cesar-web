import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { PROYECTOS } from '../../../contenido/proyectos';
import { ProyectosApi, ResumenDeProyecto } from '../../api/proyectos-api';
import { formatearPesos } from '../../nucleo/formato';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

/** Los proyectos del equipo (F12, HU-53 a HU-56). Sin SSR, como todo `admin/**`. */
@Component({
  selector: 'app-pagina-listado-proyectos',
  templateUrl: './listado-proyectos.html',
  styleUrl: '../listado-cotizaciones/listado-cotizaciones.scss',
  imports: [RouterLink, CerrarSesionButton],
})
export class ListadoProyectosPage implements OnInit {
  private readonly api = inject(ProyectosApi);

  protected readonly t = PROYECTOS.panel;
  protected readonly estados = PROYECTOS.estadosDelProyecto;
  protected readonly pesos = formatearPesos;

  protected readonly proyectos = signal<ResumenDeProyecto[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal(false);

  ngOnInit(): void {
    this.api.listar().subscribe({
      next: (proyectos) => {
        this.proyectos.set(proyectos);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.error.set(true);
      },
    });
  }
}
