# Imagen del sitio para el servidor propio. Se construye con el contexto
# en frontend/ (igual que backend/Dockerfile con el suyo).
#
# Vuelve a existir tras ADR-12: al pasar a Static Site en Render se
# borro porque alli no hacia falta imagen. En el servidor propio si:
# kamal-proxy enruta por dominio, no sirve archivos, asi que el sitio
# viaja con su propio servidor web dentro.

# Etapa de build: no queda nada de ella en la imagen final.
#
# Version de Node exacta y no "24-alpine": Angular 22 exige 22.22.3,
# 24.15.0 o 26, y un 24.14.1 cumple cualquier ">=24" pero el CLI lo
# rechaza. Eso costo un despliegue el 12 ago 2026 (ver `engines` en
# package.json).
FROM node:24.15.0-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# Prerenderiza las 19 paginas publicas y genera sitemap.xml y robots.txt
# (ADR-06: la URL base sale de contenido/sitio.ts, no se duplica aqui).
RUN npm run build

# Etapa de ejecucion: Caddy y los archivos, nada de Node.
FROM caddy:2-alpine

COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/dist/frontend/browser /srv

EXPOSE 8080
