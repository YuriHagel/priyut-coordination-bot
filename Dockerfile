FROM gradle:8.14-jdk25 AS build

WORKDIR /workspace
COPY . .
RUN gradle clean build -x test

FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]