# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

# Primero solo lo necesario para descargar las dependencias: así se reutiliza la caché
# de Docker mientras no cambie el pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# La aplicación no se ejecuta como root
RUN groupadd --system app && useradd --system --gid app --home-dir /app app

COPY --from=build /build/target/*.jar app.jar

# Carpeta de las fotos subidas (ruta relativa "uploads" respecto a /app)
RUN mkdir -p /app/uploads && chown -R app:app /app
USER app
VOLUME /app/uploads

EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "app.jar"]
