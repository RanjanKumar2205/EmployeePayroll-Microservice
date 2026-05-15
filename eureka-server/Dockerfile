FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
COPY build/libs/eureka-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
