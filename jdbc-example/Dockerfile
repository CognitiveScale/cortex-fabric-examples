FROM gradle:jdk11 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build

FROM openjdk:11-jre-slim-bullseye
COPY --from=builder /app/build/distributions/jdbc-example-1.0-SNAPSHOT.tar jdbc-example-1.0-SNAPSHOT.tar
RUN tar -xvf jdbc-example-1.0-SNAPSHOT.tar
WORKDIR jdbc-example-1.0-SNAPSHOT
EXPOSE 5000
CMD ["./bin/jdbc-example"]
