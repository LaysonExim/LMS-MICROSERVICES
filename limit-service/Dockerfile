FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY limit-service/pom.xml ./limit-service/
COPY limit-service/src ./limit-service/src
RUN mvn clean package -DskipTests -pl limit-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/limit-service/target/limit-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
