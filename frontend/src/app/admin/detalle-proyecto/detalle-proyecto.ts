import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { RouterLink } from '@angular/router';

import { PROYECTOS } from '../../../contenido/proyectos';
import {
  Entregable,
  EstadoEntregable,
  Fase,
  MedioDePago,
  MomentoDeCobro,
  Proyecto,
  ProyectosApi,
  TRANSICIONES_DEL_EQUIPO,
  entregablesDe,
} from '../../api/proyectos-api';
import { BarraDeAvance } from '../../componentes/barra-de-avance/barra-de-avance';
import { EstadoDeEntregable } from '../../componentes/estado-de-entregable/estado-de-entregable';
import { formatearFecha, formatearFechaDeInstante, formatearPesos, hoyEnColombia } from '../../nucleo/formato';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

type Confirmacion = 'pausa' | 'cierre';

/**
 * Gestión de un proyecto (F12, HU-53 a HU-56). Cada acción va al
 * servidor y la pantalla se redibuja con el proyecto que devuelve, ya
 * recalculado: aquí no se calcula dinero ni se decide qué transición es
 * válida —eso lo hace el dominio—; solo se evita ofrecer botones
 * inútiles. Reordenar se hace con botones, no arrastrando: con teclado
 * tiene que funcionar igual.
 */
@Component({
  selector: 'app-pagina-detalle-proyecto-admin',
  templateUrl: './detalle-proyecto.html',
  styleUrl: './detalle-proyecto.scss',
  imports: [RouterLink, CerrarSesionButton, BarraDeAvance, EstadoDeEntregable],
})
export class DetalleProyectoAdminPage implements OnInit {
  private readonly api = inject(ProyectosApi);

  readonly id = input.required<string>();

  protected readonly t = PROYECTOS.panel;
  protected readonly acciones = PROYECTOS.accionesDelEquipo;
  protected readonly estadosDelProyecto = PROYECTOS.estadosDelProyecto;
  protected readonly medios = PROYECTOS.mediosDePago;
  protected readonly listaDeMedios = Object.keys(PROYECTOS.mediosDePago) as MedioDePago[];
  protected readonly pesos = formatearPesos;
  protected readonly fecha = formatearFecha;
  protected readonly fechaDeInstante = formatearFechaDeInstante;

  protected readonly proyecto = signal<Proyecto | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorDeCarga = signal(false);
  protected readonly mensaje = signal<string | null>(null);
  protected readonly ocupado = signal(false);

  protected readonly confirmando = signal<Confirmacion | null>(null);
  protected readonly editando = signal<string | null>(null);
  protected readonly ajustesDe = signal<string | null>(null);
  protected readonly notaDeAjustes = signal('');
  protected readonly notaVacia = signal(false);

  // Nueva entrega
  protected readonly nuevaFase = signal<string | null>(null);
  protected readonly nuevoNombre = signal('');
  protected readonly nuevoValor = signal(0);
  protected readonly nuevoCobro = signal<MomentoDeCobro>('AL_APROBAR');
  protected readonly nuevoCambioDeAlcance = signal(false);

  // Pago
  protected readonly pagoEntregable = signal<string | null>(null);
  protected readonly pagoMonto = signal(0);
  protected readonly pagoFecha = signal(hoyEnColombia());
  protected readonly pagoMedio = signal<MedioDePago>('TRANSFERENCIA');
  protected readonly pagoReferencia = signal('');

  protected readonly entregables = computed(() => {
    const proyecto = this.proyecto();
    return proyecto ? entregablesDe(proyecto) : [];
  });

  /**
   * Decisión 22: en un proyecto de cotización, o en uno en garantía, lo
   * nuevo es un cambio de alcance. El servidor lo exige; aquí la casilla
   * queda marcada y bloqueada para que no haya sorpresa.
   */
  protected readonly cambioDeAlcanceObligatorio = computed(() => {
    const proyecto = this.proyecto();
    return !!proyecto && (proyecto.origenCotizacionId !== null || proyecto.estado === 'EN_GARANTIA');
  });

  protected readonly puedeMoverEntregas = computed(() => this.proyecto()?.estado === 'ACTIVO');
  protected readonly cerrado = computed(() => this.proyecto()?.estado === 'CERRADO');

  ngOnInit(): void {
    this.api.obtener(this.id()).subscribe({
      next: (proyecto) => {
        this.aplicar(proyecto);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.errorDeCarga.set(true);
      },
    });
  }

  protected valorDe(evento: Event): string {
    return (evento.target as HTMLInputElement).value;
  }

  protected transicionesDe(entregable: Entregable): EstadoEntregable[] {
    return TRANSICIONES_DEL_EQUIPO[entregable.estado];
  }

  /** Lo que vuelve de ajustes ya se había empezado: se retoma. */
  protected textoDeAccion(entregable: Entregable, destino: EstadoEntregable): string {
    return entregable.estado === 'CON_AJUSTES' && destino === 'EN_CURSO' ? this.t.retomar : this.acciones[destino];
  }

  // --- Estado del proyecto ---

  protected pedirConfirmacion(cual: Confirmacion): void {
    this.confirmando.set(cual);
  }

  protected confirmar(): void {
    const cual = this.confirmando();
    this.confirmando.set(null);
    if (cual === 'pausa') {
      this.ejecutar(this.api.pausar(this.id()));
    } else if (cual === 'cierre') {
      this.ejecutar(this.api.cerrar(this.id()));
    }
  }

  protected reanudar(): void {
    this.ejecutar(this.api.reanudar(this.id()));
  }

  // --- Descripción y fases ---

  protected guardarDescripcion(nombre: string, descripcion: string, inicio: string, entrega: string): void {
    this.ejecutar(
      this.api.cambiarDescripcion(this.id(), {
        nombre: nombre.trim(),
        descripcion: descripcion.trim() || null,
        inicio,
        entregaEstimada: entrega || null,
      }),
    );
  }

  protected agregarFase(nombre: string): void {
    if (!nombre.trim()) {
      return;
    }
    this.ejecutar(this.api.agregarFase(this.id(), { nombre: nombre.trim(), objetivo: null, inicioPlaneado: null, finPlaneado: null }));
  }

  protected guardarFase(fase: Fase, nombre: string, objetivo: string, inicio: string, fin: string): void {
    this.ejecutar(
      this.api.editarFase(this.id(), fase.id, {
        nombre: nombre.trim(),
        objetivo: objetivo.trim() || null,
        inicioPlaneado: inicio || null,
        finPlaneado: fin || null,
      }),
    );
  }

  protected moverFase(fase: Fase, posicion: number): void {
    this.ejecutar(this.api.moverFase(this.id(), fase.id, posicion));
  }

  protected quitarFase(fase: Fase): void {
    this.ejecutar(this.api.quitarFase(this.id(), fase.id));
  }

  protected guardarResumen(fase: Fase, resumen: string): void {
    this.ejecutar(this.api.escribirResumen(this.id(), fase.id, resumen.trim()));
  }

  // --- Entregas ---

  protected cambiarEstado(entregable: Entregable, destino: EstadoEntregable): void {
    if (destino === 'CON_AJUSTES') {
      this.notaDeAjustes.set('');
      this.notaVacia.set(false);
      this.ajustesDe.set(entregable.id);
      return;
    }
    this.ejecutar(this.api.cambiarEstado(this.id(), entregable.id, destino, null));
  }

  protected escribirNota(evento: Event): void {
    this.notaDeAjustes.set(this.valorDe(evento));
    this.notaVacia.set(false);
  }

  protected devolverConAjustes(entregable: Entregable): void {
    const nota = this.notaDeAjustes().trim();
    if (!nota) {
      this.notaVacia.set(true);
      return;
    }
    this.ejecutar(this.api.cambiarEstado(this.id(), entregable.id, 'CON_AJUSTES', nota));
  }

  protected guardarEntregable(entregable: Entregable, nombre: string, descripcion: string, valor: string, cobro: string): void {
    this.ejecutar(
      this.api.editarEntregable(this.id(), entregable.id, {
        nombre: nombre.trim(),
        descripcion: descripcion.trim() || null,
        valor: Number(valor) || 0,
        momentoDeCobro: cobro as MomentoDeCobro,
      }),
    );
  }

  protected guardarDemo(entregable: Entregable, url: string): void {
    this.ejecutar(this.api.ponerDemo(this.id(), entregable.id, url.trim() || null));
  }

  protected moverEntregable(entregable: Entregable, fase: string, posicion: number): void {
    this.ejecutar(this.api.moverEntregable(this.id(), entregable.id, fase, posicion));
  }

  protected quitarEntregable(entregable: Entregable): void {
    this.ejecutar(this.api.quitarEntregable(this.id(), entregable.id));
  }

  protected agregarEntregable(): void {
    const fase = this.nuevaFase();
    if (!fase || !this.nuevoNombre().trim()) {
      return;
    }
    this.ejecutar(
      this.api.agregarEntregable(
        this.id(),
        fase,
        { nombre: this.nuevoNombre().trim(), descripcion: null, valor: this.nuevoValor(), momentoDeCobro: this.nuevoCobro() },
        this.cambioDeAlcanceObligatorio() || this.nuevoCambioDeAlcance(),
      ),
      () => {
        this.nuevoNombre.set('');
        this.nuevoValor.set(0);
      },
    );
  }

  // --- Pagos ---

  protected registrarPago(): void {
    const entregable = this.pagoEntregable();
    if (!entregable) {
      return;
    }
    this.ejecutar(
      this.api.registrarPago(this.id(), {
        entregableId: entregable,
        monto: this.pagoMonto(),
        fecha: this.pagoFecha(),
        medio: this.pagoMedio(),
        referencia: this.pagoReferencia().trim() || null,
      }),
      () => {
        this.pagoMonto.set(0);
        this.pagoReferencia.set('');
      },
    );
  }

  /** Manda el cambio, y con la respuesta redibuja todo; si falla, muestra el mensaje del servidor. */
  private ejecutar(peticion: Observable<Proyecto>, alTerminar?: () => void): void {
    if (this.ocupado()) {
      return;
    }
    this.ocupado.set(true);
    this.mensaje.set(null);
    peticion.subscribe({
      next: (proyecto) => {
        this.aplicar(proyecto);
        this.editando.set(null);
        this.ajustesDe.set(null);
        this.ocupado.set(false);
        alTerminar?.();
      },
      error: (respuesta: HttpErrorResponse) => {
        this.ocupado.set(false);
        this.mensaje.set((respuesta.error as { mensaje?: string } | null)?.mensaje ?? this.t.error);
      },
    });
  }

  private aplicar(proyecto: Proyecto): void {
    this.proyecto.set(proyecto);
    const fases = proyecto.fases.map((fase) => fase.id);
    if (!this.nuevaFase() || !fases.includes(this.nuevaFase()!)) {
      this.nuevaFase.set(fases[0] ?? null);
    }
    // Primero lo que ya se puede cobrar y no está pagado; si no hay,
    // cualquier cosa con saldo (un adelanto); si no, la primera.
    // Preseleccionar algo ya pagado llevaba a registrar el pago donde no era.
    const todas = entregablesDe(proyecto);
    const conSaldo = (entregable: Entregable) => entregable.cobro - entregable.pagado > 0;
    const sugerida =
      todas.find((entregable) => entregable.esCobrable && conSaldo(entregable)) ??
      todas.find(conSaldo) ??
      todas[0];
    const actual = todas.find((entregable) => entregable.id === this.pagoEntregable());
    if (!actual || !conSaldo(actual)) {
      this.pagoEntregable.set(sugerida?.id ?? null);
    }
  }
}
