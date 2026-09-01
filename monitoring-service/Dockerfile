# Purpose: Dockerfile for Monitoring Service
# File: monitoring-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl monitoring-service -am
# 2. Then build Docker image: docker build -t monitoring-service ./monitoring-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/monitoring-service-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
