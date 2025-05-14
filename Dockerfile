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

# Generate Javadoc. Output will be in /app/build/docs/javadoc/ within this build stage
RUN ./gradlew javadoc --no-daemon

# Create the target directory in src/main/resources for Spring Boot to pick up
# This directory needs to exist *before* the JAR is built by the next command.
RUN mkdir -p /app/src/main/resources/static/apidocs

# Copy the generated Javadoc from build/docs/javadoc into src/main/resources/static/apidocs
# This makes the Javadoc part of the resources that get packaged into the JAR.
RUN cp -r /app/build/docs/javadoc/* /app/src/main/resources/static/apidocs/

# Build the application.
# This will now include the Javadoc from src/main/resources/static/apidocs in the JAR.
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