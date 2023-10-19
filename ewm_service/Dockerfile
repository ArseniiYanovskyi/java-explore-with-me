FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar ewm_service_server.jar
ENTRYPOINT ["java", "-jar", "ewm_service_server.jar"]