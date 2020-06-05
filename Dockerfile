FROM maven:3.6.3-jdk-8 AS builder
RUN mkdir webservice
WORKDIR webservice
COPY . .
RUN mvn clean package -Dmaven.test.skip=true \
    && mv target/webservice-*.jar /webservice.jar

FROM openjdk:8
COPY --from=builder /webservice.jar /webservice.jar
ENTRYPOINT ["java", "-jar", "/webservice.jar"]