FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY API-Gateway/pom.xml ./API-Gateway/
COPY API-Gateway/src ./API-Gateway/src
RUN mvn clean package -DskipTests -pl API-Gateway -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/API-Gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
