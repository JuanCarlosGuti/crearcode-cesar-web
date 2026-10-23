import { Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormField, required, schema, form } from '@angular/forms/signals';

import { AuthApi } from '../../api/auth-api';
import { rutaDeVuelta } from '../../nucleo/ruta-de-vuelta';
import { SesionService } from '../../nucleo/sesion';

interface DatosLogin {
  correo: string;
  contrasena: string;
}

const ESQUEMA_LOGIN = schema<DatosLogin>((campo) => {
  required(campo.correo, { message: 'Escribe tu correo.' });
  required(campo.contrasena, { message: 'Escribe tu contraseña.' });
});

@Component({
  selector: 'app-pagina-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
  imports: [FormField],
})
export class LoginPage {
  private readonly authApi = inject(AuthApi);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  /** `?volver=`: la página del panel que pidió ingresar (ISS-241). */
  readonly volver = input<string>();

  private readonly datos = signal<DatosLogin>({ correo: '', contrasena: '' });
  protected readonly formulario = form(this.datos, ESQUEMA_LOGIN);

  protected readonly enviando = signal(false);
  protected readonly error = signal(false);

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }

    this.enviando.set(true);
    this.error.set(false);
    const { correo, contrasena } = this.datos();
    this.authApi.login(correo, contrasena).subscribe({
      next: (sesion) => {
        this.sesion.iniciarSesion(sesion);
        this.enviando.set(false);
        this.router.navigateByUrl(rutaDeVuelta(this.volver(), 'ADMIN') ?? '/admin');
      },
      error: () => {
        this.enviando.set(false);
        this.error.set(true);
      },
    });
  }
}
