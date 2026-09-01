# Purpose: Dockerfile for Discovery Server
# File: discovery-server/Dockerfile
# 
# BUILD INSTRUCTIONS:
# 1. First build the JAR: ./mvnw clean package -DskipTests -pl discovery-server -am
# 2. Then build Docker image: docker build -t discovery-server ./discovery-server

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/discovery-server-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
