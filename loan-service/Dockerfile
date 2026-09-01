# Purpose: Dockerfile for Loan Service
# File: loan-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl loan-service -am
# 2. Then build Docker image: docker build -t loan-service ./loan-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/loan-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
