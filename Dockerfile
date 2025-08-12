FROM ubuntu:latest AS build
LABEL authors="luis-chiqueto"

RUN apt-get update
RUN apt-get install openjdk-21-jdk -y
COPY src/main/java/com/senai/gestao_beneficios .

RUN apt-get install maven -y
RUN mvn clean install

FROM openjdk:21-jdk-slim

EXPOSE 3000

COPY --from=build /target/gestao-beneficios-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]