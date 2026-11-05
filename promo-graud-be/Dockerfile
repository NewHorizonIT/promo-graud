# ---- Stage 1: Build ----
# Build context = repo root (set via railway.toml) so checkstyle.xml is reachable
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY checkstyle.xml /checkstyle.xml
COPY promo-graud-be/pom.xml .
RUN mvn dependency:go-offline -B

COPY promo-graud-be/src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]