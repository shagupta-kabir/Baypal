# Build stage: use the Java 21 LTS, matching the project.
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the Maven descriptor first for better Docker layer caching.
COPY pom.xml .

# Download dependencies.
RUN mvn -B dependency:go-offline

# Copy source and build the executable Spring Boot jar.
COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage: smaller image with Java 21.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
