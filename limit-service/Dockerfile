# Purpose: Dockerfile for Limit Service
# File: limit-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl limit-service -am
# 2. Then build Docker image: docker build -t limit-service ./limit-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/limit-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
