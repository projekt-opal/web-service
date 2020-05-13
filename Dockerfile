FROM maven:3.6.3-jdk-8
RUN mkdir webservice
WORKDIR webservice
COPY . .
RUN mvn clean package
ENTRYPOINT ["java","-jar","target/webservice-1.0.0.jar"]
