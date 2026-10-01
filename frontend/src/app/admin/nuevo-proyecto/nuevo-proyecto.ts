import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { PROYECTOS } from '../../../contenido/proyectos';
import { Cotizacion, CotizacionesApi } from '../../api/cotizaciones-api';
import { FaseDelPlan, MomentoDeCobro, ProyectosApi } from '../../api/proyectos-api';
import { formatearPesos, hoyEnColombia } from '../../nucleo/formato';
import { CerrarSesionButton } from '../cerrar-sesion/cerrar-sesion';

const IVA_POR_DEFECTO = 19;

/**
 * Crear un proyecto (F12, HU-53): desde una cotización aceptada
 * (`?cotizacion=`), con sus ítems propuestos como entregas, o en blanco.
 * La suma que se muestra es una ayuda para no equivocarse: quien exige
 * que el plan sume lo aceptado es el servidor (invariante 5).
 */
@Component({
  selector: 'app-pagina-nuevo-proyecto',
  templateUrl: './nuevo-proyecto.html',
  styleUrl: './nuevo-proyecto.scss',
  imports: [RouterLink, CerrarSesionButton],
})
export class NuevoProyectoPage implements OnInit {
  private readonly proyectosApi = inject(ProyectosApi);
  private readonly cotizacionesApi = inject(CotizacionesApi);
  private readonly router = inject(Router);

  /** Llega del query param `?cotizacion=`. */
  readonly cotizacion = input<string>();

  protected readonly t = PROYECTOS.panel;
  protected readonly pesos = formatearPesos;

  protected readonly deLaCotizacion = signal<Cotizacion | null>(null);
  protected readonly plan = signal<FaseDelPlan[]>([]);
  protected readonly nombre = signal('');
  protected readonly descripcion = signal('');
  protected readonly inicio = signal(hoyEnColombia());
  protected readonly entregaEstimada = signal('');
  protected readonly clienteNombre = signal('');
  protected readonly clienteCorreo = signal('');
  protected readonly impuesto = signal(IVA_POR_DEFECTO);

  protected readonly cargando = signal(false);
  protected readonly creando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly suma = computed(() =>
    this.plan()
      .flatMap((fase) => fase.entregables)
      .reduce((total, entregable) => total + (Number(entregable.valor) || 0), 0),
  );

  protected readonly diferencia = computed(() => {
    const cotizacion = this.deLaCotizacion();
    return cotizacion ? this.suma() - cotizacion.subtotal : 0;
  });

  protected readonly puedeCrear = computed(
    () => !this.creando() && !this.cargando() && this.diferencia() === 0,
  );

  ngOnInit(): void {
    const cotizacion = this.cotizacion();
    if (!cotizacion) {
      this.plan.set([this.faseVacia(1)]);
      return;
    }
    this.cargando.set(true);
    this.cotizacionesApi.obtener(cotizacion).subscribe({
      next: (encontrada) => this.deLaCotizacion.set(encontrada),
      error: () => this.error.set(this.t.error),
    });
    this.proyectosApi.proponerPlan(cotizacion).subscribe({
      next: (plan) => {
        this.plan.set(plan);
        this.cargando.set(false);
      },
      error: (respuesta: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(this.mensajeDe(respuesta));
      },
    });
  }

  protected textoDeOrigen(cotizacion: Cotizacion): string {
    return this.t.nuevoDesdeCotizacion
      .replace('{numero}', cotizacion.numero ?? '')
      .replace('{cliente}', cotizacion.clienteNombre);
  }

  protected valorDe(evento: Event): string {
    return (evento.target as HTMLInputElement).value;
  }

  // --- Edición del plan (inmutable: cada cambio arma un plan nuevo) ---

  protected cambiarFase(indice: number, campo: 'nombre' | 'objetivo', valor: string): void {
    this.plan.update((plan) =>
      plan.map((fase, i) => (i === indice ? { ...fase, fase: { ...fase.fase, [campo]: valor || null } } : fase)),
    );
  }

  protected cambiarEntregable(
    indiceFase: number,
    indice: number,
    campo: 'nombre' | 'valor' | 'momentoDeCobro',
    valor: string,
  ): void {
    const nuevo = campo === 'valor' ? Number(valor) || 0 : valor;
    this.plan.update((plan) =>
      plan.map((fase, i) =>
        i !== indiceFase
          ? fase
          : {
              ...fase,
              entregables: fase.entregables.map((entregable, j) =>
                j === indice ? { ...entregable, [campo]: nuevo } : entregable,
              ),
            },
      ),
    );
  }

  protected agregarFase(): void {
    this.plan.update((plan) => [...plan, this.faseVacia(plan.length + 1)]);
  }

  protected quitarFase(indice: number): void {
    this.plan.update((plan) => plan.filter((_, i) => i !== indice));
  }

  protected agregarEntregable(indiceFase: number): void {
    const cobro: MomentoDeCobro = this.plan().some((fase) => fase.entregables.length > 0) ? 'AL_APROBAR' : 'AL_INICIAR';
    this.plan.update((plan) =>
      plan.map((fase, i) =>
        i === indiceFase
          ? { ...fase, entregables: [...fase.entregables, { nombre: '', descripcion: null, valor: 0, momentoDeCobro: cobro }] }
          : fase,
      ),
    );
  }

  protected quitarEntregable(indiceFase: number, indice: number): void {
    this.plan.update((plan) =>
      plan.map((fase, i) =>
        i === indiceFase ? { ...fase, entregables: fase.entregables.filter((_, j) => j !== indice) } : fase,
      ),
    );
  }

  protected crear(): void {
    if (!this.puedeCrear()) {
      return;
    }
    this.creando.set(true);
    this.error.set(null);
    const cotizacion = this.cotizacion() ?? null;
    this.proyectosApi
      .crear({
        cotizacionId: cotizacion,
        cliente: cotizacion ? null : { nombre: this.clienteNombre().trim(), correo: this.clienteCorreo().trim() },
        impuestoPorcentaje: cotizacion ? null : this.impuesto(),
        descripcion: {
          nombre: this.nombre().trim(),
          descripcion: this.descripcion().trim() || null,
          inicio: this.inicio(),
          entregaEstimada: this.entregaEstimada() || null,
        },
        plan: this.plan(),
      })
      .subscribe({
        next: (proyecto) => this.router.navigate(['/admin/proyectos', proyecto.id]),
        error: (respuesta: HttpErrorResponse) => {
          this.creando.set(false);
          this.error.set(this.mensajeDe(respuesta));
        },
      });
  }

  private faseVacia(numero: number): FaseDelPlan {
    return {
      fase: { nombre: `Fase ${numero}`, objetivo: null, inicioPlaneado: null, finPlaneado: null },
      entregables: [],
    };
  }

  /** El servidor explica qué falló (`{ mensaje }`); si no, el texto genérico. */
  private mensajeDe(respuesta: HttpErrorResponse): string {
    const mensaje = (respuesta.error as { mensaje?: string } | null)?.mensaje;
    return mensaje ?? this.t.error;
  }
}
