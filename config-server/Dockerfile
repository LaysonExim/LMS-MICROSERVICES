# Purpose: Dockerfile for Config Server
# File: config-server/Dockerfile
#
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl config-server -am
# 2. Then build Docker image: docker build -t config-server ./config-server

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
