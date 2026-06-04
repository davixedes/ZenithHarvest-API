FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY src ./src
RUN ./mvnw package -B -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
