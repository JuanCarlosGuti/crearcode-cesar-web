import { Component, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormField, required, schema, form, validate } from '@angular/forms/signals';

import { AuthApi } from '../../api/auth-api';
import {
  CUENTA,
  MENSAJE_ERROR_CONTRASENAS_NO_COINCIDEN,
  MENSAJE_ERROR_CONTRASENA_CORTA,
} from '../../../contenido/cuenta';

interface DatosRestablecimiento {
  contrasena: string;
  confirmacion: string;
}

const LONGITUD_MINIMA_CONTRASENA = 10;

const ESQUEMA_RESTABLECIMIENTO = schema<DatosRestablecimiento>((campo) => {
  required(campo.contrasena, { message: MENSAJE_ERROR_CONTRASENA_CORTA });
  validate(campo.contrasena, ({ value }) => {
    if (value().length === 0 || value().length >= LONGITUD_MINIMA_CONTRASENA) {
      return undefined;
    }
    return { kind: 'contrasena-corta', message: MENSAJE_ERROR_CONTRASENA_CORTA };
  });

  validate(campo.confirmacion, ({ value, valueOf }) => {
    if (value() === valueOf(campo.contrasena)) {
      return undefined;
    }
    return { kind: 'contrasenas-no-coinciden', message: MENSAJE_ERROR_CONTRASENAS_NO_COINCIDEN };
  });
});

/**
 * Destino del enlace del correo de recuperación (?token=...). Solo se
 * renderiza en el cliente (RenderMode.Client), igual que
 * /verificar-correo.
 */
@Component({
  selector: 'app-pagina-restablecer-contrasena',
  templateUrl: './restablecer-contrasena.html',
  styleUrl: './restablecer-contrasena.scss',
  imports: [FormField, RouterLink],
})
export class RestablecerContrasenaPage {
  private readonly authApi = inject(AuthApi);

  readonly token = input<string>();

  protected readonly textos = CUENTA.restablecimiento;
  protected readonly enviando = signal(false);
  protected readonly exito = signal(false);
  protected readonly errorEnlace = signal(false);

  private readonly datos = signal<DatosRestablecimiento>({ contrasena: '', confirmacion: '' });
  protected readonly formulario = form(this.datos, ESQUEMA_RESTABLECIMIENTO);

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }

    this.enviando.set(true);
    this.errorEnlace.set(false);
    this.authApi.restablecerContrasena(this.token() ?? '', this.datos().contrasena).subscribe({
      next: () => {
        this.enviando.set(false);
        this.exito.set(true);
      },
      error: () => {
        this.enviando.set(false);
        this.errorEnlace.set(true);
      },
    });
  }
}
