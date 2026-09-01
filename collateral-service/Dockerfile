FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY collateral-service/pom.xml ./collateral-service/
COPY collateral-service/src ./collateral-service/src
RUN mvn clean package -DskipTests -pl collateral-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/collateral-service/target/collateral-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
