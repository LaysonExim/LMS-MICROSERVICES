# Purpose: Dockerfile for Collateral Service
# File: collateral-service/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl collateral-service -am
# 2. Then build Docker image: docker build -t collateral-service ./collateral-service

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/collateral-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
