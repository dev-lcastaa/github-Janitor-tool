# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Accept build args
ARG GITHUB_API_KEY
ARG DISCORD_REPORT_NOTIFICATION

# Set environment variables
ENV GITHUB_API_KEY=${GITHUB_API_KEY}
ENV DISCORD_REPORT_NOTIFICATION=${DISCORD_REPORT_NOTIFICATION}

# Set the working directory
WORKDIR /app

# Copy the JAR
COPY target/janitor-tool-0.0.1.jar app.jar

# Expose the app port
EXPOSE 9001

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
