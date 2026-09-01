FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY monitoring-service/pom.xml ./monitoring-service/
COPY monitoring-service/src ./monitoring-service/src
RUN mvn clean package -DskipTests -pl monitoring-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/monitoring-service/target/monitoring-service-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
