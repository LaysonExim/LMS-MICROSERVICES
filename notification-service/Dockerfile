# Purpose: Dockerfile for Notification Service
# File: notification-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl notification-service -am
# 2. Then build Docker image: docker build -t notification-service ./notification-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/notification-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
