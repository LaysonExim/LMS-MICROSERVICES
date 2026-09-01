FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY loan-service/pom.xml ./loan-service/
COPY loan-service/src ./loan-service/src
RUN mvn clean package -DskipTests -pl loan-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/loan-service/target/loan-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
