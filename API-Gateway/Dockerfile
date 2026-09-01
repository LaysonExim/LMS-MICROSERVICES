# Purpose: Dockerfile for API Gateway
# File: API-Gateway/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl API-Gateway -am
# 2. Then build Docker image: docker build -t api-gateway ./API-Gateway

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
