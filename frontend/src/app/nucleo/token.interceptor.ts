import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { SesionService } from './sesion';

const RUTA_LOGIN = '/api/auth/login';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const sesion = inject(SesionService);
  const router = inject(Router);

  const esPeticionDeLogin = req.url.includes(RUTA_LOGIN);
  const token = sesion.obtenerToken();

  const peticion = token && !esPeticionDeLogin
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: unknown) => {
      // Un 401 en el propio login es "credenciales incorrectas": lo
      // debe mostrar el formulario de login, no disparar el manejo
      // global de sesion invalida (que redirigiria a la misma pagina
      // en la que la persona ya esta parada).
      if (!esPeticionDeLogin && error instanceof HttpErrorResponse && error.status === 401) {
        sesion.cerrarSesion();
        router.navigateByUrl('/admin/login');
      }
      return throwError(() => error);
    }),
  );
};
