FROM gradle:8.5-jdk21 AS builder
LABEL authors="doqin"

ENV GRADLE_USER_HOME=/home/gradle/.gradle

WORKDIR /app

# Copy Gradle files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build --no-daemon || return 0

# Copy source files and build
COPY src ./src
RUN gradle clean build --no-daemon

# --- Runtime Stage ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy built JAR file from build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 6969

# Render sets PORT env variable dynamically, so your app must support it:
# In application.properties or application.yml, make sure:
# server.port=${PORT:8080}
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]