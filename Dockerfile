# syntax=docker/dockerfile:1

##
# Dockerfile único do monorepo.
#
# Exemplos:
#   docker build --build-arg MODULE=gateway -t zenith-gateway .
#   docker build --build-arg MODULE=core-svc -t zenith-core-svc .
#   docker build --build-arg MODULE=analise-svc -t zenith-analise-svc .
##

FROM maven:3.9.11-eclipse-temurin-21 AS build

ARG MODULE=core-svc

WORKDIR /workspace

# Copia os POMs primeiro para aproveitar cache de dependências.
COPY pom.xml .
COPY gateway/pom.xml gateway/pom.xml
COPY core-svc/pom.xml core-svc/pom.xml
COPY analise-svc/pom.xml analise-svc/pom.xml

RUN mvn -q -pl ${MODULE} -am dependency:go-offline

# Copia o código-fonte após resolver dependências.
COPY gateway gateway
COPY core-svc core-svc
COPY analise-svc analise-svc

RUN mvn -q -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:21-jre AS runtime

ARG MODULE=core-svc

WORKDIR /app

COPY --from=build /workspace/${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080 8081 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
