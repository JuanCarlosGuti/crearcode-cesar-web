import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormField, required, pattern, schema, form, validate } from '@angular/forms/signals';

import { AuthApi } from '../../api/auth-api';
import {
  BENEFICIOS_CUENTA,
  CUENTA,
  MENSAJE_ERROR_CONTRASENAS_NO_COINCIDEN,
  MENSAJE_ERROR_CONTRASENA_CORTA,
  MENSAJE_ERROR_CORREO_CUENTA,
  MENSAJE_ERROR_POLITICA,
} from '../../../contenido/cuenta';
import { METADATOS_REGISTRO } from '../../../contenido/metadatos-paginas';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

interface DatosRegistro {
  correo: string;
  contrasena: string;
  confirmacion: string;
  aceptaPolitica: boolean;
}

const FORMATO_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const LONGITUD_MINIMA_CONTRASENA = 10;

const ESQUEMA_REGISTRO = schema<DatosRegistro>((campo) => {
  required(campo.correo, { message: MENSAJE_ERROR_CORREO_CUENTA });
  pattern(campo.correo, FORMATO_CORREO, {
    message: MENSAJE_ERROR_CORREO_CUENTA,
    when: ({ value }) => value().length > 0,
  });

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

  required(campo.aceptaPolitica, { message: MENSAJE_ERROR_POLITICA });
});

@Component({
  selector: 'app-pagina-registro',
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
  imports: [FormField, RouterLink],
})
export class RegistroPage {
  private readonly authApi = inject(AuthApi);

  protected readonly textos = CUENTA.registro;
  protected readonly beneficios = BENEFICIOS_CUENTA;
  protected readonly enviando = signal(false);
  protected readonly exito = signal(false);
  protected readonly correoRegistrado = signal('');
  protected readonly errorYaRegistrada = signal(false);
  protected readonly errorEnvio = signal(false);

  private readonly datos = signal<DatosRegistro>({
    correo: '',
    contrasena: '',
    confirmacion: '',
    aceptaPolitica: false,
  });

  protected readonly formulario = form(this.datos, ESQUEMA_REGISTRO);

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_REGISTRO, ruta: '/registro' }));
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }

    this.enviando.set(true);
    this.errorYaRegistrada.set(false);
    this.errorEnvio.set(false);
    const { correo, contrasena } = this.datos();
    this.authApi.registrar(correo, contrasena).subscribe({
      next: () => {
        this.enviando.set(false);
        this.correoRegistrado.set(correo);
        this.exito.set(true);
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        if (error instanceof HttpErrorResponse && error.status === 409) {
          this.errorYaRegistrada.set(true);
        } else {
          this.errorEnvio.set(true);
        }
      },
    });
  }
}
