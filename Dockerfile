FROM openjdk:8
COPY target/webservice-1.0.0.jar ./webservice.jar
ENTRYPOINT ["java","-jar","webservice.jar"]
