import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormField, required, pattern, schema, form } from '@angular/forms/signals';

import { AuthApi } from '../../api/auth-api';
import { CUENTA, MENSAJE_ERROR_CORREO_CUENTA } from '../../../contenido/cuenta';

interface DatosReenvio {
  correo: string;
}

const FORMATO_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const ESQUEMA_REENVIO = schema<DatosReenvio>((campo) => {
  required(campo.correo, { message: MENSAJE_ERROR_CORREO_CUENTA });
  pattern(campo.correo, FORMATO_CORREO, {
    message: MENSAJE_ERROR_CORREO_CUENTA,
    when: ({ value }) => value().length > 0,
  });
});

/**
 * Destino del enlace del correo de verificación (?token=...). Solo se
 * renderiza en el cliente (RenderMode.Client): el token viene en la
 * URL y la llamada tiene efectos, nada que prerenderizar.
 */
@Component({
  selector: 'app-pagina-verificar-correo',
  templateUrl: './verificar-correo.html',
  styleUrl: './verificar-correo.scss',
  imports: [FormField, RouterLink],
})
export class VerificarCorreoPage implements OnInit {
  private readonly authApi = inject(AuthApi);

  readonly token = input<string>();

  protected readonly textos = CUENTA.verificacion;
  protected readonly estado = signal<'verificando' | 'exito' | 'error'>('verificando');
  protected readonly reenviando = signal(false);
  protected readonly reenvioEnviado = signal(false);

  private readonly datos = signal<DatosReenvio>({ correo: '' });
  protected readonly formulario = form(this.datos, ESQUEMA_REENVIO);

  ngOnInit(): void {
    const token = this.token();
    if (!token) {
      this.estado.set('error');
      return;
    }
    this.authApi.verificarCorreo(token).subscribe({
      next: () => this.estado.set('exito'),
      error: () => this.estado.set('error'),
    });
  }

  protected reenviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }

    this.reenviando.set(true);
    this.authApi.reenviarVerificacion(this.datos().correo).subscribe({
      // La respuesta es 202 incondicional; incluso ante un fallo de red
      // mostramos el mismo mensaje generico: no revela nada y el
      // visitante siempre puede reintentar desde su correo.
      next: () => {
        this.reenviando.set(false);
        this.reenvioEnviado.set(true);
      },
      error: () => {
        this.reenviando.set(false);
        this.reenvioEnviado.set(true);
      },
    });
  }
}
