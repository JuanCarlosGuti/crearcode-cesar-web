import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormField, required, schema, form } from '@angular/forms/signals';

import { AuthApi } from '../../api/auth-api';
import { SesionService } from '../../nucleo/sesion';
import {
  CUENTA,
  MENSAJE_ERROR_CONTRASENA_REQUERIDA,
  MENSAJE_ERROR_CORREO_REQUERIDO,
} from '../../../contenido/cuenta';
import { METADATOS_INGRESO } from '../../../contenido/metadatos-paginas';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

interface DatosIngreso {
  correo: string;
  contrasena: string;
}

const ESQUEMA_INGRESO = schema<DatosIngreso>((campo) => {
  required(campo.correo, { message: MENSAJE_ERROR_CORREO_REQUERIDO });
  required(campo.contrasena, { message: MENSAJE_ERROR_CONTRASENA_REQUERIDA });
});

@Component({
  selector: 'app-pagina-ingreso',
  templateUrl: './ingreso.html',
  styleUrl: './ingreso.scss',
  imports: [FormField, RouterLink],
})
export class IngresoPage {
  private readonly authApi = inject(AuthApi);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  protected readonly textos = CUENTA.ingreso;
  protected readonly enviando = signal(false);
  protected readonly errorCredenciales = signal(false);
  protected readonly errorSinVerificar = signal(false);
  protected readonly errorEnvio = signal(false);

  private readonly datos = signal<DatosIngreso>({ correo: '', contrasena: '' });
  protected readonly formulario = form(this.datos, ESQUEMA_INGRESO);

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_INGRESO, ruta: '/ingreso' }));
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }

    this.enviando.set(true);
    this.errorCredenciales.set(false);
    this.errorSinVerificar.set(false);
    this.errorEnvio.set(false);
    const { correo, contrasena } = this.datos();
    this.authApi.login(correo, contrasena).subscribe({
      next: (sesion) => {
        this.sesion.iniciarSesion(sesion);
        this.enviando.set(false);
        this.router.navigateByUrl(sesion.rol === 'ADMIN' ? '/admin' : '/mi-cuenta');
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        if (error instanceof HttpErrorResponse && error.status === 401) {
          this.errorCredenciales.set(true);
        } else if (error instanceof HttpErrorResponse && error.status === 403) {
          this.errorSinVerificar.set(true);
        } else {
          this.errorEnvio.set(true);
        }
      },
    });
  }
}
