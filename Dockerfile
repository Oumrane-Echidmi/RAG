# syntax=docker/dockerfile:1

# ─── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

# Cache dependencies separately from source changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

# Build the application
COPY src/ src/
RUN ./mvnw -B -ntp -DskipTests package

# ─── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre

WORKDIR /app

# Run as an unprivileged user
RUN groupadd --system spring && useradd --system --gid spring --home-dir /app spring

COPY --from=build --chown=spring:spring /workspace/target/*.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
