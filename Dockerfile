FROM openjdk:11-jre-slim-buster

ADD build/libs/car-pooling-challenge-0.0.1-SNAPSHOT.jar service.jar

EXPOSE 9091

ENTRYPOINT ["java", "-jar", "service.jar"]
