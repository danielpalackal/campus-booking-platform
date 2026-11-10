# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy the Maven descriptor first to improve Docker layer caching.
COPY backend/pom.xml backend/pom.xml
COPY backend/checkstyle.xml backend/checkstyle.xml

# Download dependencies before copying the source code.
RUN mvn -f backend/pom.xml dependency:go-offline

# Copy the backend source, including the bundled frontend files.
COPY backend/src backend/src

# Build the executable Spring Boot JAR.
RUN mvn -f backend/pom.xml clean package -DskipTests

# Fail the image build if the AWS MySQL runtime driver is missing from the
# executable Spring Boot JAR.
RUN executable_jar="$(find /build/backend/target -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' -print -quit)" \
    && test -n "$executable_jar" \
    && jar tf "$executable_jar" | grep -q 'BOOT-INF/lib/mysql-connector-j-'

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the JAR produced by the build stage.
COPY --from=build /build/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
