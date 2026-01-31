# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /workspace
COPY backend/ .
RUN ./gradlew --no-daemon clean bootJar -x test

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/oneonone-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
