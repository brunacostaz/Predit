FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S predit && adduser -S predit -G predit
WORKDIR /app
COPY identity-service/target/identity-service-1.0.0.jar app.jar
USER predit
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
