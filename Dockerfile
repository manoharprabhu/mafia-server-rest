FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .
COPY gradle/ gradle/
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:23-jre
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=build /app/build/libs/*.jar app.jar


RUN chown spring:spring app.jar
USER spring

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
