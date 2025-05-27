# Use a openjdk:24-jdk as the base image
FROM openjdk:24-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Run the Spring Boot application using Maven
CMD ["./mvnw", "spring-boot:run"]