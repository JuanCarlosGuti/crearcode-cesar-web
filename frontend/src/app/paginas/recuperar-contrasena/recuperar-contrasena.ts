import { Component, inject, signal } from '@angular/core';
import { FormField, required, pattern, schema, form } from '@angular/forms/signals';

import { AuthApi } from '../../api/auth-api';
import { CUENTA, MENSAJE_ERROR_CORREO_CUENTA } from '../../../contenido/cuenta';
import { METADATOS_RECUPERAR } from '../../../contenido/metadatos-paginas';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

interface DatosRecuperacion {
  correo: string;
}

const FORMATO_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const ESQUEMA_RECUPERACION = schema<DatosRecuperacion>((campo) => {
  required(campo.correo, { message: MENSAJE_ERROR_CORREO_CUENTA });
  pattern(campo.correo, FORMATO_CORREO, {
    message: MENSAJE_ERROR_CORREO_CUENTA,
    when: ({ value }) => value().length > 0,
  });
});

@Component({
  selector: 'app-pagina-recuperar-contrasena',
  templateUrl: './recuperar-contrasena.html',
  styleUrl: './recuperar-contrasena.scss',
  imports: [FormField],
})
export class RecuperarContrasenaPage {
  private readonly authApi = inject(AuthApi);

  protected readonly textos = CUENTA.recuperacion;
  protected readonly enviando = signal(false);
  protected readonly enviado = signal(false);
  protected readonly errorEnvio = signal(false);

  private readonly datos = signal<DatosRecuperacion>({ correo: '' });
  protected readonly formulario = form(this.datos, ESQUEMA_RECUPERACION);

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_RECUPERAR, ruta: '/recuperar-contrasena' }));
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }

    this.enviando.set(true);
    this.errorEnvio.set(false);
    this.authApi.solicitarRecuperacion(this.datos().correo).subscribe({
      next: () => {
        this.enviando.set(false);
        this.enviado.set(true);
      },
      error: () => {
        this.enviando.set(false);
        this.errorEnvio.set(true);
      },
    });
  }
}
