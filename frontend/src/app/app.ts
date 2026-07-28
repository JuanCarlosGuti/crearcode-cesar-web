import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';

import { ChatAsistente } from './componentes/chat-asistente/chat-asistente';
import { Footer } from './layout/footer/footer';
import { Header } from './layout/header/header';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer, ChatAsistente],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  private readonly urlActual = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  // El panel admin (detras de login) no lleva el header/footer del
  // sitio publico: es una seccion distinta de la app, no una pagina de
  // contenido mas (ver ADR-08 en docs/02-arquitectura.md).
  protected readonly esRutaAdmin = computed(() => this.urlActual().startsWith('/admin'));
}
