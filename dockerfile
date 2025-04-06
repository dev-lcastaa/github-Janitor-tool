# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/janitor-tool-0.0.1.jar app.jar

# Expose the port the app runs on
EXPOSE 9001

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]