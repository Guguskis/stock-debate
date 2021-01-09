FROM openjdk:14-jdk-alpine
RUN apk add --no-cache --upgrade bash

COPY /target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT [ "java", "-jar", "app.jar"]