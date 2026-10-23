import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { PARAMETRO_VOLVER } from './ruta-de-vuelta';
import { SesionService } from './sesion';

// Como clienteGuard: tras ingresar, de vuelta a la página del panel
// que se pidió (ISS-241).
export const adminGuard: CanActivateFn = (_ruta, estado) => {
  const sesion = inject(SesionService);
  const router = inject(Router);

  return sesion.esAdmin()
    ? true
    : router.createUrlTree(['/admin/login'], { queryParams: { [PARAMETRO_VOLVER]: estado.url } });
};
