FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY config-server/pom.xml ./config-server/
COPY config-server/src ./config-server/src
RUN mvn clean package -DskipTests -pl config-server -am

FROM eclipse-temurin:21-jre-slim
WORKDIR /app
COPY --from=builder /build/config-server/target/config-server-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
