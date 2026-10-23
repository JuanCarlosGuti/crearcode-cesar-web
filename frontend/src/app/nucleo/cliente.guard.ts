import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { PARAMETRO_VOLVER } from './ruta-de-vuelta';
import { SesionService } from './sesion';

// Sin sesión manda a ingresar y deja dicho a dónde volver: el enlace del
// correo a un proyecto termina en el proyecto, no en /mi-cuenta (ISS-241).
export const clienteGuard: CanActivateFn = (_ruta, estado) => {
  const sesion = inject(SesionService);
  const router = inject(Router);

  return sesion.esCliente()
    ? true
    : router.createUrlTree(['/ingreso'], { queryParams: { [PARAMETRO_VOLVER]: estado.url } });
};
