# --- Build Stage ---
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and config files
COPY gradlew gradlew.bat gradle.properties ./
COPY gradle ./gradle

# Copy root project files
COPY settings.gradle ./

# Copy the actual app module
COPY app ./app

# Build the project
RUN ./gradlew :app:build --no-daemon

# --- Runtime Stage ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=builder /app/app/build/libs/*.jar app.jar

EXPOSE 8080
EXPOSE 42069

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
