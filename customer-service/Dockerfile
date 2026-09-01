# Purpose: Dockerfile for Customer Service
# File: customer-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl customer-service -am
# 2. Then build Docker image: docker build -t customer-service ./customer-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/customer-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
