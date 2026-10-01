import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { PROYECTOS } from '../../../contenido/proyectos';
import { Entregable, MisProyectosApi, Proyecto, entregablesDe } from '../../api/proyectos-api';
import { BarraDeAvance } from '../../componentes/barra-de-avance/barra-de-avance';
import { EstadoDeEntregable } from '../../componentes/estado-de-entregable/estado-de-entregable';
import { formatearFecha, formatearFechaDeInstante, formatearPesos } from '../../nucleo/formato';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

interface Respuesta {
  entregable: string;
  tipo: 'aprobar' | 'ajustes';
}

/**
 * El proyecto del cliente (F12, HU-49 a HU-51 y HU-57): avance, plan por
 * fases, lo que le toca revisar y sus pagos. Es también el destino del
 * enlace que llega por correo, así que tiene que funcionar entrando
 * directo. Sin SSR, como toda el área de cuenta (RenderMode.Client).
 *
 * Todo el dinero viene calculado del servidor; aquí solo se muestra.
 */
@Component({
  selector: 'app-pagina-mi-proyecto',
  templateUrl: './mi-proyecto.html',
  styleUrl: './mi-proyecto.scss',
  imports: [RouterLink, BarraDeAvance, EstadoDeEntregable],
})
export class MiProyectoPage implements OnInit {
  private readonly api = inject(MisProyectosApi);

  readonly id = input.required<string>();

  protected readonly t = PROYECTOS.detalle;
  protected readonly estadosDelProyecto = PROYECTOS.estadosDelProyecto;
  protected readonly medios = PROYECTOS.mediosDePago;
  protected readonly pesos = formatearPesos;
  protected readonly fecha = formatearFecha;
  protected readonly fechaDeInstante = formatearFechaDeInstante;

  protected readonly proyecto = signal<Proyecto | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorDeCarga = signal(false);
  protected readonly noEncontrado = signal(false);

  protected readonly respuesta = signal<Respuesta | null>(null);
  protected readonly nota = signal('');
  protected readonly notaVacia = signal(false);
  protected readonly enviando = signal(false);
  protected readonly errorAlResponder = signal(false);

  protected readonly entregables = computed(() => {
    const proyecto = this.proyecto();
    return proyecto ? entregablesDe(proyecto) : [];
  });

  protected readonly paraRevisar = computed(() =>
    this.entregables().filter((entregable) => entregable.estado === 'EN_REVISION'),
  );

  /** Decisión 27: el cliente responde solo con el proyecto en marcha. */
  protected readonly puedeResponder = computed(() => this.proyecto()?.estado === 'ACTIVO');

  constructor() {
    establecerMetadatosDePagina(() => ({
      titulo: 'Mi proyecto — Crear Code Cesar',
      descripcion: 'El avance de tu proyecto, lo que te toca revisar y tus pagos.',
      ruta: '/mi-cuenta',
    }));
  }

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.errorDeCarga.set(false);
    this.noEncontrado.set(false);
    this.api.obtener(this.id()).subscribe({
      next: (proyecto) => {
        this.proyecto.set(proyecto);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        // Ajeno o inexistente se ven igual (404): reintentar no lo arregla.
        if (error.status === 404) {
          this.noEncontrado.set(true);
        } else {
          this.errorDeCarga.set(true);
        }
      },
    });
  }

  protected empezarAprobacion(entregable: Entregable): void {
    this.abrir({ entregable: entregable.id, tipo: 'aprobar' });
  }

  protected empezarAjustes(entregable: Entregable): void {
    this.nota.set('');
    this.abrir({ entregable: entregable.id, tipo: 'ajustes' });
  }

  protected cancelar(): void {
    this.respuesta.set(null);
  }

  protected escribirNota(evento: Event): void {
    this.nota.set((evento.target as HTMLTextAreaElement).value);
    this.notaVacia.set(false);
  }

  protected confirmarAprobacion(entregable: Entregable): void {
    this.enviar(this.api.aprobar(this.id(), entregable.id));
  }

  protected enviarAjustes(entregable: Entregable): void {
    const nota = this.nota().trim();
    if (!nota) {
      this.notaVacia.set(true);
      return;
    }
    this.enviar(this.api.pedirAjustes(this.id(), entregable.id, nota));
  }

  protected respondiendo(entregable: Entregable, tipo: Respuesta['tipo']): boolean {
    const respuesta = this.respuesta();
    return respuesta?.entregable === entregable.id && respuesta.tipo === tipo;
  }

  protected textoDeConfirmacion(entregable: Entregable): string {
    return this.t.confirmarAprobacion.replace('{nombre}', entregable.nombre);
  }

  protected textoDeAprobacion(entregable: Entregable): string {
    const plantilla = entregable.aprobadoPor === 'CLIENTE' ? this.t.aprobadoPorTi : this.t.aprobadoPorEquipo;
    return plantilla.replace('{fecha}', this.fechaDeInstante(entregable.aprobadoEn));
  }

  protected fechasDeFase(inicio: string | null, fin: string | null): string {
    if (!inicio || !fin) {
      return '';
    }
    return this.t.lasFechas.replace('{inicio}', this.fecha(inicio)).replace('{fin}', this.fecha(fin));
  }

  protected nombreDelEntregable(id: string): string {
    return this.entregables().find((entregable) => entregable.id === id)?.nombre ?? '';
  }

  private abrir(respuesta: Respuesta): void {
    this.errorAlResponder.set(false);
    this.notaVacia.set(false);
    this.respuesta.set(respuesta);
  }

  private enviar(peticion: ReturnType<MisProyectosApi['aprobar']>): void {
    if (this.enviando()) {
      return;
    }
    this.enviando.set(true);
    this.errorAlResponder.set(false);
    peticion.subscribe({
      next: (proyecto) => {
        this.proyecto.set(proyecto);
        this.respuesta.set(null);
        this.enviando.set(false);
      },
      // Lo escrito se conserva: la nota sigue en la señal y en el campo.
      error: () => {
        this.enviando.set(false);
        this.errorAlResponder.set(true);
      },
    });
  }
}
