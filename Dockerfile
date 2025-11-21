# Versión mínima - solo lo esencial
FROM eclipse-temurin:17-jre

# Solo curl para health check (más rápido)
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083

# Health check con curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8083/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]