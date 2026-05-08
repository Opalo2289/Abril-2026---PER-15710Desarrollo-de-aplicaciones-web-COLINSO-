FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY . .
RUN mvn -pl operaciones-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/operaciones-service/target/operaciones-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
