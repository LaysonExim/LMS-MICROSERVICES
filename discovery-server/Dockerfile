FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY discovery-server/pom.xml ./discovery-server/
COPY discovery-server/src ./discovery-server/src
RUN mvn clean package -DskipTests -pl discovery-server -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/discovery-server/target/discovery-server-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
