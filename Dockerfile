FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY . .
# Ensure gradlew is executable
RUN chmod +x gradlew
EXPOSE 8080
CMD ["./gradlew", "clean", "bootRun"]