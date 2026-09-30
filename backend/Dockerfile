# Etapa de build: solo necesaria para compilar, no queda en la imagen final.
#
# Imagen con Maven YA instalado en vez de temurin + Maven Wrapper: el
# wrapper descarga el distro de Maven con un cliente Java simple, y
# Maven Central empezo a responder 403 a esa descarga desde los runners
# de CI (11 ago 2026) — el build fallaba antes de compilar una sola
# clase. Las dependencias del proyecto se siguen bajando de Central
# normalmente; lo que se evita es descargar Maven mismo.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src/ src/
RUN mvn -B -DskipTests package

# Etapa de ejecucion: solo el JRE y el jar ya construido.
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Usuario sin privilegios: por omision el proceso corre como root
# dentro del contenedor, asi que cualquier ejecucion de codigo lo hace
# con todo el poder en ese espacio de nombres — y este proceso tiene a
# mano la base de datos y los secretos del entorno. No cuesta nada: el
# jar solo necesita leerse a si mismo y escuchar en 8080, que no es
# privilegiado (auditoria del 28 sep 2026, P3-b).
RUN addgroup -S app && adduser -S app -G app && chown -R app:app /app
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
