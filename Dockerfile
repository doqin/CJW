# --- Build Stage ---
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy only gradle files first
COPY build.gradle* settings.gradle* gradle/ ./

# Create an empty src folder to avoid COPY error if missing
RUN mkdir -p src

# Download dependencies early
RUN gradle build --no-daemon || return 0

# Copy the rest of the project
COPY . .

# Build the app
RUN gradle clean build --no-daemon

# --- Runtime Stage ---
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the built jar
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
