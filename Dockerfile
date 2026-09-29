# Etapa 1: Construcción (Generar el JAR)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
# Compilamos saltando los tests para no sobrecargar tu procesador Celeron
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Ultra ligera con Java 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos el JAR usando el nombre exacto de tu pom.xml: users-management-2.1.0.jar
COPY --from=build /app/target/users-management-2.1.0.jar app.jar
EXPOSE 8080
# Límite de memoria estricto para proteger tus 8GB de RAM
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
