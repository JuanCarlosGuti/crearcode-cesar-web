import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { SesionService } from './sesion';

const PREFIJO_AUTH = '/api/auth/';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const sesion = inject(SesionService);
  const router = inject(Router);

  // Ningun endpoint de /api/auth/ recibe el Bearer: todos son publicos
  // y sus errores (401 de credenciales, 400 de token de correo) los
  // maneja la propia pagina que los llamo, no el manejo global.
  const esPeticionDeAuth = req.url.includes(PREFIJO_AUTH);
  const token = sesion.obtenerToken();

  const peticion = token && !esPeticionDeAuth
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: unknown) => {
      if (!esPeticionDeAuth && error instanceof HttpErrorResponse && error.status === 401) {
        sesion.cerrarSesion();
        // El destino depende de la pagina en la que esta parada la
        // persona (router.url), no de la URL del request fallido: un
        // 401 en una pagina publica solo limpia la sesion vencida.
        const paginaActual = router.url;
        if (paginaActual.startsWith('/admin')) {
          router.navigateByUrl('/admin/login');
        } else if (paginaActual.startsWith('/mi-cuenta')) {
          router.navigateByUrl('/ingreso');
        }
      }
      return throwError(() => error);
    }),
  );
};
