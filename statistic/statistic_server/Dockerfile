FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar statistic_server.jar
ENTRYPOINT ["java", "-jar", "statistic_server.jar"]