# Purpose: Dockerfile for Audit Service
# File: audit-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl audit-service -am
# 2. Then build Docker image: docker build -t audit-service ./audit-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/audit-service-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
