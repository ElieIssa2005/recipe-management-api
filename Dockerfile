# Use Java 21 as the base image for building
FROM eclipse-temurin:21-jdk-alpine as build

# Set working directory
WORKDIR /app

# Copy gradle configuration files
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Ensure Gradle wrapper is executable
RUN chmod +x ./gradlew

# Run initial Gradle tasks to cache dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code (this includes src/main/java, src/main/resources, etc.)
COPY src src

# Generate Javadoc.
# As per build.gradle.kts, output will go directly to /app/src/main/resources/static/apidocs/
# This step ensures the apidocs directory is populated before the JAR is built.
RUN ./gradlew javadoc --no-daemon

# The mkdir and cp commands that were here are NO LONGER NEEDED
# because Javadoc is now generated directly into the static resources path
# by the `gradlew javadoc` command above, thanks to the destinationDir setting in build.gradle.kts.

# Build the application.
# This will package files from src/main/resources (including static/apidocs) into the JAR.
RUN ./gradlew build -x test --no-daemon

# Create a lean runtime image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Add diagnostic command to print environment variables (sanitizing sensitive info)
RUN echo "Diagnostics will be printed on startup"

# Expose the port the app will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dlogging.level.org.springframework.data.mongodb=DEBUG", "-jar", "app.jar"]