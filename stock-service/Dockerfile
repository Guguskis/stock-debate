FROM openjdk:14-jdk-alpine
COPY /target/*.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar"]

EXPOSE 8083
