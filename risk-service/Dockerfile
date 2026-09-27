FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S predit && adduser -S predit -G predit
WORKDIR /app
COPY risk-service/target/risk-service-1.0.0.jar app.jar
USER predit
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
