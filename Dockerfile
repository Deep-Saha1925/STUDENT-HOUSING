# ── Build stage ──
# Compiles the jar. This stage's tools (Maven, full JDK, source code) never
# make it into the final image — only the built jar gets copied out below.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy just the POM first so dependency downloads are cached as their own
# Docker layer — this layer only invalidates when pom.xml changes, not on
# every source-code edit, which makes rebuilds much faster during development.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the rest of the source and build the jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Runtime stage ──
# Alpine-based JRE-only image — small, and has no build tooling or source
# code in it at all, just the JRE and the one jar file it needs to run.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/student-housing-0.0.1-SNAPSHOT.jar app.jar

# Documentation only — Render (and most PaaS hosts) assign the real port via
# the $PORT env var at runtime, which application.properties already binds
# to via server.port=${PORT:8080}.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
