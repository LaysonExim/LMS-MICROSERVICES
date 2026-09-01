FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY customer-service/pom.xml ./customer-service/
COPY customer-service/src ./customer-service/src
RUN mvn clean package -DskipTests -pl customer-service -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/customer-service/target/customer-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
