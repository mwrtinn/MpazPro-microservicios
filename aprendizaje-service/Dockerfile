# Compilacion del microservicio con Maven y Java 17.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B -DskipTests package

# Entorno de ejecucion del microservicio.
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Ejecuta la aplicacion sin privilegios de administrador.
USER 10001:10001

EXPOSE 10000

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50.0", "-jar", "/app/app.jar"]