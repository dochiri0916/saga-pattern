FROM eclipse-temurin:21-jre
ARG SERVICE
WORKDIR /app
COPY ${SERVICE}/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
