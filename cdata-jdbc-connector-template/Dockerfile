FROM gradle:6.0.1-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build

FROM adoptopenjdk/openjdk8:jre8u252-b09-alpine
WORKDIR /app
RUN apk --no-cache add curl
COPY --from=builder /app/jdbc-plugin-cdata/build/libs/jdbc-plugin-cdata.jar plugin.jar
COPY --from=builder /app/jdbc-service-harness/build/libs/jdbc-service-harness.jar server.jar
EXPOSE 8080
CMD ["/bin/sh","-c","java -jar /app/server.jar"]
