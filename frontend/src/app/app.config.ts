import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { tokenInterceptor } from './nucleo/token.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withComponentInputBinding(),
      // El CTA del demo en la Home enlaza a /herramientas#demo-diseno (ISS-133).
      withInMemoryScrolling({ anchorScrolling: 'enabled' }),
    ),
    provideClientHydration(),
    provideHttpClient(withFetch(), withInterceptors([tokenInterceptor])),
  ],
};
