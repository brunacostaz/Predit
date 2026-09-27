FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache curl
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY identity-service/pom.xml identity-service/pom.xml
COPY risk-service/pom.xml risk-service/pom.xml
RUN chmod +x mvnw && ./mvnw --batch-mode -pl risk-service -am dependency:go-offline

COPY risk-service/src risk-service/src
RUN ./mvnw --batch-mode -pl risk-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S predit && adduser -S predit -G predit
WORKDIR /app
COPY --from=build /workspace/risk-service/target/risk-service-1.0.0.jar app.jar
USER predit
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
