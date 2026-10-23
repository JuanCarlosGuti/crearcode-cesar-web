import { Signal, afterNextRender, computed, inject, signal } from '@angular/core';

import { SesionService } from './sesion';

/**
 * Si hay sesión, pero solo después de hidratar. En el prerender nunca
 * la hay, y si el navegador pintara otra cosa antes de hidratar, la
 * hidratación se rompería: por eso lo que depende de la sesión espera
 * a `afterNextRender`, como el enlace de cuenta del header.
 *
 * <p>
 * Para ocultar a quien ya tiene cuenta lo que lo invita a crearla
 * (ISS-242). Se llama en el contexto de inyección de un componente.
 */
export function sesionTrasHidratar(): Signal<boolean> {
  const sesion = inject(SesionService);
  const hidratado = signal(false);
  afterNextRender(() => hidratado.set(true));
  return computed(() => hidratado() && sesion.estaAutenticado());
}
