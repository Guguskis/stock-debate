FROM openjdk:14-jdk-alpine
RUN apk add --no-cache --upgrade bash
RUN apk add --no-cache --upgrade curl

COPY /target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT [ "java", "-jar", "app.jar"]