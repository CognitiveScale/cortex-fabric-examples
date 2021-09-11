FROM adoptopenjdk/openjdk8:jre8u252-b09-alpine
EXPOSE 8080
ADD /jdbc-plugin-cdata/build/libs/jdbc-plugin-cdata.jar plugin.jar
ADD /jdbc-service-harness/build/libs/jdbc-service-harness.jar server.jar
CMD ["/bin/sh","-c","java -jar /server.jar"]
