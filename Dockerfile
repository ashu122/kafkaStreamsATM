FROM openjdk:8-alpine

COPY target/kafka-streams-atm.jar /kafka-streams-atm.jar

ENTRYPOINT ["java", "-jar", "kafka-streams-atm.jar"]