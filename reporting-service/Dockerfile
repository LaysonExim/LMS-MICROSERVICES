# Purpose: Dockerfile for Reporting Service
# File: reporting-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl reporting-service -am
# 2. Then build Docker image: docker build -t reporting-service ./reporting-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/reporting-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
