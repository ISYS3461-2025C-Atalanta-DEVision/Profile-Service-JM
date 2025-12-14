# Multi-module Dockerfile for Render Deployment
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy all pom.xml files first (for dependency caching)
COPY pom.xml ./pom.xml
COPY profile-api/pom.xml ./profile-api/pom.xml
COPY profile-core/pom.xml ./profile-core/pom.xml
COPY profile-app/pom.xml ./profile-app/pom.xml

# Create source directories
RUN mkdir -p profile-api/src profile-core/src profile-app/src

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code for all modules
COPY profile-api/src ./profile-api/src
COPY profile-core/src ./profile-core/src
COPY profile-app/src ./profile-app/src

# Build all modules (profile-app depends on profile-api and profile-core)
RUN mvn clean package -DskipTests -B -pl profile-app -am

# Runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Add curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the built jar from profile-app module
COPY --from=builder /app/profile-app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port (Render will override with PORT env var)
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8082}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
