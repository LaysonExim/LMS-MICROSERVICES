FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY audit-service/pom.xml ./audit-service/
COPY audit-service/src ./audit-service/src
RUN mvn clean package -DskipTests -pl audit-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/audit-service/target/audit-service-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
