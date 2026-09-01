FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY reporting-service/pom.xml ./reporting-service/
COPY reporting-service/src ./reporting-service/src
RUN mvn clean package -DskipTests -pl reporting-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/reporting-service/target/reporting-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
