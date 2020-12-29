FROM openjdk:14-jdk-alpine
COPY /target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT [ "java", "-jar", "app.jar"]
