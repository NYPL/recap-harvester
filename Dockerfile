# ----- Stage 1: Build the application -----
FROM openjdk:17-slim AS builder

WORKDIR /app

# Copy only the files needed to download dependencies first
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradlew ./gradlew
COPY gradle ./gradle

# Download dependencies. This layer is cached and only re-runs
# if the build.gradle or settings.gradle files change.
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the source code
COPY src ./src

# Build the application. This will reuse the cached dependencies.
RUN ./gradlew build --no-daemon

# ----- Stage 2: Create the final, lightweight image -----
FROM openjdk:17-slim

RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# COPY bulk-import ./bulk-import

# Copy *only* the built application JAR from the 'builder' stage
COPY --from=builder /app/build/libs/harvester-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port and set the command to run the application
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
