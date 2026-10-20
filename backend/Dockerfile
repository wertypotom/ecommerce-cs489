# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the POM first so dependency resolution is cached across source-only changes.
COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
# Tests run here as a build gate (they use in-memory H2, no external services).
RUN mvn -B clean package

# ---------- Runtime stage ----------
# jammy (not alpine): the alpine JRE tag is amd64-only, whereas jammy is
# multi-arch, so this builds on Apple Silicon locally and on Render's amd64.
FROM eclipse-temurin:17-jre-jammy AS runtime

# Non-root runtime user.
RUN addgroup --system spring && adduser --system --ingroup spring spring

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN chown -R spring:spring /app
USER spring

# Container-aware heap sizing; override JAVA_OPTS to tune.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8085

HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
  CMD sh -c 'wget -qO- "http://127.0.0.1:${PORT:-8085}/api/products" >/dev/null || exit 1'

# exec so the JVM receives SIGTERM and can shut down gracefully during redeploys.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
