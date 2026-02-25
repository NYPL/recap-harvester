# ----- Stage 1: Build the application -----
FROM eclipse-temurin:17-jdk AS builder

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

# Save the secrets during build
RUN --mount=type=secret,id=env_file,target=/build/.env \
    cp /build/.env .env

# Build the application. This will reuse the cached dependencies.
RUN ./gradlew build --no-daemon

# ----- Stage 2: Create the final, lightweight image -----
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy *only* the built application JAR from the 'builder' stage
COPY --from=builder /app/build/libs/harvester-0.0.1-SNAPSHOT.jar ./app.jar

COPY ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Copy the secrets during runtime
COPY --from=builder /app/.env /.env

# Expose the port and set the command to run the application
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]