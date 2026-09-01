FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY notification-service/pom.xml ./notification-service/
COPY notification-service/src ./notification-service/src
RUN mvn clean package -DskipTests -pl notification-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/notification-service/target/notification-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
