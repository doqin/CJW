# --- Build Stage ---
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy only build files for caching
COPY app/build.gradle app/settings.gradle ./app/
COPY gradle ./gradle
COPY gradlew gradlew.bat gradle.properties ./

# Copy the actual source
COPY app/src ./app/src

# Run build inside the app folder
WORKDIR /app/app
RUN ../gradlew clean build --no-daemon

# --- Runtime Stage ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the generated JAR from build stage
COPY --from=builder /app/app/build/libs/*.jar app.jar

EXPOSE 6969

# Optional: fine-tune memory usage for Render
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
