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

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
